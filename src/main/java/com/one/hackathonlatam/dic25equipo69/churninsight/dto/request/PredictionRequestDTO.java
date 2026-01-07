package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO de entrada para POST /api/v1/predict
 * Basado en el contrato de integración versión 3.
 */
@Schema(name = "PredictionRequest", description = "Datos de entrada para la predicción de churn. Campos opcionales; el modelo intentará predecir con la información disponible.")
public record PredictionRequestDTO(

        @Schema(description = "País o región del cliente", example = "Spain")
        @NotNull(message = "El campo 'geography' es obligatorio")
        @Pattern(regexp = "France|Spain|Germany", message = "El campo 'geography' debe ser: France, Spain o Germany")
        String geography,

        @Schema(description = "Género del cliente", example = "Male")
        @NotNull(message = "El campo 'gender' es obligatorio")
        @Pattern(regexp = "Male|Female", message = "El campo 'gender' debe ser: Male o Female")
        String gender,

        @Schema(description = "Edad del cliente (entero)", example = "42")
        @NotNull(message = "El campo 'age' es obligatorio")
        @Min(value = 18, message = "La edad mínima permitida es 18 años")
        @Max(value = 100, message = "La edad máxima permitida es 100 años")
        Integer age,

        @Schema(description = "Puntaje de crédito (entero)", example = "650")
        @NotNull(message = "El campo 'creditScore' es obligatorio")
        @Min(value = 100, message = "El creditScore mínimo es 100")
        @Max(value = 1000, message = "El creditScore máximo es 1000")
        Integer creditScore,

        @Schema(description = "Balance de la cuenta", example = "14.5")
        @NotNull(message = "El campo 'balance' es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El balance no puede ser negativo")
        Double balance,

        @Schema(description = "Salario estimado", example = "14.0")
        @NotNull(message = "El campo 'estimatedSalary' es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El salario estimado no puede ser negativo")
        Double estimatedSalary,

        @Schema(description = "Tiempo con la compañía (meses)", example = "6")
        @NotNull(message = "El campo 'tenure' es obligatorio")
        @Min(value = 0, message = "La antigüedad (tenure) mínima es 0")
        @Max(value = 50, message = "La antigüedad (tenure) máxima es 50 años")
        Integer tenure,

        @Schema(description = "Número de productos contratados", example = "5")
        @NotNull(message = "El campo 'numOfProducts' es obligatorio")
        @Min(value = 1, message = "El número mínimo de productos es 1")
        @Max(value = 4, message = "El número máximo de productos es 4")
        Integer numOfProducts,

        @Schema(description = "Puntuación de satisfacción (ej.: 1-5)", example = "2")
        @NotNull(message = "El campo 'satisfactionScore' es obligatorio")
        @Min(value = 1, message = "El satisfactionScore mínimo es 1")
        @Max(value = 5, message = "El satisfactionScore máximo es 5")
        Integer satisfactionScore,

        @Schema(description = "Si es miembro activo", example = "true")
        @NotNull(message = "El campo 'isActiveMember' es obligatorio")
        Boolean isActiveMember,

        @Schema(description = "Si tiene tarjeta de crédito", example = "true")
        @NotNull(message = "El campo 'hasCrCard' es obligatorio")
        Boolean hasCrCard,

        @Schema(description = "Si ha presentado quejas", example = "false")
        @NotNull(message = "El campo 'complain' es obligatorio")
        Boolean complain

) {}
