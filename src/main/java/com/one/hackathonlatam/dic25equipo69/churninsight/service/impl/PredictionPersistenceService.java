package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.DuplicatePredictionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.PredictionPersistenceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de persistencia para predicciones de churn.
 * Gestiona el almacenamiento de predicciones en base de datos con validación de duplicados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionPersistenceService {

    private final PredictionRepository predictionRepository;
    private final PredictionMapper predictionMapper;
    private final FeatureExplainerService featureExplainerService;
    private final ObjectMapper objectMapper;

    /**
     * Persiste una predicción completa con features opcionales.
     * Valida que no existan predicciones duplicadas (mismo customerId + misma metadata).
     *
     * @param request Datos del cliente enviados en el request
     * @param mlResponse Respuesta del modelo ML con predicción y features
     * @return Prediction guardada en BD con su ID asignado
     * @throws DuplicatePredictionException Si ya existe una predicción idéntica
     * @throws PredictionPersistenceException Si falla la persistencia por otros motivos
     */
    @Transactional
    public Prediction savePrediction(PredictionRequestDTO request, MLPredictionFullResponseDTO mlResponse) {
        try {
            log.info("Iniciando persistencia de predicción para customerId: {}", request.customerId());

            // 1. Serializar customerMetadata (snapshot JSON del request)
            String customerMetadataJson = serializeCustomerMetadata(request);

            // 2. Calcular hash SHA-256 de la metadata para detección de duplicados
            String metadataHash = calculateMetadataHash(customerMetadataJson);

            // 3. Verificar si ya existe predicción duplicada exacta
            Optional<Prediction> existingPrediction = predictionRepository
                    .findByCustomerIdAndMetadataHash(request.customerId(), metadataHash);

            if (existingPrediction.isPresent()) {
                log.warn("Predicción duplicada detectada para customerId: {} (Prediction ID: {})",
                        request.customerId(), existingPrediction.get().getId());
                throw new DuplicatePredictionException(
                        request.customerId(),
                        existingPrediction.get().getId()
                );
            }

            // 4. Convertir forecast (0/1) a Boolean
            Boolean predictionResult = mlResponse.forecast() == 1;

            // 5. Convertir probability (Double) a BigDecimal
            BigDecimal probability = BigDecimal.valueOf(mlResponse.probability());

            // 6. Crear entidad Prediction usando mapper
            Prediction prediction = predictionMapper.toEntity(
                    request.customerId(),
                    predictionResult,
                    probability
            );

            // 7. Asignar metadata JSON (esto también calcula y asigna el hash automáticamente)
            prediction.setCustomerMetadata(customerMetadataJson);

            // 8. Procesar features si existen en la respuesta del modelo
            if (mlResponse.featureImportances() != null && !mlResponse.featureImportances().isEmpty()) {
                List<FeatureImportance> features = processFeatureImportances(
                        prediction,
                        mlResponse.featureImportances()
                );
                prediction.setFeatureImportances(features);
            }

            // 9. Guardar en BD
            Prediction savedPrediction = predictionRepository.save(prediction);
            log.info("Predicción guardada exitosamente con ID: {} para customerId: {}",
                    savedPrediction.getId(), request.customerId());

            return savedPrediction;

        } catch (DuplicatePredictionException e) {
            // Re-lanzar excepción de duplicado sin envolverla
            throw e;
        } catch (DataIntegrityViolationException e) {
            // Constraint de BD violado (plan B si la verificación previa falla por concurrencia)
            log.error("Violación de constraint único para customerId: {}", request.customerId(), e);
            throw new DuplicatePredictionException(request.customerId(), null);
        } catch (Exception e) {
            log.error("Error al persistir predicción para customerId: {}", request.customerId(), e);
            throw new PredictionPersistenceException(
                    "Error al guardar la predicción: " + e.getMessage(), e
            );
        }
    }

    /**
     * Serializa el request completo a JSON para guardar como metadata.
     * Esto permite análisis histórico y auditoría sin necesidad de tabla Customer separada.
     *
     * @param request DTO con todos los datos del cliente
     * @return String JSON serializado del request
     * @throws PredictionPersistenceException Si falla la serialización JSON
     */
    private String serializeCustomerMetadata(PredictionRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar customerMetadata para customerId: {}", request.customerId(), e);
            throw new PredictionPersistenceException(
                    "Error al serializar metadata del cliente", e
            );
        }
    }

    /**
     * Calcula el hash SHA-256 en Base64 de la metadata del cliente.
     * Este hash se usa para detectar predicciones duplicadas de forma eficiente.
     * Cualquier cambio mínimo en los datos genera un hash completamente diferente.
     *
     * @param metadata String JSON con los datos del cliente
     * @return Hash SHA-256 en formato Base64 (64 caracteres)
     * @throws IllegalArgumentException Si metadata es null o vacío
     * @throws RuntimeException Si el algoritmo SHA-256 no está disponible
     */
    private String calculateMetadataHash(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            throw new IllegalArgumentException("customerMetadata no puede ser null o vacío");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(metadata.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular metadata hash: SHA-256 no disponible", e);
        }
    }

    /**
     * Procesa y enriquece las feature importances recibidas del modelo ML.
     * Traduce nombres técnicos a nombres legibles y determina el impacto de cada variable.
     *
     * @param prediction Entidad Prediction a la que se asociarán las features
     * @param mlFeatures Lista de features retornadas por el modelo ML
     * @return Lista de FeatureImportance enriquecidas y listas para persistir
     */
    private List<FeatureImportance> processFeatureImportances(
            Prediction prediction,
            List<MLFeatureImportanceDTO> mlFeatures) {

        List<FeatureImportance> features = new ArrayList<>();
        for (MLFeatureImportanceDTO mlFeature : mlFeatures) {
            FeatureImportance feature = FeatureImportance.builder()
                    .prediction(prediction)
                    .name(mlFeature.featureName())
                    .featureValue(String.valueOf(mlFeature.featureValue()))
                    .rankPosition(mlFeature.ranking())
                    .impactDirection(featureExplainerService.determineImpact(mlFeature.importanceValue()))
                    .displayName(featureExplainerService.translateFeatureName(mlFeature.featureName()))
                    .build();
            features.add(feature);
        }
        return features;
    }
}
