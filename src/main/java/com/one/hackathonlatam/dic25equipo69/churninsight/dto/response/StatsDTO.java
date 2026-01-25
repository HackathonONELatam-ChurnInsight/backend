package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

public record StatsDTO (
    long totalEvaluated,
    long highRiskCount,
    double churnRate // Porcentaje de riesgo
) {}