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

/**
 * Repositorio para operaciones CRUD y consultas de Prediction.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Historial de predicciones de un cliente
    List<Prediction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Filtrar por resultado de predicción
    List<Prediction> findByPredictionResult(Boolean predictionResult);

    // Consultas por rango de fechas
    List<Prediction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Filtro combinado: resultado + rango de fechas
    List<Prediction> findByPredictionResultAndCreatedAtBetween(
            Boolean predictionResult,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Contadores para estadísticas
    Long countByPredictionResult(Boolean predictionResult);

    // Última predicción de un cliente
    @Query("SELECT p FROM Prediction p WHERE p.customer.id = :customerId ORDER BY p.createdAt DESC LIMIT 1")
    Prediction findLatestByCustomerId(@Param("customerId") Long customerId);

    // Estadísticas agregadas por rango de fechas
    @Query("""
        SELECT COUNT(*) as total,
               SUM(CASE WHEN p.predictionResult = true THEN 1 ELSE 0 END) as churnCount,
               SUM(CASE WHEN p.predictionResult = false THEN 1 ELSE 0 END) as noChurnCount
        FROM Prediction p 
        WHERE p.createdAt BETWEEN :startDate AND :endDate
    """)
    Object[] getStatisticsByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    // Para obtener las últimas 10 predicciones (para la tabla del dashboard)
    List<Prediction> findTop10ByOrderByCreatedAtDesc();

    /**
     * Contar total de predicciones en un período.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Contar predicciones de churn en un período.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE " +
            "p.predictionResult = true AND " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    Long countChurnByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calcular promedio de probabilidad en un período.
     */
    @Query("SELECT AVG(p.probability) FROM Prediction p WHERE " +
            "p.createdAt BETWEEN :startDate AND :endDate")
    Double avgProbabilityByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Obtener clientes de alto riesgo ordenados por probabilidad.
     */
    @Query("SELECT p FROM Prediction p WHERE " +
            "p.probability >= 0.7 AND " +// p.riskLevel = 'HIGH'
            "p.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY p.probability DESC")
    Page<Prediction> findHighRiskCustomers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    // Query optimizada para contar cuántos "Va a cancelar" (o el string que devuelva Python) hay
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.predictionResult = 'Va a cancelar' OR p.predictionResult = 'CHURN'")
    long countChurnRisks();
}