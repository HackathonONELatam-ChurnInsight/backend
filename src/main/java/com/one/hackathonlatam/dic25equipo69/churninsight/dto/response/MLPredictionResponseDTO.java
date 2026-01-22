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
                return new PredictionResponseDTO(
                        this.forecast.equals(1)? "Va a cancelar" : "No va a cancelar",
                        BigDecimal.valueOf(this.probability)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue()
                );
        }
}
