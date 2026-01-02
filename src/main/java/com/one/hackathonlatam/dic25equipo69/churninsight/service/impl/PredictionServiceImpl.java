package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de predicción
 */
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;

    public PredictionServiceImpl(ModelClientService modelClientService) {
        this.modelClientService = modelClientService;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        // Llama al cliente del modelo DS (Python)
        //return modelClientService.predict(request);

        //*
        // SIMULACIÓN (MOCK)
        // Simulamos una probabilidad lógica basada en la edad para que parezca real
        String forecast = (request.age() > 50) ? "CHURN" : "NO_CHURN";
        Double probability = (request.age() > 50) ? 0.85 : 0.15;

        return new PredictionResponseDTO(forecast, probability);
        //*/
    }
}
