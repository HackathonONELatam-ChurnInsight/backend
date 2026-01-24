package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO de entrada para POST /api/v1/predict
 */
@Builder
@Schema(name = "PredictionRequest", description = "Datos de entrada para la predicción de churn. Campos opcionales; el modelo intentará predecir con la información disponible.")
public record PredictionRequestDTO(

        // OPCIONAL - Si no se envía, se genera automáticamente
        @Schema(
                description = "Identificador único del cliente en el sistema externo. Si no se proporciona, se generará automáticamente.",
                example = "CUST-12345",
                required = false
        )
        @Size(max = 50, message = "El customerId no puede exceder 50 caracteres")
        String customerId,

        @Schema(description = "País o región del cliente", example = "Spain")
        @NotNull(message = "El campo 'geography' es obligatorio")
        Geography geography,

        @Schema(description = "Género del cliente", example = "Male")
        // campo opcional
        Gender gender,

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
        // campo opcional
        @DecimalMin(value = "0.0", inclusive = true, message = "El salario estimado no puede ser negativo")
        Double estimatedSalary,

        @Schema(description = "Tiempo con la compañía (meses)", example = "6")
        // campo opcional
        @Min(value = 0, message = "La antigüedad (tenure) mínima es 0")
        @Max(value = 20, message = "La antigüedad (tenure) máxima es 20 años")
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
        // campo opcional
        Boolean hasCrCard,

        @Schema(description = "Si ha presentado quejas", example = "false")
        @NotNull(message = "El campo 'complain' es obligatorio")
        Boolean complain

) {}
