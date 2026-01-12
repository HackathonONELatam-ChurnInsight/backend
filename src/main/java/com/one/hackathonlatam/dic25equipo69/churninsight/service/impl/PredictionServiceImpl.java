package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import org.springframework.context.annotation.Profile;
import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de predicción
 */
@Profile("prod")
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;

    public PredictionServiceImpl(ModelClientService modelClientService) {
        this.modelClientService = modelClientService;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        // Llama al cliente del modelo DS (Python)
        return modelClientService.predict(request);
    }
}
