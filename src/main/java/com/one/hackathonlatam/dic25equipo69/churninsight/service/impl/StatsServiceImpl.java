package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IStatsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements IStatsService {

    private final PredictionRepository predictionRepository;

    @Override
    public StatsResponseDTO getStats() {

        long totalPredictions = predictionRepository.count();
        Double churnRate = calculatePredictionResultRate(true, totalPredictions);
        Double avgProbability = calculateAvgProbability();

        return StatsResponseDTO.builder()
                .totalPredictions(totalPredictions)
                .churnRate(churnRate)
                .avgProbability(avgProbability)
                .build();
    }

    private Double calculatePredictionResultRate(boolean result, long totalPredictions) {
        Long resultCount = predictionRepository.countByPredictionResult(result);
        return totalPredictions > 0 ? (double) resultCount / totalPredictions : 0.0;
    }

    private Double calculateAvgProbability() {
        return Optional.ofNullable(predictionRepository.findAverageProbability())
                .orElse(0.0);
    }
}
