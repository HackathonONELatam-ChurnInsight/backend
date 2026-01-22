package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import lombok.Builder;

/**
 * DTO para respuesta con estadísticas de predicciones.
 */
@Builder
public record StatsResponseDTO(

        // Total de predicciones realizadas
        Long totalPredictions,

        // Tasa de churn (0.0 - 1.0)
        Double churnRate,

        // Promedio de probabilidad
        Double avgProbability
) { }
