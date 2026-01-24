package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PredictionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PredictionRepository predictionRepository;

    @Test
    void whenSavePrediction_thenCanRetrieve() {
        Prediction prediction = Prediction.builder()
                .customerId("CLI-TEST-001")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .customerMetadata("{\"age\":42,\"geography\":\"Spain\"}")
                .build();

        Prediction saved = predictionRepository.save(prediction);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("CLI-TEST-001");
        assertThat(saved.getPredictionResult()).isTrue();
        assertThat(saved.getProbability()).isEqualByComparingTo("0.8500");
        assertThat(saved.getMetadataHash()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void whenSavePredictionWithFeatures_thenFeaturesArePersisted() {
        FeatureImportance feature1 = FeatureImportance.builder()
                .name("complain")
                .displayName("Tiene quejas")
                .featureValue("1")
                .impactDirection(ImpactDirection.POSITIVE)
                .rankPosition(1)
                .build();

        FeatureImportance feature2 = FeatureImportance.builder()
                .name("age")
                .displayName("Edad")
                .featureValue("42")
                .impactDirection(ImpactDirection.NEGATIVE)
                .rankPosition(2)
                .build();

        Prediction prediction = Prediction.builder()
                .customerId("CLI-TEST-002")
                .predictionResult(true)
                .probability(new BigDecimal("0.7500"))
                .customerMetadata("{\"age\":42,\"complain\":true}")
                .build();

        feature1.setPrediction(prediction);
        feature2.setPrediction(prediction);
        prediction.getFeatureImportances().add(feature1);
        prediction.getFeatureImportances().add(feature2);

        Prediction saved = predictionRepository.save(prediction);
        entityManager.flush();
        entityManager.clear();

        prediction.setCustomerMetadata("{\"test\":\"features\"}");


        Prediction retrieved = predictionRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getFeatureImportances()).hasSize(2);
        assertThat(retrieved.getFeatureImportances().get(0).getName()).isEqualTo("complain");
        assertThat(retrieved.getFeatureImportances().get(1).getName()).isEqualTo("age");
    }

    @Test
    void whenFindByCustomerId_thenReturnsPredictions() {
        createPrediction("CLI-TEST-003", true, "0.85");
        createPrediction("CLI-TEST-003", false, "0.25");
        createPrediction("CLI-TEST-004", true, "0.90");

        List<Prediction> predictions = predictionRepository
                .findByCustomerIdOrderByCreatedAtDesc("CLI-TEST-003");

        assertThat(predictions).hasSize(2);
        assertThat(predictions.get(0).getCustomerId()).isEqualTo("CLI-TEST-003");
        assertThat(predictions.get(1).getCustomerId()).isEqualTo("CLI-TEST-003");
    }

    @Test
    void whenFindByPredictionResult_thenReturnsFiltered() {
        createPrediction("CLI-TEST-005", true, "0.85");
        createPrediction("CLI-TEST-006", true, "0.90");
        createPrediction("CLI-TEST-007", false, "0.25");

        List<Prediction> churnPredictions = predictionRepository.findByPredictionResult(true);
        List<Prediction> noChurnPredictions = predictionRepository.findByPredictionResult(false);

        assertThat(churnPredictions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(noChurnPredictions).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void whenFindByCreatedAtBetween_thenReturnsInRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        createPrediction("CLI-TEST-008", true, "0.85");

        List<Prediction> predictions = predictionRepository
                .findByCreatedAtBetween(start, end);

        assertThat(predictions).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void whenCountByPredictionResult_thenReturnsCount() {
        long initialChurnCount = predictionRepository.countByPredictionResult(true);
        long initialNoChurnCount = predictionRepository.countByPredictionResult(false);

        createPrediction("CLI-TEST-009", true, "0.85");
        createPrediction("CLI-TEST-010", true, "0.90");
        createPrediction("CLI-TEST-011", false, "0.25");

        long churnCount = predictionRepository.countByPredictionResult(true);
        long noChurnCount = predictionRepository.countByPredictionResult(false);

        assertThat(churnCount).isEqualTo(initialChurnCount + 2);
        assertThat(noChurnCount).isEqualTo(initialNoChurnCount + 1);
    }

    @Test
    void whenFindLatestByCustomerId_thenReturnsNewest() {
        createPrediction("CLI-TEST-012", true, "0.85");
        entityManager.flush();

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        Prediction latest = createPrediction("CLI-TEST-012", false, "0.25");
        entityManager.flush();

        Prediction result = predictionRepository.findTopByCustomerIdOrderByCreatedAtDesc("CLI-TEST-012");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(latest.getId());
        assertThat(result.getPredictionResult()).isFalse();
    }

    @Test
    void whenFindByCustomerIdAndMetadataHash_thenReturnsDuplicate() {
        String customerId = "CLI-TEST-013";
        String metadata = "{\"age\":42,\"geography\":\"Spain\"}";

        Prediction prediction = Prediction.builder()
                .customerId(customerId)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .customerMetadata(metadata)
                .build();

        prediction.setCustomerMetadata("{\"test\":\"duplicate\"}");

        Prediction saved = predictionRepository.save(prediction);
        entityManager.flush();

        Optional<Prediction> duplicate = predictionRepository
                .findByCustomerIdAndMetadataHash(customerId, saved.getMetadataHash());

        assertThat(duplicate).isPresent();
        assertThat(duplicate.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void whenSaveDuplicatePrediction_thenHashIsCalculatedAutomatically() {
        String metadata = "{\"age\":42,\"geography\":\"Spain\",\"complain\":true}";

        Prediction prediction1 = Prediction.builder()
                .customerId("CLI-TEST-014")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .customerMetadata(metadata)
                .build();

        prediction1.setCustomerMetadata("{\"test\":\"hashtest\"}");

        Prediction prediction2 = Prediction.builder()
                .customerId("CLI-TEST-014")
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .customerMetadata(metadata)
                .build();


        Prediction saved1 = predictionRepository.save(prediction1);
        entityManager.flush();

        assertThat(saved1.getMetadataHash()).isNotNull();
        assertThat(prediction2.getMetadataHash()).isEqualTo(saved1.getMetadataHash());
    }

    @Test
    void whenCountByCustomerId_thenReturnsCorrectCount() {
        createPrediction("CLI-TEST-015", true, "0.85");
        createPrediction("CLI-TEST-015", false, "0.25");
        createPrediction("CLI-TEST-015", true, "0.90");

        long count = predictionRepository.countByCustomerId("CLI-TEST-015");

        assertThat(count).isEqualTo(3);
    }

    @Test
    void whenFindAverageProbability_thenReturnsCorrectAverage() {
        createPrediction("CLI-TEST-016", true, "0.80");
        createPrediction("CLI-TEST-017", false, "0.20");

        Double avgProbability = predictionRepository.findAverageProbability();

        assertThat(avgProbability).isNotNull();
        assertThat(avgProbability).isGreaterThan(0.0);
    }

    @Test
    void whenFindTopNByOrderByCreatedAtDesc_thenReturnsLimitedResults() {
        createPrediction("CLI-TEST-018", true, "0.85");
        createPrediction("CLI-TEST-019", false, "0.25");
        createPrediction("CLI-TEST-020", true, "0.90");

        List<Prediction> top2 = predictionRepository
                .findTopNByOrderByCreatedAtDesc(PageRequest.of(0, 2));

        assertThat(top2).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void whenCountChurnPredictionsBetween_thenReturnsCorrectCount() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        createPrediction("CLI-TEST-021", true, "0.85");
        createPrediction("CLI-TEST-022", false, "0.25");

        long churnCount = predictionRepository.countChurnPredictionsBetween(start, end);

        assertThat(churnCount).isGreaterThanOrEqualTo(1);
    }

    private Prediction createPrediction(String customerId, boolean result, String probability) {
        Prediction prediction = Prediction.builder()
                .customerId(customerId)
                .predictionResult(result)
                .probability(new BigDecimal(probability))
                .build();

        // Usar setCustomerMetadata para calcular automáticamente el hash
        prediction.setCustomerMetadata("{\"test\":\"data\"}");

        return entityManager.persistAndFlush(prediction);
    }

}
