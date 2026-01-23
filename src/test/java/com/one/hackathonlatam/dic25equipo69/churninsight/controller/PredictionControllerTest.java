package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
    void predict_ValidRequest_ReturnsOk() throws Exception {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
           null, Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0, 5, 2, 4, true, true, false
        );
        PredictionResponseDTO response = new PredictionResponseDTO("NO_CHURN", 0.25);

        when(predictionService.predict(any(PredictionRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forecast").value("NO_CHURN"))
                .andExpect(jsonPath("$.probability").value(0.25));
    }

    @Test
    void predict_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - usando un valor JSON raw con valor inválido para geography
        String invalidRequestJson = """
            {
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
}
