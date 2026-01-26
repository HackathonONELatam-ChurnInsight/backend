package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Información del período analizado.
 */
public record PeriodInfo(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endDate,

        String periodDescription
) {
}
