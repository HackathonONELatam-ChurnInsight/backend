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
                null, Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0,
                5, 2, 4, true, true, false
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecast").value("No va a cancelar"))
                .andExpect(jsonPath("$.probability").value(0.25));
    }

    @Test
    void predict_InvalidPayload_PropagatesValidationError() throws Exception {
        PredictionRequestDTO invalidRequest = new PredictionRequestDTO(
                null, null, Gender.MALE, 30, 600, 50000.0, 100000.0,
                5, 2, 4, true, true, false
        );

        mockMvc.perform(post("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación"))
                .andExpect(jsonPath("$.details[0]").value("El campo 'geography' es obligatorio"));
    }
}
