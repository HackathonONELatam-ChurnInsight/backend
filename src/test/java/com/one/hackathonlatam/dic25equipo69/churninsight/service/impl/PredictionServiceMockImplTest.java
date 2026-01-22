package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PredictionServiceMockImplTest {

    private final PredictionServiceMockImpl service = new PredictionServiceMockImpl();

    @Test
    void predict_AgeOver50_ReturnsChurn() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
            Geography.FRANCE, Gender.MALE, 55, 600, 50000.0, 100000.0, 5, 2, 4, true, true, false
        );

        // When
        PredictionResponseDTO response = service.predict(request);

        // Then
        assertThat(response.forecast()).isEqualTo("CHURN");
        assertThat(response.probability()).isEqualTo(0.85);
    }

    @Test
    void predict_AgeUnder50_ReturnsNoChurn() {
        // Given
        PredictionRequestDTO request = new PredictionRequestDTO(
            Geography.FRANCE, Gender.MALE, 30, 600, 50000.0, 100000.0, 5, 2, 4, true, true, false
        );

        // When
        PredictionResponseDTO response = service.predict(request);

        // Then
        assertThat(response.forecast()).isEqualTo("NO_CHURN");
        assertThat(response.probability()).isEqualTo(0.15);
    }
}
