package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;

import java.time.LocalDateTime;

/**
 * Respuesta de estado de un batch en proceso.
 */
public record BatchStatusResponseDTO(
        String batchId,
        String status,
        String filename,
        Integer totalRecords,
        Integer processedRecords,
        Integer successfulPredictions,
        Integer failedPredictions,
        Double progressPercentage,
        Long durationSeconds,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endTime,

        String errorMessage,
        String resultsUrl
) {
    public static BatchStatusResponseDTO from(BatchPredictionJob job) {
        return new BatchStatusResponseDTO(
                job.getId(),
                job.getStatus().name(),
                job.getFilename(),
                job.getTotalRecords(),
                job.getProcessedRecords(),
                job.getSuccessfulPredictions(),
                job.getFailedPredictions(),
                job.getProgressPercentage(),
                job.getDurationSeconds(),
                job.getStartTime(),
                job.getEndTime(),
                job.getErrorMessage(),
                "/api/v1/predict/batch/" + job.getId() + "/results"
        );
    }
}
