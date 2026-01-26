package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MLFeatureImportanceDTO(
        @JsonProperty("feature_name")
        String featureName,

        @JsonProperty("feature_value")
        String featureValue,

        @JsonProperty("ranking")
        Integer ranking,

        @JsonProperty("importance_value")
        Double importanceValue
) {}
