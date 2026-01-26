package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para respuesta completa de predicción al cliente.
 * Incluye la predicción básica más las top 3 features más relevantes.
 */
public record PredictionFullResponseDTO(
        @JsonProperty("forecast")
        String forecast,

        @JsonProperty("probability")
        Double probability,

        @JsonProperty("top_features")
        List<FeatureImportanceResponseDTO> topFeatures
) {
        public String getRiskLevel() {
                if (probability >= 0.7) return "HIGH";
                if (probability >= 0.4) return "MEDIUM";
                return "LOW";
        }
}
