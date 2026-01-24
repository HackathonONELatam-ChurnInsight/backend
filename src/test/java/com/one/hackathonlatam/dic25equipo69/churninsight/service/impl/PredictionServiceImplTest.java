package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.ModelServiceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private FeatureExplainerService featureExplainerService;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    private PredictionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new PredictionRequestDTO(
                "CLI-12345",
                Geography.SPAIN,
                Gender.MALE,
                42,
                650,
                1000.0,
                50000.0,
                5,
                2,
                3,
                true,
                true,
                false
        );
    }

    @Test
    void whenPredict_WithFeatures_thenReturnsFullResponseWithTop3() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                1,
                0.85,
                List.of(
                        new MLFeatureImportanceDTO("complain", "1", 1, 0.35),
                        new MLFeatureImportanceDTO("age", "42", 2, 0.22),
                        new MLFeatureImportanceDTO("tenure", "5", 3, 0.18),
                        new MLFeatureImportanceDTO("balance", "1000.0", 4, 0.12),
                        new MLFeatureImportanceDTO("num_of_products", "2", 5, 0.08)
                )
        );

        Prediction mockPrediction = Prediction.builder()
                .id(1L)
                .customerId("CLI-12345")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);

        when(persistenceService.savePrediction(any(PredictionRequestDTO.class),
                any(MLPredictionFullResponseDTO.class)))
                .thenReturn(mockPrediction);

        when(featureExplainerService.translateFeatureName("complain"))
                .thenReturn("Tiene quejas");
        when(featureExplainerService.translateFeatureName("age"))
                .thenReturn("Edad");
        when(featureExplainerService.translateFeatureName("tenure"))
                .thenReturn("Tiempo como cliente");
        when(featureExplainerService.determineImpact(0.35))
                .thenReturn(com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection.POSITIVE);
        when(featureExplainerService.determineImpact(0.22))
                .thenReturn(com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection.POSITIVE);
        when(featureExplainerService.determineImpact(0.18))
                .thenReturn(com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection.POSITIVE);
        when(featureExplainerService.impactToString(any()))
                .thenReturn("positivo", "positivo", "positivo");

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("Va a cancelar");
        assertThat(result.probability()).isEqualTo(0.85);
        assertThat(result.topFeatures()).isNotNull();
        assertThat(result.topFeatures()).hasSize(3);
        assertThat(result.topFeatures().get(0).name()).isEqualTo("Tiene quejas");
        assertThat(result.topFeatures().get(0).value()).isEqualTo("1");

        verify(modelClientService).predictWithFeatures(any(MLPredictionRequestDTO.class));
        verify(persistenceService).savePrediction(eq(requestDTO), any(MLPredictionFullResponseDTO.class));
        verify(featureExplainerService, times(3)).translateFeatureName(anyString());
    }

    @Test
    void whenPredict_WithoutFeatures_thenReturnsResponseWithNullFeatures() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                0,
                0.25,
                null
        );

        Prediction mockPrediction = Prediction.builder()
                .id(2L)
                .customerId("CLI-12345")
                .predictionResult(false)
                .probability(new BigDecimal("0.2500"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("No va a cancelar");
        assertThat(result.probability()).isEqualTo(0.25);
        assertThat(result.topFeatures()).isNull();

        verify(modelClientService).predictWithFeatures(any(MLPredictionRequestDTO.class));
        verify(persistenceService).savePrediction(any(), any());
        verify(featureExplainerService, never()).translateFeatureName(anyString());
    }

    @Test
    void whenPredict_WithEmptyFeatures_thenReturnsResponseWithNullFeatures() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                1,
                0.75,
                List.of()
        );

        Prediction mockPrediction = Prediction.builder()
                .id(3L)
                .customerId("CLI-12345")
                .predictionResult(true)
                .probability(new BigDecimal("0.7500"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo("Va a cancelar");
        assertThat(result.topFeatures()).isNull();
    }

    @Test
    void whenModelServiceFails_thenThrowsModelServiceException() {
        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenThrow(new ModelServiceException("No se pudo conectar con el servicio de modelo ML"));

        assertThatThrownBy(() -> predictionService.predict(requestDTO))
                .isInstanceOf(ModelServiceException.class)
                .hasMessageContaining("No se pudo conectar con el servicio de modelo ML");

        verify(persistenceService, never()).savePrediction(any(), any());
    }

    @Test
    void whenPersistenceFails_thenThrowsException() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(1, 0.85, null);

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenThrow(new RuntimeException("Error al guardar en base de datos"));

        assertThatThrownBy(() -> predictionService.predict(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al procesar la predicción");

        verify(modelClientService).predictWithFeatures(any(MLPredictionRequestDTO.class));
        verify(persistenceService).savePrediction(any(), any());
    }

    @Test
    void whenPredictHighProbability_thenReturnsCorrectForecast() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                1,
                0.95,
                null
        );

        Prediction mockPrediction = Prediction.builder()
                .id(4L)
                .customerId("CLI-12345")
                .predictionResult(true)
                .probability(new BigDecimal("0.9500"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result.probability()).isEqualTo(0.95);
        assertThat(result.forecast()).isEqualTo("Va a cancelar");

        verify(persistenceService).savePrediction(eq(requestDTO), any(MLPredictionFullResponseDTO.class));
    }

    @Test
    void whenPredictLowProbability_thenReturnsNoChurn() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                0,
                0.15,
                null
        );

        Prediction mockPrediction = Prediction.builder()
                .id(5L)
                .customerId("CLI-12345")
                .predictionResult(false)
                .probability(new BigDecimal("0.1500"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result.forecast()).isEqualTo("No va a cancelar");
        assertThat(result.probability()).isEqualTo(0.15);
    }

    @Test
    void whenPredictWithMoreThan3Features_thenReturnsOnlyTop3() {
        MLPredictionFullResponseDTO mlResponse = new MLPredictionFullResponseDTO(
                1,
                0.88,
                List.of(
                        new MLFeatureImportanceDTO("complain", "1", 1, 0.40),
                        new MLFeatureImportanceDTO("age", "42", 2, 0.30),
                        new MLFeatureImportanceDTO("tenure", "5", 3, 0.20),
                        new MLFeatureImportanceDTO("balance", "1000.0", 4, 0.05),
                        new MLFeatureImportanceDTO("credit_score", "650", 5, 0.03)
                )
        );

        Prediction mockPrediction = Prediction.builder()
                .id(6L)
                .customerId("CLI-12345")
                .predictionResult(true)
                .probability(new BigDecimal("0.8800"))
                .build();

        when(modelClientService.predictWithFeatures(any(MLPredictionRequestDTO.class)))
                .thenReturn(mlResponse);
        when(persistenceService.savePrediction(any(), any()))
                .thenReturn(mockPrediction);
        when(featureExplainerService.translateFeatureName(anyString()))
                .thenReturn("Traducción");
        when(featureExplainerService.impactToString(any()))
                .thenReturn("positivo");

        PredictionFullResponseDTO result = predictionService.predict(requestDTO);

        assertThat(result.topFeatures()).hasSize(3);
        assertThat(result.topFeatures().get(0).value()).isEqualTo("1");
        assertThat(result.topFeatures().get(1).value()).isEqualTo("42");
        assertThat(result.topFeatures().get(2).value()).isEqualTo("5");

        verify(featureExplainerService, times(3)).translateFeatureName(anyString());
    }
}
