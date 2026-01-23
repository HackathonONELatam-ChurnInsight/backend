package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO de entrada para POST /api/v1/predict
 * Basado en el contrato de integración versión 3.
 */
@Schema(name = "PredictionRequest", description = "Datos de entrada para la predicción de churn. Campos opcionales; el modelo intentará predecir con la información disponible.")
public record PredictionRequestDTO(

        @NotNull(message = "El customer_id es obligatorio")
        @NotBlank(message = "El customer_id no puede estar vacío")
        @Size(max = 50, message = "El customer_id no puede exceder 50 caracteres")
        @Schema(description = "Identificador único del cliente en el sistema origen",
                example = "CLI-12345",
                required = true)
        String customerId,

        @NotNull(message = "El campo 'geography' es obligatorio")
        @Schema(description = "País del cliente", example = "Spain", required = true)
        Geography geography,

        @Schema(description = "Género del cliente", example = "Male")
        Gender gender,

        @NotNull(message = "El campo 'age' es obligatorio")
        @Min(value = 18, message = "La edad mínima permitida es 18 años")
        @Max(value = 100, message = "La edad máxima permitida es 100 años")
        @Schema(description = "Edad del cliente", example = "42", required = true)
        Integer age,

        @NotNull(message = "El campo 'creditScore' es obligatorio")
        @Min(value = 100, message = "El creditScore mínimo es 100")
        @Max(value = 1000, message = "El creditScore máximo es 1000")
        @Schema(description = "Puntuación de crédito del cliente", example = "650", required = true)
        Integer creditScore,

        @NotNull(message = "El campo 'balance' es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El balance no puede ser negativo")
        @Schema(description = "Balance de la cuenta del cliente", example = "14.5", required = true)
        Double balance,  // ⭐ DOUBLE como en el contrato

        @DecimalMin(value = "0.0", inclusive = true, message = "El salario estimado no puede ser negativo")
        @Schema(description = "Salario estimado del cliente", example = "14.0")
        Double estimatedSalary,  // ⭐ DOUBLE como en el contrato

        @Min(value = 0, message = "La antigüedad (tenure) mínima es 0")
        @Max(value = 20, message = "La antigüedad (tenure) máxima es 20 años")
        @Schema(description = "Antigüedad del cliente en años", example = "6")
        Integer tenure,

        @NotNull(message = "El campo 'numOfProducts' es obligatorio")
        @Min(value = 1, message = "El número mínimo de productos es 1")
        @Max(value = 4, message = "El número máximo de productos es 4")
        @Schema(description = "Número de productos que tiene el cliente", example = "5", required = true)
        Integer numOfProducts,

        @NotNull(message = "El campo 'satisfactionScore' es obligatorio")
        @Min(value = 1, message = "El satisfactionScore mínimo es 1")
        @Max(value = 5, message = "El satisfactionScore máximo es 5")
        @Schema(description = "Nivel de satisfacción del cliente (1-5)", example = "2", required = true)
        Integer satisfactionScore,

        @NotNull(message = "El campo 'isActiveMember' es obligatorio")
        @Schema(description = "Indica si el cliente es miembro activo", example = "true", required = true)
        Boolean isActiveMember,

        @Schema(description = "Indica si el cliente tiene tarjeta de crédito", example = "true")
        Boolean hasCrCard,

        @NotNull(message = "El campo 'complain' es obligatorio")
        @Schema(description = "Indica si el cliente ha presentado quejas", example = "false", required = true)
        Boolean complain
) {}
