package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Error de validación en la petición: {}", ex.getBindingResult().getFieldError().getDefaultMessage());
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error de validación")
                .message("Los datos enviados no son válidos")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonParseException(
            HttpMessageNotReadableException ex) {
        log.error("Error en el cuerpo de la petición: {}", ex.getMessage());

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .error("Error en la petición")
                .message("El cuerpo de la petición es inválido")
                .details(List.of(ex.getMostSpecificCause().getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleRestClientException(RestClientException ex) {
        log.error("Error de cliente REST capturado: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error de conexión")
                .message("No se pudo establecer comunicación con el modelo de predicción")
                .details(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maneja errores relacionados con el servicio de modelo ML.
     * Incluye problemas de comunicación, timeouts, y respuestas inválidas.
     */
    @ExceptionHandler(ModelServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleModelServiceException(ModelServiceException ex) {
        log.error("Error en servicio de modelo ML: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error en servicio de predicción")
                .message("El modelo de predicción no pudo procesar la solicitud")
                .details(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maneja errores al extraer o procesar feature importances.
     * Ocurre cuando la respuesta del modelo no tiene el formato esperado.
     */
    @ExceptionHandler(FeatureExtractionException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeatureExtractionException(FeatureExtractionException ex) {
        log.error("Error al extraer feature importances: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error en explicabilidad")
                .message("No se pudieron extraer las variables más relevantes")
                .details(List.of(ex.getMessage(),
                        "Verifique que el modelo esté retornando feature_importances correctamente"))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Maneja errores de persistencia de predicciones con features.
     * Ocurre cuando falla el guardado en base de datos.
     */
    @ExceptionHandler(PredictionPersistenceException.class)
    public ResponseEntity<ErrorResponseDTO> handlePredictionPersistenceException(PredictionPersistenceException ex) {
        log.error("Error al persistir predicción: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error de persistencia")
                .message("No se pudo guardar la predicción en la base de datos")
                .details(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja intentos de guardar predicciones duplicadas.
     * Ocurre cuando ya existe una predicción con el mismo customerId y metadata.
     * Retorna HTTP 409 Conflict indicando que el recurso ya existe.
     */
    @ExceptionHandler(DuplicatePredictionException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicatePredictionException(DuplicatePredictionException ex) {
        log.warn("Predicción duplicada rechazada para customerId: {} - Predicción existente ID: {}",
                ex.getCustomerId(), ex.getExistingPredictionId());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Predicción duplicada")
                .message("Ya existe una predicción idéntica para este cliente")
                .details(List.of(
                        ex.getMessage(),
                        "Si los datos del cliente han cambiado, envíe la nueva información",
                        "Si desea consultar la predicción existente, use el endpoint GET /api/v1/predictions/" + ex.getCustomerId()
                ))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex) {
        log.error("Excepción interna no controlada: ", ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .error("Error interno")
                .message("Ha ocurrido un error inesperado en el servidor")
                .details(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
