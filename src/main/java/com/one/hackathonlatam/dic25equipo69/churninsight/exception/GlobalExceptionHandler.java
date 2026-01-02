package com.one.hackathonlatam.dic25equipo69.churninsight.exception;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
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
