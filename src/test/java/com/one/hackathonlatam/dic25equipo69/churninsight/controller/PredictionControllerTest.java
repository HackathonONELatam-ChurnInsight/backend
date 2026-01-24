package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.DuplicatePredictionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.ModelServiceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictionController.class)
@ActiveProfiles("dev")
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IPredictionService predictionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void predict_ValidRequestWithFeatures_ReturnsOkWithTopFeatures() throws Exception {
        // Given - Cliente con alta probabilidad de churn y features
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-12345", // ✅ customerId ahora es obligatorio
                Geography.SPAIN,
                Gender.MALE,
                42,
                650,
                14.5,
                14.0,
                6,
                3,
                2,
                true,
                true,
                false
        );

        // ✅ Usar PredictionFullResponseDTO con top 3 features
        PredictionFullResponseDTO response = new PredictionFullResponseDTO(
                "Va a cancelar",
                0.81,
                List.of(
                        new FeatureImportanceResponseDTO("Tiene quejas", "1", "positivo"),
                        new FeatureImportanceResponseDTO("Edad", "42", "negativo"),
                        new FeatureImportanceResponseDTO("Tiempo como cliente", "6", "negativo")
                )
        );

        when(predictionService.predict(any(PredictionRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forecast").value("Va a cancelar"))
                .andExpect(jsonPath("$.probability").value(0.81))
                // ✅ Validar que top_features existe y tiene 3 elementos
                .andExpect(jsonPath("$.top_features").isArray())
                .andExpect(jsonPath("$.top_features.length()").value(3))
                .andExpect(jsonPath("$.top_features[0].name").value("Tiene quejas"))
                .andExpect(jsonPath("$.top_features[0].value").value("1"))
                .andExpect(jsonPath("$.top_features[0].impact").value("positivo"));
    }

    @Test
    void predict_ValidRequestWithoutFeatures_ReturnsOkWithNullFeatures() throws Exception {
        // Given - Predicción básica sin features (modelo no las retorna)
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-67890",
                Geography.FRANCE,
                Gender.FEMALE,
                30,
                600,
                50000.0,
                100000.0,
                5,
                2,
                4,
                true,
                true,
                false
        );

        // ✅ Respuesta sin features (top_features = null)
        PredictionFullResponseDTO response = new PredictionFullResponseDTO(
                "No va a cancelar",
                0.25,
                null // Sin features
        );

        when(predictionService.predict(any(PredictionRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forecast").value("No va a cancelar"))
                .andExpect(jsonPath("$.probability").value(0.25))
                // ✅ Validar que top_features es null
                .andExpect(jsonPath("$.top_features").doesNotExist());
    }

    @Test
    void predict_MissingCustomerId_ReturnsBadRequest() throws Exception {
        // Given - Request sin customerId (campo obligatorio)
        String invalidRequestJson = """
                {
                    "geography": "Spain",
                    "gender": "Male",
                    "age": 30,
                    "creditScore": 600,
                    "balance": 50000.0,
                    "estimatedSalary": 100000.0,
                    "tenure": 5,
                    "numOfProducts": 2,
                    "satisfactionScore": 4,
                    "isActiveMember": true,
                    "hasCrCard": true,
                    "complain": false
                }
                """;

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.message").value("Los datos enviados no son válidos"))
                .andExpect(jsonPath("$.details[0]").value("El customerid es obligatorio"));
    }

    @Test
    void predict_InvalidGeography_ReturnsBadRequest() throws Exception {
        // Given - Geografía inválida
        String invalidRequestJson = """
                {
                    "customerId": "CLI-99999",
                    "geography": "Invalid",
                    "gender": "Male",
                    "age": 30,
                    "creditScore": 600,
                    "balance": 50000.0,
                    "estimatedSalary": 100000.0,
                    "tenure": 5,
                    "numOfProducts": 2,
                    "satisfactionScore": 4,
                    "isActiveMember": true,
                    "hasCrCard": true,
                    "complain": false
                }
                """;

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Error en la petición"));
    }

    @Test
    void predict_InvalidAge_ReturnsBadRequest() throws Exception {
        // Given - Edad menor a 18 (validación)
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-INVALID-AGE",
                Geography.GERMANY,
                Gender.MALE,
                15, // ❌ Edad inválida
                600,
                50000.0,
                100000.0,
                5,
                2,
                4,
                true,
                true,
                false
        );

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("La edad mínima permitida es 18 años"));
    }

    @Test
    void predict_ModelServiceUnavailable_ReturnsServiceUnavailable() throws Exception {
        // Given - Servicio de modelo ML no disponible
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-SERVICE-DOWN",
                Geography.SPAIN,
                Gender.MALE,
                30,
                600,
                50000.0,
                100000.0,
                5,
                2,
                4,
                true,
                true,
                false
        );

        when(predictionService.predict(any(PredictionRequestDTO.class)))
                .thenThrow(new ModelServiceException("No se pudo conectar con el servicio de modelo ML"));

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable()) // 503
                .andExpect(jsonPath("$.error").value("Error en servicio de predicción"))
                .andExpect(jsonPath("$.message").value("El modelo de predicción no pudo procesar la solicitud"));
    }

    @Test
    void predict_DuplicatePrediction_ReturnsConflict() throws Exception {
        // Given - Predicción duplicada (mismo customerId y metadata)
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-DUPLICATE",
                Geography.SPAIN,
                Gender.MALE,
                42,
                650,
                14.5,
                14.0,
                6,
                3,
                2,
                true,
                true,
                false
        );

        when(predictionService.predict(any(PredictionRequestDTO.class)))
                .thenThrow(new DuplicatePredictionException("CLI-DUPLICATE", 123L));

        // When & Then
        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // ✅ 409 Conflict
                .andExpect(jsonPath("$.error").value("Predicción duplicada"))
                .andExpect(jsonPath("$.message").value("Ya existe una predicción idéntica para este cliente"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(3));
    }
}
