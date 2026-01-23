package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // ========== Métodos por customerId ==========

    List<Prediction> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Page<Prediction> findByCustomerId(String customerId, Pageable pageable);

    Prediction findTopByCustomerIdOrderByCreatedAtDesc(String customerId);

    long countByCustomerId(String customerId);

    // ✅ NUEVO: Buscar predicción duplicada exacta
    Optional<Prediction> findByCustomerIdAndMetadataHash(String customerId, String metadataHash);

    // ========== Métodos por fechas ==========

    List<Prediction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Prediction> findByCustomerIdAndCreatedAtBetween(
            String customerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ========== Métodos para Estadísticas (StatsService) ==========

    /**
     * Cuenta predicciones por resultado (churn o no churn).
     * Usado por StatsService.
     */
    long countByPredictionResult(Boolean predictionResult);

    /**
     * Calcula el promedio de probabilidad de todas las predicciones.
     * Usado por StatsService.
     */
    @Query("SELECT AVG(p.probability) FROM Prediction p")
    Double findAverageProbability();

    /**
     * Cuenta predicciones positivas (churn = true) en un rango de fechas.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.predictionResult = true AND p.createdAt BETWEEN :startDate AND :endDate")
    long countChurnPredictionsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ========== Otros métodos útiles ==========

    List<Prediction> findByPredictionResult(Boolean predictionResult);

    @Query("SELECT p FROM Prediction p ORDER BY p.createdAt DESC")
    List<Prediction> findTopNByOrderByCreatedAtDesc(Pageable pageable);
}
