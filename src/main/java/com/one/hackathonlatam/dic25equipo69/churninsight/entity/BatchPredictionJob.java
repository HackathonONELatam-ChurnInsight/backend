package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un job de predicción batch.
 */
@Entity
@Table(name = "batch_prediction_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPredictionJob {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status;

    @Column(name = "filename", length = 255)
    private String filename;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "processed_records")
    private Integer processedRecords;

    @Column(name = "successful_predictions")
    private Integer successfulPredictions;

    @Column(name = "failed_predictions")
    private Integer failedPredictions;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = BatchStatus.PENDING;
        }
    }

    public enum BatchStatus {
        PENDING,      // Creado, esperando procesamiento
        PROCESSING,   // En proceso
        COMPLETED,    // Completado exitosamente
        FAILED,       // Falló completamente
        PARTIAL       // Completado con algunos errores
    }

    /**
     * Calcula la duración del procesamiento en segundos.
     */
    public Long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * Calcula el porcentaje de progreso.
     */
    public Double getProgressPercentage() {
        if (totalRecords == null || totalRecords == 0) {
            return 0.0;
        }
        return (processedRecords != null)
                ? (processedRecords.doubleValue() / totalRecords.doubleValue()) * 100.0
                : 0.0;
    }
}

