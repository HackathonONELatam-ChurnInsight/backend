package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;

import java.util.List;

public record MLBatchPredictionResponseDTO(

        @JsonProperty("results")
        List<MLPredictionFullResponseDTO> predictions
) {
}
