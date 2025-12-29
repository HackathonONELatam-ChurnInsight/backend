package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PredictionRequest", description = "Datos de entrada para la predicción de churn. Campos opcionales; el modelo intentará predecir con la información disponible.")
public record PredictionRequestDTO(
    @Schema(description = "País o región del cliente", example = "Spain")
    String geography,

    @Schema(description = "Género del cliente", example = "Male")
    String gender,

    @Schema(description = "Edad del cliente (entero)", example = "42")
    Integer age,

    @Schema(description = "Puntaje de crédito (entero)", example = "650")
    Integer creditScore,

    @Schema(description = "Balance de la cuenta", example = "14.5")
    Double balance,

    @Schema(description = "Salario estimado", example = "14.0")
    Double estimatedSalary,

    @Schema(description = "Tiempo con la compañía (meses)", example = "6")
    Integer tenure,

    @Schema(description = "Número de productos contratados", example = "5")
    Integer numOfProducts,

    @Schema(description = "Puntuación de satisfacción (ej.: 1-5)", example = "2")
    Integer satisfactionScore,

    @Schema(description = "Si es miembro activo", example = "true")
    Boolean isActiveMember,

    @Schema(description = "Si tiene tarjeta de crédito", example = "true")
    Boolean hasCrCard,

    @Schema(description = "Si ha presentado quejas", example = "false")
    Boolean complain
) {}
