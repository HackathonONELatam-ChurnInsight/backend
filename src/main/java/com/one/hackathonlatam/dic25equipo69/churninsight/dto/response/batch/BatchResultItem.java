package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import lombok.Builder;

@Builder
public record BatchResultItem(
        Integer rowNumber,
        PredictionFullResponseDTO prediction,
        Boolean isSuccess,
        String errorMessage
) {
}