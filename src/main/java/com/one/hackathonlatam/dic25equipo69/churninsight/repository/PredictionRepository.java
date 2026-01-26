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

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Historial de predicciones
    List<Prediction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Contar por resultado (Usado para Stats eficiente)
    long countByPredictionResult(Boolean predictionResult);

    // Última predicción (Corregido para evitar error de sintaxis JPQL LIMIT)
    Prediction findFirstByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Para la tabla del dashboard
    List<Prediction> findTop10ByOrderByCreatedAtDesc();

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

    // Consultas de Rango de Fechas
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.predictionResult = true AND p.createdAt BETWEEN :startDate AND :endDate")
    Long countChurnByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(p.probability) FROM Prediction p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Double avgProbabilityByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Prediction p WHERE p.probability >= 0.7 AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.probability DESC")
    Page<Prediction> findHighRiskCustomers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}