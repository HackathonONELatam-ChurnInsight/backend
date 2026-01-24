package com.one.hackathonlatam.dic25equipo69.churninsight.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PredictEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void predict_WithDevProfile_ReturnsMockedServiceResponse() throws Exception {
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-TEST-001",
                Geography.FRANCE,
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

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forecast").value("No va a cancelar"))
                .andExpect(jsonPath("$.probability").value(0.25))
                .andExpect(jsonPath("$.top_features").exists());
    }

    @Test
    void predict_MissingCustomerId_ReturnsBadRequest() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                null,
                Geography.FRANCE,
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

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.message").value("Los datos enviados no son válidos"))
                .andExpect(jsonPath("$.details[0]").value("El customerid es obligatorio"));
    }

    @Test
    void predict_MissingGeography_ReturnsBadRequest() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                "CLI-TEST-002",
                null,
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

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("El campo 'geography' es obligatorio"));
    }

    @Test
    void predict_InvalidAge_ReturnsBadRequest() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                "CLI-TEST-003",
                Geography.SPAIN,
                Gender.FEMALE,
                15,
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

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("La edad mínima permitida es 18 años"));
    }

    @Test
    void predict_AllRequiredFieldsPresent_ReturnsOk() throws Exception {
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-TEST-004",
                Geography.GERMANY,
                Gender.FEMALE,
                45,
                720,
                75000.0,
                120000.0,
                8,
                3,
                5,
                false,
                true,
                true
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecast").exists())
                .andExpect(jsonPath("$.probability").exists())
                .andExpect(jsonPath("$.top_features").exists());
    }

    @Test
    void predict_MinimumRequiredFields_ReturnsOk() throws Exception {
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-TEST-005",
                Geography.SPAIN,
                null,
                25,
                500,
                0.0,
                null,
                null,
                1,
                3,
                true,
                null,
                false
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecast").exists())
                .andExpect(jsonPath("$.probability").isNumber());
    }

    @Test
    void predict_InvalidCreditScore_ReturnsBadRequest() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                "CLI-TEST-006",
                Geography.FRANCE,
                Gender.MALE,
                30,
                50,
                50000.0,
                100000.0,
                5,
                2,
                4,
                true,
                true,
                false
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("El creditScore mínimo es 100"));
    }

    @Test
    void predict_InvalidNumOfProducts_ReturnsBadRequest() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                "CLI-TEST-007",
                Geography.GERMANY,
                Gender.FEMALE,
                35,
                650,
                50000.0,
                100000.0,
                5,
                5,
                4,
                true,
                true,
                false
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("El número máximo de productos es 4"));
    }

    @Test
    void predict_MalformedJson_ReturnsBadRequest() throws Exception {
        String malformedJson = "{\"customerId\": \"CLI-TEST-008\", \"geography\": \"Spain\", invalid json";

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error en la petición"));
    }

    @Test
    void predict_ResponseContainsAllRequiredFields() throws Exception {
        PredictionRequestDTO request = new PredictionRequestDTO(
                "CLI-TEST-009",
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

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecast").isString())
                .andExpect(jsonPath("$.probability").isNumber())
                .andExpect(jsonPath("$.top_features").exists());
    }
}
