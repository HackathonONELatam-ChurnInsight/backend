package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;

import java.util.List;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;

public interface IPredictionService {

    /**
     * Realiza predicción básica sin explicabilidad.
     */
    PredictionResponseDTO predict(PredictionRequestDTO request);

    /**
     * Realiza predicción con explicabilidad.
     */
    PredictionFullResponseDTO predictWithExplanation(PredictionRequestDTO request);

    PredictionFullResponseDTO buildFullResponse(Prediction savedPrediction);

    // Métodos para el Dashboard
    List<PredictionResponseDTO> getRecentHistory();
    StatsDTO getStats();
}
