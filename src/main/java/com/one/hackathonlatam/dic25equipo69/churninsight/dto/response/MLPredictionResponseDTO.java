package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

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
                        null,
                        this.forecast.equals(1)? "Va a cancelar" : "No va a cancelar",
                        this.probability, // redondear
                        null
                );
        }
}
