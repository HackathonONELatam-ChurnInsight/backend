package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Respuesta inmediata al iniciar un batch.
 */
@Builder
public record BatchPredictionResponseDTO(
        String batchId,
        String status,
        Integer totalRecords,
        String statusUrl,
        String resultsUrl,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
}
