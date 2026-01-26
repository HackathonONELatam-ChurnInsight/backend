package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MLBatchPredictionRequestDTO(

        @JsonProperty("modelVersion")
        String modelVersion,

        @JsonProperty("customers")
        List<MLPredictionRequestDTO> customers
) {
}
