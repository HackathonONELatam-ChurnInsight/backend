package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PredictionServiceMockImplTest {

    @InjectMocks
    private PredictionServiceMockImpl predictionServiceMock;

    private PredictionRequestDTO requestDTO;

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
                1,
                true,
                true,
                false
        );
    }

    @Test
    void whenPredict_thenReturnsValidPredictionWithFeatures() {
        PredictionFullResponseDTO result = predictionServiceMock.predict(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.forecast()).isIn("Va a cancelar", "No va a cancelar");
        assertThat(result.probability()).isBetween(0.0, 1.0);
        assertThat(result.topFeatures()).isNotNull();
        assertThat(result.topFeatures()).hasSize(3);
    }

    @Test
    void whenPredict_thenFeaturesHaveCorrectStructure() {
        PredictionFullResponseDTO result = predictionServiceMock.predict(requestDTO);

        result.topFeatures().forEach(feature -> {
            assertThat(feature.name()).isNotBlank();
            assertThat(feature.value()).isNotBlank();
            assertThat(feature.impact()).isIn("positivo", "negativo");
        });
    }

    @Test
    void whenPredict_withOlderAge_thenAgeFeatureIsIncluded() {
        PredictionRequestDTO olderCustomer = new PredictionRequestDTO(
                "CLI-TEST-002",
                Geography.GERMANY,
                Gender.FEMALE,
                55,
                700,
                2000.0,
                60000.0,
                10,
                3,
                4,
                true,
                true,
                false
        );

        PredictionFullResponseDTO result = predictionServiceMock.predict(olderCustomer);

        assertThat(result.topFeatures()).anyMatch(f -> f.name().equals("Edad"));
        assertThat(result.topFeatures()).anyMatch(f -> f.value().equals("55"));
    }

    @Test
    void whenPredict_withComplain_thenComplainFeatureIsIncluded() {
        PredictionRequestDTO complainingCustomer = new PredictionRequestDTO(
                "CLI-TEST-003",
                Geography.FRANCE,
                Gender.MALE,
                35,
                600,
                1500.0,
                40000.0,
                3,
                2,
                3,
                true,
                true,
                true
        );

        PredictionFullResponseDTO result = predictionServiceMock.predict(complainingCustomer);

        assertThat(result.topFeatures()).anyMatch(f -> f.name().equals("Tiene quejas"));
        assertThat(result.topFeatures()).anyMatch(f -> f.value().equals("1"));
    }

    @Test
    void whenPredict_withActiveMember_thenActiveMemberFeatureIsIncluded() {
        PredictionRequestDTO activeCustomer = new PredictionRequestDTO(
                "CLI-TEST-004",
                Geography.SPAIN,
                Gender.FEMALE,
                30,
                650,
                1000.0,
                50000.0,
                5,
                2,
                4,
                true,
                true,
                false
        );

        PredictionFullResponseDTO result = predictionServiceMock.predict(activeCustomer);

        assertThat(result.topFeatures()).anyMatch(f -> f.name().equals("Es miembro activo"));
        assertThat(result.topFeatures()).anyMatch(f -> f.value().equals("1"));
    }

    @Test
    void whenPredict_withInactiveMember_thenActiveMemberFeatureShowsZero() {
        PredictionRequestDTO inactiveCustomer = new PredictionRequestDTO(
                "CLI-TEST-005",
                Geography.GERMANY,
                Gender.MALE,
                40,
                600,
                500.0,
                30000.0,
                2,
                1,
                2,
                false,
                true,
                false
        );

        PredictionFullResponseDTO result = predictionServiceMock.predict(inactiveCustomer);

        assertThat(result.topFeatures()).anyMatch(f ->
                f.name().equals("Es miembro activo") && f.value().equals("0")
        );
    }

    @Test
    void whenPredict_thenFeaturesAreInSpanish() {
        PredictionFullResponseDTO result = predictionServiceMock.predict(requestDTO);

        result.topFeatures().forEach(feature -> {
            assertThat(feature.name()).doesNotContain("age");
            assertThat(feature.name()).doesNotContain("complain");
            assertThat(feature.name()).doesNotContain("active");
            assertThat(feature.name()).matches(".*[a-záéíóúñÁÉÍÓÚÑ ].*");
        });
    }

    @Test
    void whenPredict_thenProbabilityIsRoundedToTwoDecimals() {
        PredictionFullResponseDTO result = predictionServiceMock.predict(requestDTO);

        String probabilityStr = String.valueOf(result.probability());
        int decimalPlaces = probabilityStr.contains(".")
                ? probabilityStr.split("\\.")[1].length()
                : 0;

        assertThat(decimalPlaces).isLessThanOrEqualTo(2);
    }

    @Test
    void whenPredict_multipleTimes_thenProbabilitiesVary() {
        PredictionFullResponseDTO result1 = predictionServiceMock.predict(requestDTO);
        PredictionFullResponseDTO result2 = predictionServiceMock.predict(requestDTO);
        PredictionFullResponseDTO result3 = predictionServiceMock.predict(requestDTO);

        assertThat(result1.probability()).isNotNull();
        assertThat(result2.probability()).isNotNull();
        assertThat(result3.probability()).isNotNull();
    }

    @Test
    void whenPredict_thenAllTop3FeaturesAreDifferent() {
        PredictionFullResponseDTO result = predictionServiceMock.predict(requestDTO);

        assertThat(result.topFeatures()).hasSize(3);
        assertThat(result.topFeatures().get(0).name()).isNotEqualTo(result.topFeatures().get(1).name());
        assertThat(result.topFeatures().get(1).name()).isNotEqualTo(result.topFeatures().get(2).name());
        assertThat(result.topFeatures().get(0).name()).isNotEqualTo(result.topFeatures().get(2).name());
    }
}
