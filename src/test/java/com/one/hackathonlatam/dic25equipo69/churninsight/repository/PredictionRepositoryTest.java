package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para PredictionRepository usando H2 in-memory.
 */
@DataJpaTest
class PredictionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PredictionRepository predictionRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerId("test-customer-001")
                .geography(Geography.SPAIN) // ✅ MAYÚSCULAS
                .age(42)
                .creditScore(650)
                .balance(new BigDecimal("1000.00"))
                .numOfProducts(2)
                .satisfactionScore(3)
                .isActiveMember(true)
                .complain(false)
                .build();
        entityManager.persistAndFlush(testCustomer);
    }

    @Test
    void whenSavePrediction_thenCanRetrieve() {
        // Given
        Prediction prediction = Prediction.builder()
                .customer(testCustomer)
                .predictionResult(true)
                .probability(new BigDecimal("0.8500"))
                .build();

        // When
        Prediction saved = predictionRepository.save(prediction);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomer().getCustomerId()).isEqualTo("test-customer-001");
        assertThat(saved.getPredictionResult()).isTrue();
        assertThat(saved.getProbability()).isEqualByComparingTo("0.8500");
    }

    @Test
    void whenFindByCustomerId_thenReturnsPredictions() {
        // Given
        createPrediction(true, "0.85");
        createPrediction(false, "0.25");

        // When
        List<Prediction> predictions = predictionRepository
                .findByCustomerIdOrderByCreatedAtDesc(testCustomer.getId());

        // Then
        assertThat(predictions).hasSize(2);
        assertThat(predictions.get(0).getCustomer().getId()).isEqualTo(testCustomer.getId());
    }

    @Test
    void whenCountByPredictionResult_thenReturnsCorrectCount() {
        // Given
        createPrediction(true, "0.85");
        createPrediction(true, "0.90");
        createPrediction(false, "0.25");

        // When
        long churnCount = predictionRepository.countByPredictionResult(true);
        long noChurnCount = predictionRepository.countByPredictionResult(false);

        // Then
        assertThat(churnCount).isEqualTo(2L);
        assertThat(noChurnCount).isEqualTo(1L);
    }

    @Test
    void whenFindLatestByCustomerId_thenReturnsNewest() {
        // Given
        createPrediction(true, "0.85");
        entityManager.flush();

        // Esperar para asegurar timestamp diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }

        Prediction latest = createPrediction(false, "0.25");
        entityManager.flush();

        // When
        Prediction result = predictionRepository.findFirstByCustomerIdOrderByCreatedAtDesc(testCustomer.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(latest.getId());
    }

    private Prediction createPrediction(boolean result, String probability) {
        Prediction prediction = Prediction.builder()
                .customer(testCustomer)
                .predictionResult(result)
                .probability(new BigDecimal(probability))
                .build();
        return entityManager.persistAndFlush(prediction);
    }
}
