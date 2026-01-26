package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PaginationInfo;

import java.util.List;

/**
 * Respuesta con resultados de un batch.
 */
public record BatchResultsResponseDTO(
        String batchId,
        String status,
        List<BatchResultItem> results,
        Integer totalResults,
        Integer successfulCount,
        Integer failedCount,
        PaginationInfo pagination
) {
}
