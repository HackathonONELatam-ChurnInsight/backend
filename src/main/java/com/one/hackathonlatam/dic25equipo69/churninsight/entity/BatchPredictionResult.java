package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Resultado individual de una predicción dentro de un batch.
 */
@Entity
@Table(name = "batch_prediction_results", indexes = {
        @Index(name = "idx_batch_id", columnList = "batch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_success")
    private Boolean isSuccess;
}