package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

/**
 * DTO para respuesta con estadísticas de predicciones.
 */
public record StatsResponseDTO(

        // Total de predicciones realizadas
        Integer totalPredictions,

        // Tasa de churn (0.0 - 1.0)
        Double churnRate,

        // Promedio de probabilidad
        Double avgProbability
) { }
