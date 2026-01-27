package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.junit.jupiter.api.BeforeEach;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Tests unitarios para PredictionServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

        @Mock
        private ModelClientService modelClientService;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private PredictionRepository predictionRepository;

        @Mock
        private PredictionPersistenceService persistenceService;

        @InjectMocks
        private PredictionServiceImpl service;

        @InjectMocks
        private PredictionServiceImpl predictionService;

        private PredictionRequestDTO requestDTO;
        private MLPredictionResponseDTO mlResponseDTO;

        @BeforeEach
        void setUp() {
                requestDTO = new PredictionRequestDTO(
                                null, Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                                2, 3, true, true, false);

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
                when(persistenceService.saveMlPrediction(any(), any()))
                                .thenReturn(mockPrediction);

                // When
                PredictionResponseDTO result = predictionService.predict(requestDTO);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.forecast()).isEqualTo("Va a cancelar");
                assertThat(result.probability()).isEqualTo(0.85);
                verify(modelClientService).predict(any(MLPredictionRequestDTO.class));
                verify(persistenceService).saveMlPrediction(eq(requestDTO), any(MLPredictionResponseDTO.class));
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
                when(persistenceService.saveMlPrediction(any(), any()))
                                .thenReturn(mockPrediction);

                // When
                PredictionResponseDTO result = predictionService.predict(requestDTO);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.forecast()).isEqualTo("No va a cancelar");
                assertThat(result.probability()).isEqualTo(0.25);
                verify(modelClientService).predict(any(MLPredictionRequestDTO.class));
                verify(persistenceService).saveMlPrediction(any(), any());
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

                verify(persistenceService, never()).saveMlPrediction(any(), any());
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
                when(persistenceService.saveMlPrediction(any(), any()))
                                .thenReturn(mockPrediction);

                // When
                PredictionResponseDTO result = predictionService.predict(requestDTO);

                // Then
                assertThat(result.probability()).isEqualTo(0.95);
                assertThat(result.forecast()).isEqualTo("Va a cancelar");
                verify(persistenceService).saveMlPrediction(eq(requestDTO), any(MLPredictionResponseDTO.class));
        }

        @Test
        void predict_DelegatesToModelClient() {
                // Given
                PredictionRequestDTO request = new PredictionRequestDTO(
                                null, Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0, 5, 2, 4, true, true,
                                false);
                // (Asumo que el constructor es Integer forecast, Double probability)
                MLPredictionResponseDTO mlResponse = new MLPredictionResponseDTO(0, 0.25);

                // Configurar comportamiento del Mock de Python
                when(modelClientService.predict(any(MLPredictionRequestDTO.class))).thenReturn(mlResponse);

                // Simulamos guardado de Cliente en BD para evitar NullPointerException
                Customer savedCustomer = new Customer();
                savedCustomer.setCustomerId("test-uuid-123");
                when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

                // Simulamos guardado de Predicción
                Prediction savedPrediction = new Prediction();
                savedPrediction.setCreatedAt(LocalDateTime.now());
                when(predictionRepository.save(any(Prediction.class))).thenReturn(savedPrediction);

                // When
                PredictionResponseDTO response = service.predict(request);

                // Then
                // Verificamos que la conversión se hizo correctamente (0 -> "No va a cancelar")
                assertThat(response.forecast()).isEqualTo("No va a cancelar");
                assertThat(response.probability()).isEqualTo(0.25);

                // Verificamos que se llamó a los repositorios
                verify(customerRepository).save(any(Customer.class));
                verify(predictionRepository).save(any(Prediction.class));
        }

        // Test Stats (Necesario para el 80% de cobertura)
        @Test
        void getStats_CalculatesCorrectly() {
                // Given
                when(predictionRepository.count()).thenReturn(100L);
                when(predictionRepository.countByPredictionResult(true)).thenReturn(20L);

                // When
                StatsDTO stats = service.getStats();

                // Then
                assertThat(stats.totalEvaluated()).isEqualTo(100);
                assertThat(stats.highRiskCount()).isEqualTo(20);
                assertThat(stats.churnRate()).isEqualTo(0.20);
        }

        // Test Historial (Necesario para el 80% de cobertura)
        @Test
        void getRecentHistory_ReturnsMappedDTOs() {
                // Given
                Customer c = new Customer();
                c.setCustomerId("abc-123");

                Prediction p = new Prediction();
                p.setPredictionResult(true);
                p.setProbability(new BigDecimal("0.95"));
                p.setCreatedAt(LocalDateTime.now());
                p.setCustomer(c);

                when(predictionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(p));

                // When
                List<PredictionResponseDTO> history = service.getRecentHistory();

                // Then
                assertThat(history).hasSize(1);
                assertThat(history.get(0).clientId()).isEqualTo("abc-123");
                assertThat(history.get(0).forecast()).isEqualTo("Va a cancelar");
        }
}
