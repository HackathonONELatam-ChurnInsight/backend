package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para CustomerRepository usando H2 in-memory.
 */
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void whenSaveCustomer_thenCanRetrieve() {
        // Given
        Customer customer = createTestCustomer("test-001");

        // When
        Customer saved = customerRepository.save(customer);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("test-001");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void whenFindByCustomerId_thenReturnsCustomer() {
        // Given
        Customer customer = createTestCustomer("test-002");
        entityManager.persistAndFlush(customer);

        // When
        Optional<Customer> found = customerRepository.findByCustomerId("test-002");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("test-002");
    }

    @Test
    void whenCustomerIdNotExists_thenReturnsEmpty() {
        // When
        Optional<Customer> found = customerRepository.findByCustomerId("non-existent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void whenExistsByCustomerId_thenReturnsTrue() {
        // Given
        Customer customer = createTestCustomer("test-003");
        entityManager.persistAndFlush(customer);

        // When
        boolean exists = customerRepository.existsByCustomerId("test-003");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void whenNotExistsByCustomerId_thenReturnsFalse() {
        // When
        boolean exists = customerRepository.existsByCustomerId("non-existent");

        // Then
        assertThat(exists).isFalse();
    }

    private Customer createTestCustomer(String customerId) {
        return Customer.builder()
                .customerId(customerId)
                .geography(Geography.SPAIN)
                .gender(Gender.MALE)
                .age(42)
                .creditScore(650)
                .balance(new BigDecimal("1000.00"))
                .estimatedSalary(new BigDecimal("50000.00"))
                .tenure(5)
                .numOfProducts(2)
                .satisfactionScore(3)
                .isActiveMember(true)
                .hasCrCard(true)
                .complain(false)
                .build();
    }
}
