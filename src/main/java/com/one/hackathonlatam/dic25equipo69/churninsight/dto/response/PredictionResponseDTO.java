package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PredictionResponse", description = "Respuesta con la predicción y su probabilidad")
public record PredictionResponseDTO(

    @Schema(description = "ID único del cliente", example = "a1b2-c3d4...")
    String clientId,

    @Schema(description = "Etiqueta o mensaje de la predicción", example = "Va a cancelar")
    String forecast,

    @Schema(description = "Probabilidad de la predicción (0.0 - 1.0)", example = "0.81")
    Double probability,

    @Schema(description = "Fecha de análisis", example = "2026-01-23T10:00:00")
    String timestamp
) {}