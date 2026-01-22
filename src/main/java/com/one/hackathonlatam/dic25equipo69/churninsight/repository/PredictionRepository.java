package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
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
}
