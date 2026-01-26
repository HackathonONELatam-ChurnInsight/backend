package com.one.hackathonlatam.dic25equipo69.churninsight.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import org.junit.jupiter.api.Disabled;
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

@Disabled("Esperando modelo DS")
@WebMvcTest
@ActiveProfiles("dev")
class ModelClientServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelClientService modelClientService;  // ← Mock del service

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void predict_ReturnsResponseFromServer() throws Exception {
        // Given
        MLPredictionRequestDTO request = new MLPredictionRequestDTO(
                Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0, 5, 2, 4, 1, 1, 1
        );
        MLPredictionResponseDTO expectedResponse = new MLPredictionResponseDTO(0, 0.25);
        when(modelClientService.predict(any(MLPredictionRequestDTO.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/predict")  // ← Endpoint del controller
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecast").value("NO_CHURN"))
                .andExpect(jsonPath("$.probability").value(0.25));
    }
}
