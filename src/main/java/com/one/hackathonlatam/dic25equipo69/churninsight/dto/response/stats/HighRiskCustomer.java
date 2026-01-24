package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats;

/**
 * Cliente de alto riesgo.
 */
public record HighRiskCustomer(
        Long id,
        String customerId,
        Double probability,
        String geography,
        String riskLevel,
        String predictedAt
) {
    public String getRiskLevel() {
        if (probability >= 0.7) return "HIGH";
        if (probability >= 0.4) return "MEDIUM";
        return "LOW";
    }
}
