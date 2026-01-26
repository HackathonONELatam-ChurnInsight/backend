package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

/**
 * Excepción lanzada cuando hay problemas al persistir predicciones o feature importances en la base de datos.
 */
public class PredictionPersistenceException extends RuntimeException {

    public PredictionPersistenceException(String message) {
        super(message);
    }

    public PredictionPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
