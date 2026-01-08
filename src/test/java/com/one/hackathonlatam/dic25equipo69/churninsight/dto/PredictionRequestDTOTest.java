package com.one.hackathonlatam.dic25equipo69.churninsight.dto;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private PredictionRequestDTO buildValidRequest() {
        return new PredictionRequestDTO(
                "France", "Male", 30, 600, 50000.0, 100000.0,
                5, 2, 4, true, true, false
        );
    }

    @Test
    void validRequest_ShouldPassAllConstraints() {
        Set<ConstraintViolation<PredictionRequestDTO>> violations = validator.validate(buildValidRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void geographyOutsideAllowedValues_ShouldRaisePatternViolation() {
        PredictionRequestDTO invalid = new PredictionRequestDTO(
                "Brazil", "Male", 30, 600, 50000.0, 100000.0,
                5, 2, 4, true, true, false
        );

        assertThat(validator.validate(invalid))
                .extracting(ConstraintViolation::getMessage)
                .contains("El campo 'geography' debe ser: France, Spain o Germany");
    }

    @Test
    void ageBelowMinimum_ShouldRaiseConstraintViolation() {
        PredictionRequestDTO invalid = new PredictionRequestDTO(
                "France", "Male", 17, 600, 50000.0, 100000.0,
                5, 2, 4, true, true, false
        );

        assertThat(validator.validate(invalid))
                .extracting(ConstraintViolation::getMessage)
                .contains("La edad mínima permitida es 18 años");
    }

    @Test
    void nullActiveMember_ShouldRaiseNotNullViolation() {
        PredictionRequestDTO invalid = new PredictionRequestDTO(
                "France", "Male", 30, 600, 50000.0, 100000.0,
                5, 2, 4, null, true, false
        );

        assertThat(validator.validate(invalid))
                .extracting(ConstraintViolation::getMessage)
                .contains("El campo 'isActiveMember' es obligatorio");
    }
}
