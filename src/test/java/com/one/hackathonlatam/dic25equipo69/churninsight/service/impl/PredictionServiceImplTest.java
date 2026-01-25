package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private ModelClientService modelClientService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @InjectMocks
    private PredictionServiceImpl service;

    @Test
    void predict_DelegatesToModelClient() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
            Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0, 5, 2, 4, true, true, false
        );
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

    //  Test  Stats (Necesario para el 80% de cobertura)
    @Test
    void getStats_CalculatesCorrectly() {
        // Given
        when(predictionRepository.count()).thenReturn(100L);
        when(predictionRepository.countChurnRisks()).thenReturn(20L);

        // When
        StatsDTO stats = service.getStats();

        // Then
        assertThat(stats.totalEvaluated()).isEqualTo(100);
        assertThat(stats.highRiskCount()).isEqualTo(20);
        assertThat(stats.churnRate()).isEqualTo(0.20);
    }

    //  Test  Historial (Necesario para el 80% de cobertura)
    @Test
    void getRecentHistory_ReturnsMappedDTOs() {
        // Given
        Customer c = new Customer();
        c.setCustomerId("abc-123");

        Prediction p = new Prediction();
        p.setPredictionResult("Va a cancelar");
        p.setProbability(0.95);
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
