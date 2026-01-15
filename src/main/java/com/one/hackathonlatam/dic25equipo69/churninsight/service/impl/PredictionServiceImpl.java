package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
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

        MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

        // Llama al cliente del modelo DS (Python)
        MLPredictionResponseDTO mlResponse = modelClientService.predict(mlRequest);

        PredictionResponseDTO response = mlResponse.toPredictionResponseDTO();

        return response;
    }
}
