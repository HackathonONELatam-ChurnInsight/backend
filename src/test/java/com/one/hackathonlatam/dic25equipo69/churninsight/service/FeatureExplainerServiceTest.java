package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para FeatureExplainerService.
 * Valida traducción de features, determinación de impacto y conversión a strings.
 */
class FeatureExplainerServiceTest {

    private FeatureExplainerService featureExplainerService;

    @BeforeEach
    void setUp() {
        featureExplainerService = new FeatureExplainerService();
    }

    // ========== TESTS DE TRADUCCIÓN ==========

    @Test
    void whenTranslateFeatureName_withAge_thenReturnsEdad() {
        // When
        String result = featureExplainerService.translateFeatureName("age");

        // Then
        assertThat(result).isEqualTo("Edad");
    }

    @Test
    void whenTranslateFeatureName_withBalance_thenReturnsSaldoEnCuenta() {
        // When
        String result = featureExplainerService.translateFeatureName("balance");

        // Then
        assertThat(result).isEqualTo("Saldo en cuenta");
    }

    @Test
    void whenTranslateFeatureName_withGeography_thenReturnsUbicacionGeografica() {
        // When
        String result = featureExplainerService.translateFeatureName("geography");

        // Then
        assertThat(result).isEqualTo("Ubicación geográfica");
    }

    @Test
    void whenTranslateFeatureName_withComplain_thenReturnsTieneQuejas() {
        // When
        String result = featureExplainerService.translateFeatureName("complain");

        // Then
        assertThat(result).isEqualTo("Tiene quejas");
    }

    @Test
    void whenTranslateFeatureName_withSatisfactionScore_thenReturnsPuntuacionDeSatisfaccion() {
        // When
        String result = featureExplainerService.translateFeatureName("satisfaction_score");

        // Then
        assertThat(result).isEqualTo("Puntuación de satisfacción");
    }

    @Test
    void whenTranslateFeatureName_withUnknownFeature_thenReturnsOriginalName() {
        // Given
        String unknownFeature = "unknown_feature_xyz";

        // When
        String result = featureExplainerService.translateFeatureName(unknownFeature);

        // Then
        assertThat(result).isEqualTo(unknownFeature);
    }

    @Test
    void whenTranslateFeatureName_withNullInput_thenReturnsNull() {
        // When
        String result = featureExplainerService.translateFeatureName(null);

        // Then
        assertThat(result).isNull();
    }

    // ========== TESTS DE DETERMINACIÓN DE IMPACTO ==========

    @Test
    void whenDetermineImpact_withPositiveValue_thenReturnsPositive() {
        // Given
        Double positiveValue = 0.45;

        // When
        ImpactDirection result = featureExplainerService.determineImpact(positiveValue);

        // Then
        assertThat(result).isEqualTo(ImpactDirection.POSITIVE);
    }

    @Test
    void whenDetermineImpact_withNegativeValue_thenReturnsNegative() {
        // Given
        Double negativeValue = -0.32;

        // When
        ImpactDirection result = featureExplainerService.determineImpact(negativeValue);

        // Then
        assertThat(result).isEqualTo(ImpactDirection.NEGATIVE);
    }

    @Test
    void whenDetermineImpact_withZeroValue_thenReturnsNegative() {
        // Given
        Double zeroValue = 0.0;

        // When
        ImpactDirection result = featureExplainerService.determineImpact(zeroValue);

        // Then
        assertThat(result).isEqualTo(ImpactDirection.NEGATIVE);
    }

    @Test
    void whenDetermineImpact_withVerySmallPositiveValue_thenReturnsPositive() {
        // Given
        Double smallPositiveValue = 0.001;

        // When
        ImpactDirection result = featureExplainerService.determineImpact(smallPositiveValue);

        // Then
        assertThat(result).isEqualTo(ImpactDirection.POSITIVE);
    }

    // ========== TESTS DE CONVERSIÓN A STRING ==========

    @Test
    void whenImpactToString_withPositive_thenReturnsPositivo() {
        // When
        String result = featureExplainerService.impactToString(ImpactDirection.POSITIVE);

        // Then
        assertThat(result).isEqualTo("positivo");
    }

    @Test
    void whenImpactToString_withNegative_thenReturnsNegativo() {
        // When
        String result = featureExplainerService.impactToString(ImpactDirection.NEGATIVE);

        // Then
        assertThat(result).isEqualTo("negativo");
    }

    // ========== TEST DE INTEGRACIÓN (flujo completo) ==========

    @Test
    void whenCompleteFlow_fromFeatureToDisplayString_thenWorksCorrectly() {
        // Given - Simulamos feature "age" con importance value positivo
        String technicalName = "age";
        Double importanceValue = 0.25;

        // When
        String displayName = featureExplainerService.translateFeatureName(technicalName);
        ImpactDirection impact = featureExplainerService.determineImpact(importanceValue);
        String impactString = featureExplainerService.impactToString(impact);

        // Then
        assertThat(displayName).isEqualTo("Edad");
        assertThat(impact).isEqualTo(ImpactDirection.POSITIVE);
        assertThat(impactString).isEqualTo("positivo");
    }
}
