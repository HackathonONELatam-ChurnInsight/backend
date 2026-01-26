package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchPredictionJobRepository extends JpaRepository<BatchPredictionJob, String> {

    /**
     * Buscar jobs por estado.
     */
    List<BatchPredictionJob> findByStatus(BatchPredictionJob.BatchStatus status);

    /**
     * Buscar jobs en un rango de fechas.
     */
    List<BatchPredictionJob> findByStartTimeBetweenOrderByStartTimeDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
