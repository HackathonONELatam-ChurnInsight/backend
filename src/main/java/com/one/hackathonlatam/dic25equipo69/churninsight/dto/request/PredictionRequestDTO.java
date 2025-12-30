package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO de entrada para POST /api/v1/predict
 * Basado en el contrato de integración versión 3.
 */
public record PredictionRequestDTO(

        @NotNull(message = "geography es obligatorio")
        @NotBlank(message = "geography no puede estar vacío")
        String geography,

        @NotNull(message = "gender es obligatorio")
        @Pattern(regexp = "Male|Female", message = "gender debe ser 'Male' o 'Female'")
        String gender,

        @NotNull(message = "age es obligatorio")
        @Min(value = 0, message = "age debe ser >= 0")
        @Max(value = 120, message = "age debe ser <= 120")
        Integer age,

        @NotNull(message = "creditScore es obligatorio")
        @Min(value = 0, message = "creditScore debe ser >= 0")
        @Max(value = 1000, message = "creditScore debe ser <= 1000")
        Integer creditScore,

        @NotNull(message = "balance es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "balance debe ser >= 0")
        Double balance,

        @NotNull(message = "estimatedSalary es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "estimatedSalary debe ser >= 0")
        Double estimatedSalary,

        @NotNull(message = "tenure es obligatorio")
        @Min(value = 0, message = "tenure debe ser >= 0")
        @Max(value = 50, message = "tenure debe ser <= 50")
        Integer tenure,

        @NotNull(message = "numOfProducts es obligatorio")
        @Min(value = 0, message = "numOfProducts debe ser >= 0")
        @Max(value = 5, message = "numOfProducts debe ser <= 5")
        Integer numOfProducts,

        @NotNull(message = "satisfactionScore es obligatorio")
        @Min(value = 0, message = "satisfactionScore debe ser >= 0")
        @Max(value = 5, message = "satisfactionScore debe ser <= 5")
        Integer satisfactionScore,

        @NotNull(message = "isActiveMember es obligatorio")
        Boolean isActiveMember,

        @NotNull(message = "hasCrCard es obligatorio")
        Boolean hasCrCard,

        @NotNull(message = "complain es obligatorio")
        Boolean complain

) {}
