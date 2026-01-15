package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para PredictionServiceMockImpl (desarrollo).
 */
@ExtendWith(MockitoExtension.class)
class PredictionServiceMockImplTest {

    @Mock
    private PredictionPersistenceService persistenceService;

    @InjectMocks
    private PredictionServiceMockImpl predictionServiceMock;

    private PredictionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new PredictionRequestDTO(
                Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 1, true, true, false  // satisfactionScore = 1 (bajo)
        );
    }

    @Test
    void whenPredictWithLowSatisfaction_thenReturnsWillChurn() {
        // Given - satisfactionScore < 3
        Prediction mockPrediction = Prediction.builder()
                .id(1L)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        PredictionResponseDTO result = predictionServiceMock.predict(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("Va a cancelar");
        assertThat(result.probability()).isEqualTo(0.85);

        verify(persistenceService).savePrediction(eq(requestDTO), any(PredictionResponseDTO.class));
    }

    @Test
    void whenPredictWithHighSatisfaction_thenReturnsNoChurn() {
        // Given - satisfactionScore >= 3
        PredictionRequestDTO highSatisfactionRequest = new PredictionRequestDTO(
                Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 4, true, true, false  // satisfactionScore = 4 (alto)
        );

        Prediction mockPrediction = Prediction.builder()
                .id(2L)
                .predictionResult(false)
                .probability(new BigDecimal("0.2500"))
                .build();

        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        PredictionResponseDTO result = predictionServiceMock.predict(highSatisfactionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("No va a cancelar");
        assertThat(result.probability()).isEqualTo(0.25);

        verify(persistenceService).savePrediction(eq(highSatisfactionRequest), any(PredictionResponseDTO.class));
    }

    @Test
    void whenPredictCalled_thenAlwaysPersists() {
        // Given
        Prediction mockPrediction = Prediction.builder()
                .id(3L)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        predictionServiceMock.predict(requestDTO);

        // Then
        verify(persistenceService, times(1))
                .savePrediction(any(PredictionRequestDTO.class), any(PredictionResponseDTO.class));
    }
}
