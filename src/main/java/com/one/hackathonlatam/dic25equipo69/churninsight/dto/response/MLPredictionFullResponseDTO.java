package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta del modelo ML con predicción y features opcionales")
public record MLPredictionFullResponseDTO(

        @Schema(description = "Resultado de la predicción (0 = no cancelará, 1 = cancelará)",
                example = "1",
                required = true)
        Integer forecast,

        @Schema(description = "Probabilidad de cancelación (0.0 - 1.0)",
                example = "0.85",
                required = true)
        Double probability,

        @JsonProperty("feature_importances")
        @Schema(description = "Características más importantes (null si no hay explicabilidad)",
                nullable = true)
        List<MLFeatureImportanceDTO> featureImportances
) {
}
