package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PredictionServiceMockImplTest {

    @InjectMocks
    private PredictionServiceMockImpl service;

    @Test
    void predict_WhenAgeGreaterThan50_ReturnsChurn() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
                Geography.SPAIN, Gender.FEMALE, 55, 600, 1000.0, 2000.0, 5, 2, 3, true, true, false
        );

        // When
        PredictionResponseDTO response = service.predict(request);

        // Then
        // CORREGIDO: Ahora validamos el texto real que usa tu aplicación
        assertThat(response.forecast()).isEqualTo("CHURN");
        assertThat(response.probability()).isEqualTo(0.85);
    }

    @Test
    void predict_WhenAgeLessThan50_ReturnsNoChurn() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
                Geography.FRANCE, Gender.MALE, 30, 600, 1000.0, 2000.0, 5, 2, 3, true, true, false
        );

        // When
        PredictionResponseDTO response = service.predict(request);

        // Then
        // CORREGIDO: Texto en español
        assertThat(response.forecast()).isEqualTo("NO_CHURN");
        assertThat(response.probability()).isEqualTo(0.15);
    }

    // Test para subir cobertura (Stats)
    @Test
    void getStats_ReturnsMockedData() {
        // When
        StatsDTO stats = service.getStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.totalEvaluated()).isGreaterThan(0);
    }

    // Test para subir cobertura (History)
    @Test
    void getRecentHistory_ReturnsMockedList() {
        // When
        List<PredictionResponseDTO> history = service.getRecentHistory();

        // Then
        assertThat(history).isNotNull();
        assertThat(history).isNotEmpty();
        assertThat(history.size()).isEqualTo(3); // El mock devuelve 3 fijos
    }
}