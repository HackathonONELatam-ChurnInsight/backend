package com.one.hackathonlatam.dic25equipo69.churninsight.client;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.FeatureExtractionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.ModelServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ModelClientService modelClientService;

    private MLPredictionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        String modelServiceUrl = "http://localhost:8000";
        modelClientService = new ModelClientService(modelServiceUrl);

        java.lang.reflect.Field restTemplateField;
        try {
            restTemplateField = ModelClientService.class.getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(modelClientService, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        requestDTO = new MLPredictionRequestDTO(
                Geography.FRANCE,
                Gender.MALE,
                30,
                600,
                50000.0,
                100000.0,
                5,
                2,
                4,
                1,
                1,
                0
        );
    }

    @Test
    void whenPredict_thenReturnsMLPredictionFullResponseDTO() {
        MLPredictionFullResponseDTO expectedResponse = new MLPredictionFullResponseDTO(0, 0.25, null);

        when(restTemplate.postForObject(
                eq("http://localhost:8000/predict"),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenReturn(expectedResponse);

        MLPredictionFullResponseDTO result = modelClientService.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo(0);
        assertThat(result.probability()).isEqualTo(0.25);
        verify(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(MLPredictionFullResponseDTO.class));
    }

    @Test
    void whenPredictWithFeatures_thenReturnsResponseWithFeatures() {
        MLPredictionFullResponseDTO expectedResponse = new MLPredictionFullResponseDTO(
                1,
                0.85,
                List.of(
                        new MLFeatureImportanceDTO("complain", "1", 1, 0.35),
                        new MLFeatureImportanceDTO("age", "42", 2, 0.22),
                        new MLFeatureImportanceDTO("tenure", "5", 3, 0.18)
                )
        );

        when(restTemplate.postForObject(
                eq("http://localhost:8000/predict"),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenReturn(expectedResponse);

        MLPredictionFullResponseDTO result = modelClientService.predictWithFeatures(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isEqualTo(1);
        assertThat(result.probability()).isEqualTo(0.85);
        assertThat(result.featureImportances()).hasSize(3);
        assertThat(result.featureImportances().get(0).featureName()).isEqualTo("complain");
    }

    @Test
    void whenPredictWithFeatures_ResponseIsNull_thenThrowsModelServiceException() {
        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenReturn(null);

        assertThatThrownBy(() -> modelClientService.predictWithFeatures(requestDTO))
                .isInstanceOf(ModelServiceException.class)
                .hasMessageContaining("El modelo retornó una respuesta vacía");
    }

    @Test
    void whenPredictWithFeatures_NoFeatures_thenThrowsFeatureExtractionException() {
        MLPredictionFullResponseDTO responseWithoutFeatures = new MLPredictionFullResponseDTO(1, 0.85, null);

        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenReturn(responseWithoutFeatures);

        assertThatThrownBy(() -> modelClientService.predictWithFeatures(requestDTO))
                .isInstanceOf(FeatureExtractionException.class)
                .hasMessageContaining("El modelo no retornó feature_importances");
    }

    @Test
    void whenPredictWithFeatures_EmptyFeatures_thenThrowsFeatureExtractionException() {
        MLPredictionFullResponseDTO responseWithEmptyFeatures = new MLPredictionFullResponseDTO(
                1,
                0.85,
                List.of()
        );

        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenReturn(responseWithEmptyFeatures);

        assertThatThrownBy(() -> modelClientService.predictWithFeatures(requestDTO))
                .isInstanceOf(FeatureExtractionException.class)
                .hasMessageContaining("El modelo no retornó feature_importances");
    }

    @Test
    void whenPredict_RestClientException_thenThrowsException() {
        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> modelClientService.predict(requestDTO))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void whenPredictWithFeatures_RestClientException_thenThrowsModelServiceException() {
        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(MLPredictionFullResponseDTO.class)))
                .thenThrow(new RestClientException("Connection refused"));

        assertThatThrownBy(() -> modelClientService.predictWithFeatures(requestDTO))
                .isInstanceOf(ModelServiceException.class)
                .hasMessageContaining("No se pudo conectar con el servicio de modelo ML");
    }

    @Test
    @Disabled("Requiere modelo DS ejecutándose en http://localhost:8000")
    void integrationTest_predict_WithRealModel() {
        ModelClientService realService = new ModelClientService("http://localhost:8000");

        MLPredictionFullResponseDTO result = realService.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isIn(0, 1);
        assertThat(result.probability()).isBetween(0.0, 1.0);
    }

    @Test
    @Disabled("Requiere modelo DS ejecutándose en http://localhost:8000")
    void integrationTest_predictWithFeatures_WithRealModel() {
        ModelClientService realService = new ModelClientService("http://localhost:8000");

        MLPredictionFullResponseDTO result = realService.predictWithFeatures(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isIn(0, 1);
        assertThat(result.probability()).isBetween(0.0, 1.0);
        assertThat(result.featureImportances()).isNotEmpty();
    }
}
