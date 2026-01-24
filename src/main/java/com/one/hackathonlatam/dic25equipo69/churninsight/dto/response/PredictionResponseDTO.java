package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PredictionResponse", description = "Respuesta con la predicción y su probabilidad")
public record PredictionResponseDTO(
        @Schema(description = "Etiqueta o mensaje de la predicción", example = "Va a cancelar")
        String forecast,

        @Schema(description = "Probabilidad de la predicción (0.0 - 1.0)", example = "0.81")
        Double probability
) {
    public String getRiskLevel() {
        if (probability >= 0.7) return "HIGH";
        if (probability >= 0.4) return "MEDIUM";
        return "LOW";
    }
}
