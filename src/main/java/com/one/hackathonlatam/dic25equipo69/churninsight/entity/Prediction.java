package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prediction", indexes = {
    @Index(name = "idx_prediction_created_at", columnList = "created_at"),
    @Index(name = "idx_prediction_result", columnList = "prediction_result"),
    @Index(name = "idx_prediction_created_at_result", columnList = "created_at, prediction_result")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Prediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;
    
    // Relación con Customer - FK apunta a customer.id (clave primaria)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_fk_id", nullable = false)
    private Customer customer;

    @Column(name = "prediction_result", nullable = false, columnDefinition = "VARCHAR(255)")
    private String predictionResult;

    @Column(name = "probability", nullable = false)
    private Double probability;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Metadata JSON con datos del cliente para análisis estadístico
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_metadata", columnDefinition = "json")
    private String customerMetadata;

}
