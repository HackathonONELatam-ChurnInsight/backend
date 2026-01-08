package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private ModelClientService modelClientService;

    @InjectMocks
    private PredictionServiceImpl service;

    @Test
    void predict_DelegatesToModelClient() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
            "France", "Male", 30, 600, 50000.0, 100000.0, 5, 2, 4, true, true, false
        );
        PredictionResponseDTO expectedResponse = new PredictionResponseDTO("NO_CHURN", 0.25);

        when(modelClientService.predict(any(PredictionRequestDTO.class))).thenReturn(expectedResponse);

        // When
        PredictionResponseDTO response = service.predict(request);

        // Then
        assertThat(response).isEqualTo(expectedResponse);
    }
}
