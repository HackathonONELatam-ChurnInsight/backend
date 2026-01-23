package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

/**
 * Excepción lanzada cuando se intenta guardar una predicción duplicada
 * (mismo customerId + misma metadata).
 */
public class DuplicatePredictionException extends RuntimeException {

    private final String customerId;
    private final Long existingPredictionId;

    public DuplicatePredictionException(String customerId, Long existingPredictionId) {
        super(String.format(
                "Ya existe una predicción idéntica para el cliente '%s' (ID de predicción: %d). " +
                        "No se permite guardar duplicados exactos.",
                customerId, existingPredictionId
        ));
        this.customerId = customerId;
        this.existingPredictionId = existingPredictionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Long getExistingPredictionId() {
        return existingPredictionId;
    }
}
