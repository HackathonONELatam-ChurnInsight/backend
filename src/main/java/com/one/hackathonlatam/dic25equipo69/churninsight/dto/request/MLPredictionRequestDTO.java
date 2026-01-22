package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;

/**
 * DTO para enviar datos al servicio de ML en Python.
 * Mapea exactamente al contrato esperado por el modelo.
 */
public record MLPredictionRequestDTO(
        @JsonProperty("Geography")
        Geography geography,

        @JsonProperty("Gender")
        Gender gender,

        @JsonProperty("Age")
        Integer age,

        @JsonProperty("CreditScore")
        Integer creditScore,

        @JsonProperty("Balance")
        Double balance,

        @JsonProperty("EstimatedSalary")
        Double estimatedSalary,

        @JsonProperty("Tenure")
        Integer tenure,

        @JsonProperty("NumOfProducts")
        Integer numOfProducts,

        @JsonProperty("SatisfactionScore")
        Integer satisfactionScore,

        @JsonProperty("IsActiveMember")
        Integer isActiveMember,

        @JsonProperty("HasCrCard")
        Integer hasCrCard,

        @JsonProperty("Complain")
        Integer complain
) {
    public static MLPredictionRequestDTO from(PredictionRequestDTO predictionRequestDTO) {
        return new MLPredictionRequestDTO(
                predictionRequestDTO.geography(),
                predictionRequestDTO.gender(),
                predictionRequestDTO.age(),
                predictionRequestDTO.creditScore(),
                predictionRequestDTO.balance(),
                predictionRequestDTO.estimatedSalary(),
                predictionRequestDTO.tenure(),
                predictionRequestDTO.numOfProducts(),
                predictionRequestDTO.satisfactionScore(),
                booleanToInteger(predictionRequestDTO.isActiveMember()),
                booleanToInteger(predictionRequestDTO.hasCrCard()),
                booleanToInteger(predictionRequestDTO.complain())
        );
    }

    private static Integer booleanToInteger(Boolean value) {
        if (value == null) return null;
        return value ? 1 : 0;
    }
}
