package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO de entrada para POST /api/v1/predict
 * Basado en el contrato de integración versión 3.
 */
public record PredictionRequestDTO(

        @NotNull(message = "El campo 'geography' es obligatorio")
        @Pattern(regexp = "France|Spain|Germany", message = "El campo 'geography' debe ser: France, Spain o Germany")
        String geography,

        @NotNull(message = "El campo 'gender' es obligatorio")
        @Pattern(regexp = "Male|Female", message = "El campo 'gender' debe ser: Male o Female")
        String gender,

        @NotNull(message = "El campo 'age' es obligatorio")
        @Min(value = 18, message = "La edad mínima permitida es 18 años")
        @Max(value = 100, message = "La edad máxima permitida es 100 años")
        Integer age,

        @NotNull(message = "El campo 'creditScore' es obligatorio")
        @Min(value = 100, message = "El creditScore mínimo es 100")
        @Max(value = 1000, message = "El creditScore máximo es 1000")
        Integer creditScore,

        @NotNull(message = "El campo 'balance' es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El balance no puede ser negativo")
        Double balance,

        @NotNull(message = "El campo 'estimatedSalary' es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El salario estimado no puede ser negativo")
        Double estimatedSalary,

        @NotNull(message = "El campo 'tenure' es obligatorio")
        @Min(value = 0, message = "La antigüedad (tenure) mínima es 0")
        @Max(value = 50, message = "La antigüedad (tenure) máxima es 50 años")
        Integer tenure,

        @NotNull(message = "El campo 'numOfProducts' es obligatorio")
        @Min(value = 1, message = "El número mínimo de productos es 1")
        @Max(value = 4, message = "El número máximo de productos es 4")
        Integer numOfProducts,

        @NotNull(message = "El campo 'satisfactionScore' es obligatorio")
        @Min(value = 1, message = "El satisfactionScore mínimo es 1")
        @Max(value = 5, message = "El satisfactionScore máximo es 5")
        Integer satisfactionScore,

        @NotNull(message = "El campo 'isActiveMember' es obligatorio")
        Boolean isActiveMember,

        @NotNull(message = "El campo 'hasCrCard' es obligatorio")
        Boolean hasCrCard,

        @NotNull(message = "El campo 'complain' es obligatorio")
        Boolean complain

) {}
