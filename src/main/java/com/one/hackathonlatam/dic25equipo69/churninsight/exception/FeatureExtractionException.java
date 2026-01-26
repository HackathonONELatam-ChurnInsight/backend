package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

/**
 * Excepción lanzada cuando hay problemas al extraer o procesar feature importances.
 * Por ejemplo: features vacías, ranking inválido, datos incompletos.
 */
public class FeatureExtractionException extends RuntimeException {

    public FeatureExtractionException(String message) {
        super(message);
    }

    public FeatureExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
