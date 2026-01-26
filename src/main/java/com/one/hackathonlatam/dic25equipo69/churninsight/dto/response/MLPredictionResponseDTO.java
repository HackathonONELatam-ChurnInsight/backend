package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para recibir respuesta del servicio de ML en Python.
 */
public record MLPredictionResponseDTO(
        @JsonProperty("forecast")
        Integer forecast,

        @JsonProperty("probability")
        Double probability
) {
        public PredictionResponseDTO toPredictionResponseDTO() {
                // Corrección: Se envían exactamente 4 argumentos
                return new PredictionResponseDTO(
                        null, // ID (se genera luego)
                        (this.forecast != null && this.forecast == 1) ? "Va a cancelar" : "No va a cancelar",
                        // Probabilidad redondeada
                        BigDecimal.valueOf(this.probability)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue(),
                        null // Fecha (se genera luego)
                );
        }
}
