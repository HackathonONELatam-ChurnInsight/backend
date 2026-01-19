package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.ErrorResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
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
 * Controller para endpoints de predicción de churn.
 * Proporciona dos niveles de predicción:
 * - Básica: solo forecast y probabilidad
 * - Con explicabilidad: incluye las 3 variables más relevantes
 */
@Slf4j
@RestController
@RequestMapping("/predict")
@Tag(name = "Predicción de Churn", description = "Endpoints para predecir cancelación de clientes")
public class PredictionController {

    private static final String EXAMPLE_REQUEST = """
            {
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
              "probability": 0.81
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

    private static final String EXAMPLE_ERROR_422 = """
            {
              "error": "Error en explicabilidad",
              "message": "No se pudieron extraer las variables más relevantes",
              "details": ["El modelo no retornó feature_importances", "Verifique que el modelo esté retornando feature_importances correctamente"],
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
     * Endpoint para predicción básica de churn (sin explicabilidad).
     * Retorna únicamente el forecast y la probabilidad.
     *
     * @param request Datos del cliente para predicción
     * @return Predicción con probabilidad
     */
    @Operation(
            summary = "Predecir churn para un cliente",
            description = "Recibe atributos del cliente y devuelve una predicción con probabilidad. " +
                    "Los campos son opcionales; el modelo intentará predecir con la información provista. " +
                    "Este endpoint NO incluye explicabilidad (variables relevantes)."
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
                            schema = @Schema(implementation = PredictionResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Predicción exitosa",
                                    value = EXAMPLE_RESPONSE_BASIC
                            )
                    )
            ),
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
                    )
            ),
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
                    )
            ),
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
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PredictionResponseDTO> predict(@Valid @RequestBody PredictionRequestDTO request) {
        log.info("Recibida solicitud de predicción para cliente con geografía: {} y edad: {}",
                request.geography(), request.age());

        PredictionResponseDTO response = predictionService.predict(request);

        log.info("Predicción completada exitosamente: {}", response.forecast());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para predicción de churn CON explicabilidad (top 3 features).
     * Retorna forecast, probabilidad y las 3 variables más influyentes en la decisión.
     *
     * @param request Datos del cliente para predicción
     * @return Predicción con probabilidad y top 3 variables más influyentes
     */
    @Operation(
            summary = "Predecir churn con explicabilidad",
            description = "Recibe atributos del cliente y devuelve una predicción con probabilidad más las 3 variables " +
                    "más relevantes que influyeron en el resultado. Útil para entender qué factores están impactando " +
                    "la decisión del modelo. Cada feature incluye su valor actual y si su impacto es positivo " +
                    "(aumenta probabilidad de churn) o negativo (reduce probabilidad de churn)."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del cliente para realizar la predicción de churn con explicabilidad",
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
                    description = "Predicción con explicabilidad realizada correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredictionFullResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Predicción exitosa con explicabilidad",
                                    value = EXAMPLE_RESPONSE_FULL,
                                    description = "Incluye las 3 variables más relevantes que influyeron en la decisión"
                            )
                    )
            ),
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
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Error al extraer feature importances del modelo. El modelo retornó datos incompletos o en formato inválido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error en explicabilidad",
                                    value = EXAMPLE_ERROR_422,
                                    description = "Ocurre cuando el modelo no retorna feature_importances o están vacías"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al procesar o guardar la predicción",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Error interno",
                                    value = EXAMPLE_ERROR_500
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de modelo ML no disponible, sin conexión o timeout",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Modelo no disponible",
                                    value = EXAMPLE_ERROR_503,
                                    description = "El servicio de modelo DS no responde o no está accesible"
                            )
                    )
            )
    })
    @PostMapping(
            value = "/with-explanation",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PredictionFullResponseDTO> predictWithExplanation(
            @Valid @RequestBody PredictionRequestDTO request) {

        log.info("Recibida solicitud de predicción CON EXPLICABILIDAD para cliente con geografía: {} y edad: {}",
                request.geography(), request.age());

        PredictionFullResponseDTO response = predictionService.predictWithExplanation(request);

        log.info("Predicción con explicabilidad completada: forecast={}, probability={}, top_features={}",
                response.forecast(), response.probability(), response.topFeatures().size());

        return ResponseEntity.ok(response);
    }
}
