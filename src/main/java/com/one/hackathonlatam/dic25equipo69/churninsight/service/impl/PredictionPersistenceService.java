package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.PredictionPersistenceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
     *
     * @param request Datos del cliente enviados en el request
     * @param mlResponse Respuesta del modelo ML
     * @return Prediction guardada en BD
     * @throws PredictionPersistenceException Si falla la persistencia
     */
    @Transactional
    public Prediction savePrediction(PredictionRequestDTO request, MLPredictionFullResponseDTO mlResponse) {
        try {
            log.info("Iniciando persistencia de predicción para customerId: {}", request.customerId());

            // 1. Convertir forecast (0/1) a Boolean
            Boolean predictionResult = mlResponse.forecast() == 1;

            // 2. Convertir probability (Double) a BigDecimal
            BigDecimal probability = BigDecimal.valueOf(mlResponse.probability());

            // 3. Crear entidad Prediction usando mapper
            Prediction prediction = predictionMapper.toEntity(
                    request.customerId(),
                    predictionResult,
                    probability
            );

            // 4. Serializar customerMetadata (snapshot JSON del request)
            String customerMetadataJson = serializeCustomerMetadata(request);
            prediction.setCustomerMetadata(customerMetadataJson);

            // 5. Procesar features si existen en la respuesta
            if (mlResponse.featureImportances() != null && !mlResponse.featureImportances().isEmpty()) {
                List<FeatureImportance> features = processFeatureImportances(
                        prediction,
                        mlResponse.featureImportances()
                );
                prediction.setFeatureImportances(features);
            }

            // 6. Guardar en BD
            Prediction savedPrediction = predictionRepository.save(prediction);

            log.info("Predicción guardada exitosamente con ID: {} para customerId: {}",
                    savedPrediction.getId(), request.customerId());

            return savedPrediction;

        } catch (Exception e) {
            log.error("Error al persistir predicción para customerId: {}", request.customerId(), e);
            throw new PredictionPersistenceException(
                    "Error al guardar la predicción: " + e.getMessage(), e
            );
        }
    }

    /**
     * Serializa el request completo a JSON para guardar como metadata.
     * Esto permite análisis histórico sin necesidad de tabla Customer.
     */
    private String serializeCustomerMetadata(PredictionRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Error al serializar customerMetadata para customerId: {}. Guardando null.",
                    request.customerId(), e);
            return null;
        }
    }

    /**
     * Procesa y enriquece las feature importances del modelo ML.
     * Traduce nombres técnicos a nombres legibles y determina el impacto.
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
