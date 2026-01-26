package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchPredictionResultRepository extends JpaRepository<BatchPredictionResult, Long> {

    /**
     * Buscar resultados por batch ID.
     */
    Page<BatchPredictionResult> findByBatchIdOrderByRowNumber(String batchId, Pageable pageable);

    /**
     * Buscar solo resultados exitosos de un batch con paginación.
     */
    Page<BatchPredictionResult> findByBatchIdAndIsSuccessTrueOrderByRowNumber(
            String batchId,
            Pageable pageable
    );

    /**
     * Buscar solo errores de un batch con paginación.
     */
    Page<BatchPredictionResult> findByBatchIdAndIsSuccessFalseOrderByRowNumber(
            String batchId,
            Pageable pageable
    );

    /**
     * Buscar solo resultados exitosos de un batch.
     */
    List<BatchPredictionResult> findByBatchIdAndIsSuccessTrue(String batchId);

    /**
     * Buscar solo errores de un batch.
     */
    List<BatchPredictionResult> findByBatchIdAndIsSuccessFalse(String batchId);

    /**
     * Contar resultados de un batch.
     */
    Long countByBatchId(String batchId);
}
