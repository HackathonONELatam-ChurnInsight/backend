package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionResult;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.PredictionPersistenceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.CustomerMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.FeatureImportanceMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.BatchPredictionResultRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.FeatureImportanceRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para persistencia automática de predicciones y gestión del historial.
 */
@Slf4j
@Service
public class PredictionPersistenceService {

    private final PredictionRepository predictionRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PredictionMapper predictionMapper;
    private final ObjectMapper objectMapper;
    private final FeatureImportanceRepository featureImportanceRepository;
    private final FeatureExplainerService featureExplainerService;
    private final FeatureImportanceMapper featureImportanceMapper;
    private final BatchPredictionResultRepository batchPredictionResultRepository;

    public PredictionPersistenceService(
            PredictionRepository predictionRepository,
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            PredictionMapper predictionMapper,
            ObjectMapper objectMapper,
            FeatureImportanceRepository featureImportanceRepository,
            FeatureExplainerService featureExplainerService, FeatureImportanceMapper featureImportanceMapper, BatchPredictionResultRepository batchPredictionResultRepository) {
        this.predictionRepository = predictionRepository;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.predictionMapper = predictionMapper;
        this.objectMapper = objectMapper;
        this.featureImportanceRepository = featureImportanceRepository;
        this.featureExplainerService = featureExplainerService;
        this.featureImportanceMapper = featureImportanceMapper;
        this.batchPredictionResultRepository = batchPredictionResultRepository;
    }

    /**
     * Guarda automáticamente una predicción junto con los datos del cliente.
     */
    @Transactional
    public Prediction savePrediction(PredictionRequestDTO request, PredictionResponseDTO response) {
        log.debug("Iniciando persistencia de predicción");

        // 1. Obtener o crear Customer usando mapper
        Customer customer = getOrCreateCustomer(request);

        // 2. Serializar metadata como JSON
        String metadata = serializeMetadata(request);

        // 3. Determinar resultado de predicción
        Boolean predictionResult = determinePredictionResult(response.forecast());

        // 4. Crear Prediction usando mapper
        Prediction prediction = predictionMapper.toEntity(
                customer,
                predictionResult,
                BigDecimal.valueOf(response.probability()),
                metadata
        );

        Prediction saved = predictionRepository.save(prediction);
        log.info("Predicción guardada: ID={}, Customer={}, Result={}, Probability={}",
                saved.getId(), customer.getCustomerId(), predictionResult, response.probability());
        return saved;
    }

    /**
     * Guarda predicción desde respuesta ML básica (sin features).
     * Método original - mantiene retrocompatibilidad.
     */
    @Transactional
    public Prediction saveMlPrediction(PredictionRequestDTO request, MLPredictionResponseDTO mlResponse) {
        log.debug("Persistencia ML real: forecast={}, probability={}",
                mlResponse.forecast(), mlResponse.probability());
        PredictionResponseDTO response = mlResponse.toPredictionResponseDTO();
        return savePrediction(request, response); // Reusa lógica
    }

    /**
     * Guarda predicción CON feature importances en base de datos (para explicabilidad).
     * Persiste la predicción primero y luego todas las features asociadas en batch.
     * Traduce nombres técnicos a español y calcula impacto (positivo/negativo).
     *
     * @param request datos del cliente
     * @param mlFullResponse respuesta completa del modelo con feature_importances
     * @return predicción guardada con ID generado y features asociadas
     * @throws PredictionPersistenceException si falla el guardado en BD
     */
    @Transactional
    public Prediction saveMlPredictionWithFeatures(
            PredictionRequestDTO request,
            MLPredictionFullResponseDTO mlFullResponse) {

        log.info("Persistiendo predicción completa con features");

        try {
            // 1. Obtener o crear Customer
            Customer customer = getOrCreateCustomer(request);

            // 2. Serializar metadata
            String metadata = serializeMetadata(request);

            // 3. Determinar resultado de predicción
            Boolean predictionResult = mlFullResponse.forecast().equals(1);

            // 4. Crear Prediction usando mapper
            Prediction prediction = predictionMapper.toEntity(
                    customer,
                    predictionResult,
                    BigDecimal.valueOf(mlFullResponse.probability()),
                    metadata
            );

            // 5. Guardar Prediction primero (para obtener el ID)
            Prediction savedPrediction = predictionRepository.save(prediction);
            log.info("Predicción guardada con ID={}", savedPrediction.getId());

            // 6. Guardar Feature Importances asociadas
            if (mlFullResponse.featureImportances() != null && !mlFullResponse.featureImportances().isEmpty()) {
                saveFeatureImportances(savedPrediction, mlFullResponse.featureImportances());
                log.info("Guardadas {} feature importances para predicción ID={}",
                        mlFullResponse.featureImportances().size(), savedPrediction.getId());
            } else {
                log.warn("No hay feature importances para persistir en predicción ID={}", savedPrediction.getId());
            }

            return savedPrediction;

        } catch (Exception e) {
            log.error("Error al persistir predicción con features: {}", e.getMessage(), e);
            throw new PredictionPersistenceException(
                    "No se pudo guardar la predicción con explicabilidad en la base de datos", e);
        }
    }

    /**
     * Guarda todas las feature importances asociadas a una predicción.
     * NUEVO MÉTODO para explicabilidad.
     */
    private void saveFeatureImportances(Prediction prediction, List<MLFeatureImportanceDTO> mlFeatures) {
        List<FeatureImportance> featuresToSave = new ArrayList<>();

        for (MLFeatureImportanceDTO mlFeature : mlFeatures) {
            FeatureImportance feature = FeatureImportance.builder()
                    .prediction(prediction)
                    .name(mlFeature.featureName())
                    .featureValue(mlFeature.featureValue())
                    .rankPosition(mlFeature.ranking())
                    .impactDirection(featureExplainerService.determineImpact(mlFeature.importanceValue()))
                    .displayName(featureExplainerService.translateFeatureName(mlFeature.featureName()))
                    .build();

            featuresToSave.add(feature);
        }

        // Guardar todas las features de una vez
        featureImportanceRepository.saveAll(featuresToSave);
    }

    /**
     * Obtiene o crea un Customer usando MapStruct.
     */
    private Customer getOrCreateCustomer(PredictionRequestDTO request) {

        Customer customer = customerMapper.toEntity(request);
        if (customer.getCustomerId() == null) customer.setCustomerId(UUID.randomUUID().toString());

        return customerRepository.save(customer);

        /*String customerId = UUID.randomUUID().toString();
        return customerRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    // Usa el mapper en vez de builder manual
                    Customer newCustomer = customerMapper.toEntity(request, customerId);
                    return customerRepository.save(newCustomer);
                });*/
    }

    private String serializeMetadata(PredictionRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar metadata: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Determina el resultado de predicción desde el texto del forecast.
     */
    private Boolean determinePredictionResult(String forecast) {
        if (forecast == null) return false;
        String lower = forecast.toLowerCase();
        return lower.startsWith("va a cancelar");
    }

    /**
     * Consulta el historial de predicciones de un cliente.
     */
    @Transactional(readOnly = true)
    public List<Prediction> getCustomerHistory(Long customerId) {
        log.debug("Consultando historial para customer ID: {}", customerId);
        return predictionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Obtiene estadísticas de predicciones en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public Object[] getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo estadísticas entre {} y {}", startDate, endDate);
        return predictionRepository.getStatisticsByDateRange(startDate, endDate);
    }

    public void saveBatch(
            List<PredictionRequestDTO> requests,
            List<MLPredictionFullResponseDTO> mlPredictions,
            String batchId,
            int rowNumber) {

        List<Customer> customers = new ArrayList<>();
        List<Prediction> predictions = new ArrayList<>();
        List<FeatureImportance> allFeatures = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            PredictionRequestDTO request = requests.get(i);
            MLPredictionFullResponseDTO mlPrediction = mlPredictions.get(i);

            Customer customer = customerMapper.toEntity(request);
            customers.add(customer);

            Prediction prediction = predictionMapper.toEntity(mlPrediction);

            prediction.setCustomer(customer);
            predictions.add(prediction);

            if (mlPrediction.featureImportances() != null) {
                for (MLFeatureImportanceDTO mlFeature : mlPrediction.featureImportances()) {

                    FeatureImportance feature = FeatureImportance.builder()
                            .prediction(prediction)
                            .name(mlFeature.featureName())
                            .featureValue(mlFeature.featureValue())
                            .rankPosition(mlFeature.ranking())
                            .impactDirection(featureExplainerService.determineImpact(mlFeature.importanceValue()))
                            .displayName(featureExplainerService.translateFeatureName(mlFeature.featureName()))
                            .build();

                    allFeatures.add(feature);
                }
            }
        }

        customerRepository.saveAll(customers);
        predictionRepository.saveAll(predictions);
        featureImportanceRepository.saveAll(allFeatures);

        List<BatchPredictionResult> batchResults = new ArrayList<>();
        for (int i = 0; i < predictions.size(); i++) {
            BatchPredictionResult result = BatchPredictionResult.builder()
                    .batchId(batchId)
                    .rowNumber(rowNumber + i)
                    .predictionId(predictions.get(i).getId())
                    .isSuccess(true)
                    .build();
            batchResults.add(result);
        }

        batchPredictionResultRepository.saveAll(batchResults);
    }
}
