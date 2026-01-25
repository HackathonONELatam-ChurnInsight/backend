package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;

import java.util.List;

public interface IPredictionService {
    PredictionResponseDTO predict(PredictionRequestDTO request);

    // Métodos para el Dashboard
    List<PredictionResponseDTO> getRecentHistory();
    StatsDTO getStats();
}
