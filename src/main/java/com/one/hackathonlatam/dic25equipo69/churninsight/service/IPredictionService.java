package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;

public interface IPredictionService {

    /**
     * Realiza predicción básica sin explicabilidad.
     */
    PredictionResponseDTO predict(PredictionRequestDTO request);

    /**
     * Realiza predicción con explicabilidad.
     */
    PredictionFullResponseDTO predictWithExplanation(PredictionRequestDTO request);
}
