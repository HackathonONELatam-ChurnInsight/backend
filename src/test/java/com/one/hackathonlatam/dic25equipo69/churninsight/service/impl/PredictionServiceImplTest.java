package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PredictionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private ModelClientService modelClientService;

    @Mock
    private PredictionPersistenceService persistenceService;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    private PredictionRequestDTO requestDTO;
    private MLPredictionResponseDTO mlResponseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new PredictionRequestDTO(
                Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 3, true, true, false
        );

        // Modelo ML ahora retorna 1 (va a cancelar) o 0 (no va a cancelar)
        mlResponseDTO = new MLPredictionResponseDTO(1, 0.85);
    }

    @Test
    void whenPredict_thenCallsModelAndPersists() {
        // Given
        Prediction mockPrediction = Prediction.builder()
                .id(1L)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        when(modelClientService.predict(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponseDTO);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        PredictionResponseDTO result = predictionService.predict(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("Va a cancelar");
        assertThat(result.probability()).isEqualTo(0.85);

        verify(modelClientService).predict(any(MLPredictionRequestDTO.class));
        verify(persistenceService).savePrediction(eq(requestDTO), any(PredictionResponseDTO.class));
    }

    @Test
    void whenPredictNoChurn_thenReturnsNoChurnMessage() {
        // Given - Cliente que NO va a cancelar
        MLPredictionResponseDTO noChurnResponse = new MLPredictionResponseDTO(0, 0.25);

        Prediction mockPrediction = Prediction.builder()
                .id(2L)
                .predictionResult(false)
                .probability(new BigDecimal("0.2500"))
                .build();

        when(modelClientService.predict(any(MLPredictionRequestDTO.class)))
                .thenReturn(noChurnResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        PredictionResponseDTO result = predictionService.predict(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("No va a cancelar");
        assertThat(result.probability()).isEqualTo(0.25);

        verify(modelClientService).predict(any(MLPredictionRequestDTO.class));
        verify(persistenceService).savePrediction(any(), any());
    }

    @Test
    void whenPredictFails_thenThrowsException() {
        // Given
        when(modelClientService.predict(any(MLPredictionRequestDTO.class)))
                .thenThrow(new RuntimeException("Model service error"));

        // When/Then
        try {
            predictionService.predict(requestDTO);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Model service error");
        }

        verify(persistenceService, never()).savePrediction(any(), any());
    }

    @Test
    void whenPredictHighProbability_thenPersistsCorrectly() {
        // Given - Alta probabilidad de churn
        MLPredictionResponseDTO highChurnResponse = new MLPredictionResponseDTO(1, 0.95);

        Prediction mockPrediction = Prediction.builder()
                .id(3L)
                .predictionResult(true)
                .probability(new BigDecimal("0.9500"))
                .build();

        when(modelClientService.predict(any(MLPredictionRequestDTO.class)))
                .thenReturn(highChurnResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        // When
        PredictionResponseDTO result = predictionService.predict(requestDTO);

        // Then
        assertThat(result.probability()).isEqualTo(0.95);
        assertThat(result.forecast()).isEqualTo("Va a cancelar");

        verify(persistenceService).savePrediction(eq(requestDTO), any(PredictionResponseDTO.class));
    }
}
