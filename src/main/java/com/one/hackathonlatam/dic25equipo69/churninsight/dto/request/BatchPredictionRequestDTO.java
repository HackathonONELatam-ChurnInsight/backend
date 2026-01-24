package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import org.springframework.web.multipart.MultipartFile;

/**
 * Request para iniciar un batch de predicciones.
 */
public record BatchPredictionRequestDTO(
        String modelVersion,
        MultipartFile csvFile
) {
    public BatchPredictionRequestDTO {
        if (csvFile == null || csvFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }
}
