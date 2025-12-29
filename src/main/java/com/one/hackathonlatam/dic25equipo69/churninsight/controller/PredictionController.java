package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PredictionController {

    private static final String EXAMPLE_REQUEST = "{\n  \"geography\": \"Spain\",\n  \"gender\": \"Male\",\n  \"age\": 42,\n  \"creditScore\": 650,\n  \"balance\": 14.5,\n  \"estimatedSalary\": 14.0,\n  \"tenure\": 6,\n  \"numOfProducts\": 5,\n  \"satisfactionScore\": 2,\n  \"isActiveMember\": true,\n  \"hasCrCard\": true,\n  \"complain\": false\n}";

    @Operation(
        summary = "Predecir churn para un cliente",
        description = "Recibe atributos del cliente y devuelve una predicción con probabilidad. Los campos son opcionales; el modelo intentará predecir con la información provista."
    )
    @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = PredictionRequestDTO.class),
        examples = @ExampleObject(name = "example", value = EXAMPLE_REQUEST)
    ))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Predicción realizada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PredictionResponseDTO.class),
                examples = @ExampleObject(name = "success", value = "{\"forecast\": \"Va a cancelar\", \"probability\": 0.81}")
            )
        ),
        @ApiResponse(responseCode = "400", description = "Petición inválida / error de validación")
    })
    @PostMapping(path = "/predict", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PredictionResponseDTO> predict(PredictionRequestDTO request) {
        // Implementación mínima: la lógica real debería delegarse al servicio de predicción.
        // Aquí devolvemos una respuesta de ejemplo para que Swagger muestre el esquema y los ejemplos.
        PredictionResponseDTO example = new PredictionResponseDTO("Va a cancelar", 0.81);
        return ResponseEntity.ok(example);
    }
}

