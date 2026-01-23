package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.ErrorResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para endpoint de predicción de churn.
 * Endpoint único que retorna forecast, probability y top 3 features (si el modelo las retorna).
 */
@Slf4j
@RestController
@RequestMapping("/predict")
@Tag(name = "Predicción de Churn", description = "Endpoint para predecir cancelación de clientes")
public class PredictionController {

    private static final String EXAMPLE_REQUEST = """
            {
                "customerId": "CLI-12345",
                "geography": "Spain",
                "gender": "Male",
                "age": 42,
                "creditScore": 650,
                "balance": 14.5,
                "estimatedSalary": 14.0,
                "tenure": 6,
                "numOfProducts": 3,
                "satisfactionScore": 2,
                "isActiveMember": true,
                "hasCrCard": true,
                "complain": false
            }""";

    private static final String EXAMPLE_RESPONSE_BASIC = """
            {
                "forecast": "Va a cancelar",
                "probability": 0.81,
                "top_features": null
            }""";

    private static final String EXAMPLE_RESPONSE_FULL = """
            {
                "forecast": "Va a cancelar",
                "probability": 0.81,
                "top_features": [
                    {
                        "name": "Tiene quejas",
                        "value": "1",
                        "impact": "positivo"
                    },
                    {
                        "name": "Edad",
                        "value": "42",
                        "impact": "negativo"
                    },
                    {
                        "name": "Tiempo como cliente",
                        "value": "6",
                        "impact": "negativo"
                    }
                ]
            }""";

    private static final String EXAMPLE_ERROR_400 = """
            {
                "error": "Error de validación",
                "message": "Los datos enviados no son válidos",
                "details": ["age: debe ser mayor a 18", "satisfactionScore: debe estar entre 1 y 5"],
                "timestamp": "2026-01-19T16:45:00"
            }""";

    private static final String EXAMPLE_ERROR_500 = """
            {
                "error": "Error interno",
                "message": "Ha ocurrido un error inesperado en el servidor",
                "details": ["Error al guardar la predicción en la base de datos"],
                "timestamp": "2026-01-19T16:45:00"
            }""";

    private static final String EXAMPLE_ERROR_503 = """
            {
                "error": "Error en servicio de predicción",
                "message": "El modelo de predicción no pudo procesar la solicitud",
                "details": ["No se pudo conectar con el servicio de modelo ML en: http://localhost:8000"],
                "timestamp": "2026-01-19T16:45:00"
            }""";

    private final IPredictionService predictionService;

    public PredictionController(IPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Endpoint único para predicción de churn.
     * Retorna forecast, probability y top 3 features (si el modelo las retorna).
     *
     * @param request Datos del cliente para predicción
     * @return Predicción con probabilidad y features opcionales
     */
    @Operation(
            summary = "Predecir churn para un cliente",
            description = "Recibe atributos del cliente y devuelve una predicción con probabilidad. " +
                    "Si el modelo retorna feature importances, incluye el top 3 de variables más relevantes. " +
                    "Si el modelo no las retorna, solo incluye forecast y probability (con top_features null)."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del cliente para realizar la predicción de churn",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PredictionRequestDTO.class),
                    examples = @ExampleObject(
                            name = "Cliente con alta probabilidad de churn",
                            value = EXAMPLE_REQUEST
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Predicción realizada correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredictionFullResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Predicción con explicabilidad (Top 3 features)",
                                            value = EXAMPLE_RESPONSE_FULL,
                                            description = "Cuando el modelo retorna feature importances"
                                    ),
                                    @ExampleObject(
                                            name = "Predicción básica (sin features)",
                                            value = EXAMPLE_RESPONSE_BASIC,
                                            description = "Cuando el modelo NO retorna feature importances"
                                    )
                            }
                    )),
            @ApiResponse(
                    responseCode = "400",
                    description = "Petición inválida o error de validación en los datos enviados",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error de validación",
                                    value = EXAMPLE_ERROR_400
                            )
                    )),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error interno",
                                    value = EXAMPLE_ERROR_500
                            )
                    )),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de modelo ML no disponible o sin conexión",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Modelo no disponible",
                                    value = EXAMPLE_ERROR_503
                            )
                    ))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PredictionFullResponseDTO> predict(@Valid @RequestBody PredictionRequestDTO request) {
        log.info("Recibida solicitud de predicción para customerId: {}, geografía: {}, edad: {}",
                request.customerId(), request.geography(), request.age());

        PredictionFullResponseDTO response = predictionService.predict(request);

        log.info("Predicción completada para customerId: {} - forecast: {}, probability: {}, features: {}",
                request.customerId(), response.forecast(), response.probability(),
                response.topFeatures() != null ? response.topFeatures().size() : 0);

        return ResponseEntity.ok(response);
    }
}
