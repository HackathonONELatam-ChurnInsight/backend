package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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

    public PredictionServiceImpl(
            ModelClientService modelClientService,
            PredictionPersistenceService persistenceService) {
        this.modelClientService = modelClientService;
        this.persistenceService = persistenceService;
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

            // 4. Persistir predicción automáticamente
            Prediction savedPrediction = persistenceService.savePrediction(request, response);
            log.info("Predicción persistida exitosamente con ID={}", savedPrediction.getId());

            return response;

        } catch (Exception e) {
            log.error("Error al procesar predicción: {}", e.getMessage(), e);
            throw e;
        }
    }
}
