package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para recibir respuesta completa del servicio de ML en Python.
 * Incluye la predicción básica más las feature importances para explicabilidad.
 */
public record MLPredictionFullResponseDTO(
        @JsonProperty("forecast")
        Integer forecast,

        @JsonProperty("probability")
        Double probability,

        @JsonProperty("feature_importances")
        List<MLFeatureImportanceDTO> featureImportances
) {}
