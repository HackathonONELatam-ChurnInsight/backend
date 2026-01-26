package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // Un customer puede tener otras predicciones de distintos modelos
    // Relación con Customer - FK apunta a customer.id (clave primaria)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_fk_id", nullable = false)
    private Customer customer;
    
    @Column(name = "prediction_result", nullable = false)
    private Boolean predictionResult;
    
    @Column(name = "probability", precision = 5, scale = 4, nullable = false)
    private BigDecimal probability;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "prediction", fetch = FetchType.LAZY)
    private List<FeatureImportance> featureImportances = new ArrayList<>();

}
