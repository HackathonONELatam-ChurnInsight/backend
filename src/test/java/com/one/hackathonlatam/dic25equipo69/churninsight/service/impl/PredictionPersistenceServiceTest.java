package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.CustomerMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PredictionPersistenceService.
 */
@ExtendWith(MockitoExtension.class)
class PredictionPersistenceServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PredictionMapper predictionMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PredictionPersistenceService persistenceService;

    private PredictionRequestDTO requestDTO;
    private PredictionResponseDTO responseDTO;
    private Customer mockCustomer;
    private Prediction mockPrediction;

    @BeforeEach
    void setUp() {
        requestDTO = new PredictionRequestDTO(
                null, Geography.SPAIN, null, 42, 650, 1000.0, null, 5,
                2, 3, true, true, false);

        responseDTO = new PredictionResponseDTO("test-client-id", "Va a cancelar", 0.85, "2026-01-27T10:00:00");

        mockCustomer = Customer.builder()
                .id(1L)
                .customerId("test-uuid")
                .geography(Geography.SPAIN)
                .age(42)
                .build();

        mockPrediction = Prediction.builder()
                .id(1L)
                .customer(mockCustomer)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();
    }

    @Test
    void whenSavePrediction_thenCreatesCustomerAndPrediction() throws Exception {
        // Given
        when(customerRepository.findByCustomerId(anyString())).thenReturn(Optional.empty());
        when(customerMapper.toEntity(any(), anyString())).thenReturn(mockCustomer);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"geography\":\"Spain\"}");
        when(predictionMapper.toEntity(any(), any(), any(), anyString())).thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        // When
        Prediction result = persistenceService.savePrediction(requestDTO, responseDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepository).save(any(Customer.class));
        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    void whenSavePredictionWithExistingCustomer_thenUsesExisting() throws Exception {
        // Given
        when(customerRepository.findByCustomerId(anyString())).thenReturn(Optional.of(mockCustomer));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(predictionMapper.toEntity(any(), any(), any(), anyString())).thenReturn(mockPrediction);
        when(predictionRepository.save(any(Prediction.class))).thenReturn(mockPrediction);

        // When
        Prediction result = persistenceService.savePrediction(requestDTO, responseDTO);

        // Then
        verify(customerRepository, never()).save(any());
        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    void whenGetCustomerHistory_thenReturnsListOfPredictions() {
        // Given
        List<Prediction> mockPredictions = List.of(mockPrediction);
        when(predictionRepository.findByCustomerIdOrderByCreatedAtDesc(1L))
                .thenReturn(mockPredictions);

        // When
        List<Prediction> result = persistenceService.getCustomerHistory(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(predictionRepository).findByCustomerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void whenGetStatistics_thenReturnsAggregatedData() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Object[] mockStats = new Object[] { 10L, 6L, 4L };
        when(predictionRepository.getStatisticsByDateRange(start, end))
                .thenReturn(mockStats);

        // When
        Object[] result = persistenceService.getStatistics(start, end);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        verify(predictionRepository).getStatisticsByDateRange(start, end);
    }
}
