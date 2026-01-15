package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementación mock del servicio de predicción para desarrollo.
 * Retorna predicciones simuladas y las persiste en H2.
 */
@Slf4j
@Profile("dev")
@Service
public class PredictionServiceMockImpl implements IPredictionService {

    private final PredictionPersistenceService persistenceService;

    public PredictionServiceMockImpl(PredictionPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        log.debug("Usando predicción MOCK para desarrollo");

        // Predicción simulada basada en satisfactionScore
        boolean willChurn = request.satisfactionScore() < 3;
        double probability = willChurn ? 0.85 : 0.25;
        String forecast = willChurn ? "Va a cancelar" : "No va a cancelar";

        PredictionResponseDTO response = new PredictionResponseDTO(forecast, probability);

        // Persistir predicción mock
        Prediction savedPrediction = persistenceService.savePrediction(request, response);
        log.info("Predicción MOCK persistida con ID={}", savedPrediction.getId());

        return response;
    }
}
