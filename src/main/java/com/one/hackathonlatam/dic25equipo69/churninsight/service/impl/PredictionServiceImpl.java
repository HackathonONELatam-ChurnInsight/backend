package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.ModelServiceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;
    private final PredictionPersistenceService predictionPersistenceService;
    private final FeatureExplainerService featureExplainerService;

    @Override
    public PredictionFullResponseDTO predict(PredictionRequestDTO request) {
        log.info("Iniciando predicción para customerId: {}", request.customerId());

        try {
            // 1. Convertir PredictionRequestDTO → MLPredictionRequestDTO
            MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

            // 2. Llamar al modelo ML (siempre intenta obtener features)
            MLPredictionFullResponseDTO mlResponse = modelClientService.predictWithFeatures(mlRequest);

            log.info("Predicción recibida del modelo para customerId: {} - forecast: {}, probability: {}, features: {}",
                    request.customerId(), mlResponse.forecast(), mlResponse.probability(),
                    mlResponse.featureImportances() != null ? mlResponse.featureImportances().size() : 0);

            // 3. Persistir la predicción en BD (con features si existen)
            Prediction savedPrediction = predictionPersistenceService.savePrediction(request, mlResponse);

            log.info("Predicción persistida con ID: {} para customerId: {}",
                    savedPrediction.getId(), request.customerId());

            // 4. Construir respuesta para el frontend
            String forecastText = mlResponse.forecast() == 1 ? "Va a cancelar" : "No va a cancelar";

            // 5. Procesar features si existen (Top 3)
            List<FeatureImportanceResponseDTO> topFeatures = null;
            if (mlResponse.featureImportances() != null && !mlResponse.featureImportances().isEmpty()) {
                topFeatures = mlResponse.featureImportances().stream()
                        .limit(3)  // Top 3 features más importantes
                        .map(mlFeature -> new FeatureImportanceResponseDTO(
                                featureExplainerService.translateFeatureName(mlFeature.featureName()),
                                String.valueOf(mlFeature.featureValue()),
                                featureExplainerService.impactToString(
                                        featureExplainerService.determineImpact(mlFeature.importanceValue())
                                )
                        ))
                        .toList();

                log.info("Features procesadas para customerId: {} - Top {}",
                        request.customerId(), topFeatures.size());
            } else {
                log.info("No se recibieron features del modelo para customerId: {}", request.customerId());
            }

            // 6. Retornar respuesta completa (con o sin features)
            PredictionFullResponseDTO response = new PredictionFullResponseDTO(
                    forecastText,
                    mlResponse.probability(),
                    topFeatures
            );

            log.info("Predicción completada exitosamente para customerId: {}", request.customerId());

            return response;

        } catch (ModelServiceException e) {
            log.error("Error al llamar al modelo ML para customerId: {}", request.customerId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante la predicción para customerId: {}", request.customerId(), e);
            throw new RuntimeException("Error al procesar la predicción: " + e.getMessage(), e);
        }
    }
}
