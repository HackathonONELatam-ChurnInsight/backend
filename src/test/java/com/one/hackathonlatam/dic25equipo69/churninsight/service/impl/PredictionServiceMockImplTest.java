package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
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
                null, Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 1, true, true, false // satisfactionScore = 1 (bajo)
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
                null, Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 4, true, true, false // satisfactionScore = 4 (alto)
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

    // ========== TESTS NUEVOS PARA EXPLICABILIDAD ==========

    @Test
    void whenPredictWithExplanation_withLowSatisfaction_thenReturnsChurnWith3Features() {
        // Given - satisfactionScore < 3
        PredictionRequestDTO lowSatisfactionRequest = new PredictionRequestDTO(
                null, Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 2, true, true, false // satisfactionScore = 2 (bajo)
        );

        // When
        PredictionFullResponseDTO result = predictionServiceMock.predictWithExplanation(lowSatisfactionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("Va a cancelar");
        assertThat(result.probability()).isEqualTo(0.85);

        // Verificar que retorna 3 features
        assertThat(result.topFeatures()).hasSize(3);

        // Verificar que las features tienen estructura correcta
        assertThat(result.topFeatures().get(0).name()).isNotBlank();
        assertThat(result.topFeatures().get(0).value()).isNotBlank();
        assertThat(result.topFeatures().get(0).impact())
                .isIn("positivo", "negativo");
    }

    @Test
    void whenPredictWithExplanation_withHighSatisfaction_thenReturnsNoChurnWith3Features() {
        // Given - satisfactionScore >= 3
        PredictionRequestDTO highSatisfactionRequest = new PredictionRequestDTO(
                null, Geography.FRANCE, null, 35, 700, 2000.0, null, 8,
                3, 4, true, true, false // satisfactionScore = 4 (alto)
        );

        // When
        PredictionFullResponseDTO result = predictionServiceMock.predictWithExplanation(highSatisfactionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("No va a cancelar");
        assertThat(result.probability()).isEqualTo(0.25);

        // Verificar que retorna 3 features
        assertThat(result.topFeatures()).hasSize(3);
    }

    @Test
    void whenPredictWithExplanation_thenFeaturesAreInSpanish() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
                null, Geography.GERMANY, null, 50, 600, 1500.0, null, 10,
                2, 1, false, false, true // satisfactionScore = 1
        );

        // When
        PredictionFullResponseDTO result = predictionServiceMock.predictWithExplanation(request);

        // Then - Verificar que los nombres están en español (no en inglés)
        result.topFeatures().forEach(feature -> {
            assertThat(feature.name()).doesNotContain("satisfaction");
            assertThat(feature.name()).doesNotContain("complain");
            assertThat(feature.name()).doesNotContain("age");
            assertThat(feature.name()).matches(".*[a-záéíóúñÁÉÍÓÚÑ].*");
        });
    }
}
