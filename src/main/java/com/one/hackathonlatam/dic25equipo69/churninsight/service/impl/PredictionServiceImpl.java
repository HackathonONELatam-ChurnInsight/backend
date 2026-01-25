package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import org.springframework.context.annotation.Profile;
import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.FeatureExtractionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.FeatureImportanceRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de predicción para producción.
 * Llama al modelo ML y persiste automáticamente cada predicción.
 */
@Slf4j
@Profile("prod")
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;
    private final PredictionPersistenceService persistenceService;
    private final FeatureImportanceRepository featureImportanceRepository;
    private final FeatureExplainerService featureExplainerService;
    private final PredictionRepository predictionRepository;
    private final CustomerRepository customerRepository;

    public PredictionServiceImpl(
            ModelClientService modelClientService,
            PredictionPersistenceService persistenceService,
            FeatureImportanceRepository featureImportanceRepository,
            FeatureExplainerService featureExplainerService,
            PredictionRepository predictionRepository,
            CustomerRepository customerRepository) {


        this.modelClientService = modelClientService;
        this.persistenceService = persistenceService;
        this.featureImportanceRepository = featureImportanceRepository;
        this.featureExplainerService = featureExplainerService;
        this.predictionRepository = predictionRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        log.info("Iniciando predicción para cliente con geography={}, age={}",
                request.geography(), request.age());
        try {
            // 1. Convertir a formato del modelo ML
            MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

            // 2. Llamar al modelo DS (Python)
            MLPredictionResponseDTO mlResponse = modelClientService.predict(mlRequest);

            // 3. Convertir respuesta del modelo a formato API
            PredictionResponseDTO response = mlResponse.toPredictionResponseDTO();
        PredictionResponseDTO responseDTO = mlResponse.toPredictionResponseDTO();

        // 2. Guardar Cliente en BD (Mapeo manual simple)
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());

        customer.setGeography(request.geography());
        customer.setGender(request.gender());
        customer.setAge(request.age());
        customer.setCreditScore(request.creditScore());
        customer.setBalance(BigDecimal.valueOf(request.balance())); // Conversión

        if (request.estimatedSalary() != null) {
            customer.setEstimatedSalary(BigDecimal.valueOf(request.estimatedSalary()));
        }

        customer.setTenure(request.tenure());
        customer.setNumOfProducts(request.numOfProducts());
        customer.setSatisfactionScore(request.satisfactionScore());
        customer.setIsActiveMember(request.isActiveMember());
        customer.setHasCrCard(request.hasCrCard());
        customer.setComplain(request.complain());

        customer = customerRepository.save(customer); // Guardamos y obtenemos el ID generado

        // 3. Guardar Predicción en BD
        Prediction prediction = new Prediction();
        prediction.setPredictionResult(responseDTO.forecast());
        prediction.setProbability(responseDTO.probability());
        prediction.setCustomer(customer); // Relación

        predictionRepository.save(prediction);

            // RETORNO ACTUALIZADO CON DATOS REALES
            return new PredictionResponseDTO(
                    customer.getCustomerId(), // ID Real (UUID)
                    responseDTO.forecast(),
                    responseDTO.probability(),
                    prediction.getCreatedAt() != null ? prediction.getCreatedAt().toString() : LocalDateTime.now().toString()
            );
            // 4. Persistir predicción automáticamente
            Prediction savedPrediction = persistenceService.saveMlPrediction(request, mlResponse);
            log.info("Predicción persistida exitosamente con ID={}", savedPrediction.getId());

            return response;

        } catch (Exception e) {
            log.error("Error al procesar predicción: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Realiza predicción de churn con explicabilidad (top 3 features más influyentes).
     * Llama al modelo ML, persiste la predicción con features y retorna las 3 variables
     * que más impactaron en la decisión del modelo.
     *
     * @param request datos del cliente para realizar la predicción
     * @return predicción completa con forecast, probabilidad y top 3 features
     * @throws FeatureExtractionException si las features están vacías o son inválidas
     */
    @Override
    public PredictionFullResponseDTO predictWithExplanation(PredictionRequestDTO request){
        log.info("Iniciando predicción con explicabilidad para cliente con geography={}, age={}",
                request.geography(), request.age());
        try {
            // 1. Convertir a formato del modelo ML
            MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

            // 2. Llamar al modelo DS con feature importances
            MLPredictionFullResponseDTO mlFullResponse = modelClientService.predictWithFeatures(mlRequest);

            // 3. Persistir predicción con features
            Prediction savedPrediction = persistenceService.saveMlPredictionWithFeatures(request, mlFullResponse);
            log.info("Predicción con explicabilidad persistida exitosamente con ID={}", savedPrediction.getId());

            // 4. Obtener top 3 features desde BD y construir respuesta
            PredictionFullResponseDTO response = buildFullResponse(savedPrediction);

            log.info("Predicción completa generada: forecast={}, probability={}, features={}",
                    response.forecast(), response.probability(), response.topFeatures().size());

            return response;

        } catch (Exception e) {
            log.error("Error al procesar predicción con explicabilidad: {}", e.getMessage(), e);
            throw e;
        }

    }
    @Override
    public List<PredictionResponseDTO> getRecentHistory() {
        return predictionRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(p -> new PredictionResponseDTO(
                        p.getCustomer().getCustomerId(), // Sacamos el ID de la relación con Customer
                        p.getPredictionResult(),
                        p.getProbability(),
                        p.getCreatedAt().toString() // Fecha real de la BD
                ))
                .collect(Collectors.toList());
    }

    @Override
    public StatsDTO getStats() { // Cambiado a StatsDTO
        long total = predictionRepository.count();
        long riskCount = predictionRepository.countChurnRisks();
        double rate = total > 0 ? (double) riskCount / total : 0.0;

        return new StatsDTO(total, riskCount, rate);
    }

    /**
     * Construye la respuesta completa con top 3 features desde la predicción guardada.
     *
     * @throws FeatureExtractionException si no se pueden obtener las features o están vacías
     */
    public PredictionFullResponseDTO buildFullResponse(Prediction savedPrediction) {
        try {
            // 1. Obtener top 3 features desde la BD
            List<FeatureImportance> topFeatures = featureImportanceRepository.findTopNByPredictionId(
                    savedPrediction.getId(), 3);

            // Validar que se obtuvieron features
            if (topFeatures == null || topFeatures.isEmpty()) {
                throw new FeatureExtractionException(
                        "No se encontraron feature importances para la predicción ID: " + savedPrediction.getId());
            }

            log.debug("Obtenidas {} features para predicción ID={}", topFeatures.size(), savedPrediction.getId());

            // 2. Convertir a DTOs de respuesta
            List<FeatureImportanceResponseDTO> topFeaturesDTO = topFeatures.stream()
                    .map(feature -> new FeatureImportanceResponseDTO(
                            feature.getDisplayName(),
                            feature.getFeatureValue(),
                            featureExplainerService.impactToString(feature.getImpactDirection())
                    ))
                    .collect(Collectors.toList());

            // 3. Construir forecast legible
            String forecast = savedPrediction.getPredictionResult() ? "Va a cancelar" : "No va a cancelar";

            // 4. Retornar respuesta completa
            return new PredictionFullResponseDTO(
                    forecast,
                    savedPrediction.getProbability().doubleValue(),
                    topFeaturesDTO
            );

        } catch (FeatureExtractionException e) {
            throw e; // Re-lanzar excepciones de negocio
        } catch (Exception e) {
            log.error("Error inesperado al construir respuesta completa: {}", e.getMessage(), e);
            throw new FeatureExtractionException("Error al construir respuesta con explicabilidad", e);
        }
    }

}