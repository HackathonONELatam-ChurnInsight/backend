package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Customer.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Buscar cliente por identificador de negocio único
    Optional<Customer> findByCustomerId(String customerId);

    // Verificar si existe un cliente por customerId
    boolean existsByCustomerId(String customerId);
}
