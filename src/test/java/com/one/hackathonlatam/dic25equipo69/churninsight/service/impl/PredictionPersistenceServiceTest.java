package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.DuplicatePredictionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.PredictionPersistenceException;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionPersistenceServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private PredictionMapper predictionMapper;

    @Mock
    private FeatureExplainerService featureExplainerService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PredictionPersistenceService persistenceService;

    private PredictionRequestDTO requestDTO;
    private MLPredictionFullResponseDTO mlResponseDTO;
    private Prediction mockPrediction;

    @BeforeEach
    void setUp() {
        requestDTO = new PredictionRequestDTO(
                "CLI-TEST-001",
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

        mlResponseDTO = new MLPredictionFullResponseDTO(
                1,
                0.85,
                null
        );

        mockPrediction = Prediction.builder()
                .id(1L)
                .customerId("CLI-TEST-001")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();
    }

    @Test
    void whenSavePrediction_WithoutFeatures_thenSavesSuccessfully() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-001\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        Prediction result = persistenceService.savePrediction(requestDTO, mlResponseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo("CLI-TEST-001");
        verify(predictionRepository).save(any(Prediction.class));
        verify(predictionRepository).findByCustomerIdAndMetadataHash(anyString(), anyString());
    }

    @Test
    void whenSavePrediction_WithFeatures_thenSavesPredictionAndFeatures() throws Exception {
        MLPredictionFullResponseDTO mlResponseWithFeatures = new MLPredictionFullResponseDTO(
                1,
                0.85,
                List.of(
                        new MLFeatureImportanceDTO("complain", "1", 1, 0.35),
                        new MLFeatureImportanceDTO("age", "42", 2, 0.22)
                )
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-001\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(featureExplainerService.translateFeatureName("complain")).thenReturn("Tiene quejas");
        when(featureExplainerService.translateFeatureName("age")).thenReturn("Edad");
        when(featureExplainerService.determineImpact(anyDouble())).thenReturn(ImpactDirection.POSITIVE);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        Prediction result = persistenceService.savePrediction(requestDTO, mlResponseWithFeatures);

        assertThat(result).isNotNull();
        verify(predictionRepository).save(any(Prediction.class));
        verify(featureExplainerService, times(2)).translateFeatureName(anyString());
        verify(featureExplainerService, times(2)).determineImpact(anyDouble());
    }

    @Test
    void whenSavePrediction_DuplicateDetected_thenThrowsDuplicatePredictionException() throws Exception {
        Prediction existingPrediction = Prediction.builder()
                .id(99L)
                .customerId("CLI-TEST-001")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-001\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.of(existingPrediction));

        assertThatThrownBy(() -> persistenceService.savePrediction(requestDTO, mlResponseDTO))
                .isInstanceOf(DuplicatePredictionException.class)
                .hasMessageContaining("CLI-TEST-001");

        verify(predictionRepository, never()).save(any());
    }

    @Test
    void whenSavePrediction_DataIntegrityViolation_thenThrowsDuplicatePredictionException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-001\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThatThrownBy(() -> persistenceService.savePrediction(requestDTO, mlResponseDTO))
                .isInstanceOf(DuplicatePredictionException.class);

        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    void whenSavePrediction_JsonProcessingFails_thenThrowsPredictionPersistenceException() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("JSON serialization error"));

        assertThatThrownBy(() -> persistenceService.savePrediction(requestDTO, mlResponseDTO))
                .isInstanceOf(PredictionPersistenceException.class)
                .hasMessageContaining("Error al guardar la predicción");


        verify(predictionRepository, never()).save(any());
    }

    @Test
    void whenSavePrediction_RepositoryFails_thenThrowsPredictionPersistenceException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-001\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> persistenceService.savePrediction(requestDTO, mlResponseDTO))
                .isInstanceOf(PredictionPersistenceException.class)
                .hasMessageContaining("Error al guardar la predicción");
    }

    @Test
    void whenSavePrediction_ForecastZero_thenSavesAsNoChurn() throws Exception {
        MLPredictionFullResponseDTO noChurnResponse = new MLPredictionFullResponseDTO(
                0,
                0.25,
                null
        );

        Prediction noChurnPrediction = Prediction.builder()
                .id(2L)
                .customerId("CLI-TEST-002")
                .predictionResult(false)
                .probability(new BigDecimal("0.2500"))
                .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-002\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), eq(false), any(BigDecimal.class)))
                .thenReturn(noChurnPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(noChurnPrediction);

        Prediction result = persistenceService.savePrediction(requestDTO, noChurnResponse);

        assertThat(result.getPredictionResult()).isFalse();
        verify(predictionMapper).toEntity(anyString(), eq(false), any(BigDecimal.class));
    }

    @Test
    void whenSavePrediction_WithEmptyFeatures_thenSavesWithoutProcessingFeatures() throws Exception {
        MLPredictionFullResponseDTO emptyFeaturesResponse = new MLPredictionFullResponseDTO(
                1,
                0.75,
                List.of()
        );

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"customerId\":\"CLI-TEST-003\"}");
        when(predictionRepository.findByCustomerIdAndMetadataHash(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        Prediction result = persistenceService.savePrediction(requestDTO, emptyFeaturesResponse);

        assertThat(result).isNotNull();
        verify(featureExplainerService, never()).translateFeatureName(anyString());
        verify(featureExplainerService, never()).determineImpact(anyDouble());
    }

    @Test
    void whenSavePrediction_MetadataHashIsCalculated() throws Exception {
        String expectedMetadata = "{\"customerId\":\"CLI-TEST-001\",\"age\":42}";

        when(objectMapper.writeValueAsString(any())).thenReturn(expectedMetadata);
        when(predictionRepository.findByCustomerIdAndMetadataHash(eq("CLI-TEST-001"), anyString()))
                .thenReturn(Optional.empty());
        when(predictionMapper.toEntity(anyString(), anyBoolean(), any(BigDecimal.class)))
                .thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        persistenceService.savePrediction(requestDTO, mlResponseDTO);

        verify(predictionRepository).findByCustomerIdAndMetadataHash(eq("CLI-TEST-001"), anyString());
    }
}
