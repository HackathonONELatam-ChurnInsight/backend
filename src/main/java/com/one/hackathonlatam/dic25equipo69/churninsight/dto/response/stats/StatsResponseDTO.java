package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para respuesta con estadísticas de predicciones.
 */
@Builder
public record StatsResponseDTO(
        Long totalPredictions,
        Double churnRate,
        Double avgProbability,
        PeriodInfo period,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
    public StatsResponseDTO(
            Long totalPredictions,
            Double churnRate,
            Double avgProbability,
            PeriodInfo period) {
        this(totalPredictions, Math.round(churnRate * 100) / 100.0, Math.round(avgProbability * 100) / 100.0, period, LocalDateTime.now());
    }
}

