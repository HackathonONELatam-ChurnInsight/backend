package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

/**
 * Excepción lanzada cuando hay problemas al comunicarse con el servicio de modelo ML.
 * Puede ser por timeout, error de conexión, o respuesta inválida del modelo.
 */
public class ModelServiceException extends RuntimeException {

    public ModelServiceException(String message) {
        super(message);
    }

    public ModelServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
