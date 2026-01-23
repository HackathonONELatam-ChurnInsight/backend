package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prediction", indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_prediction_created_at", columnList = "created_at"),
        @Index(name = "idx_prediction_result", columnList = "prediction_result"),
        @Index(name = "idx_customer_created", columnList = "customer_id, created_at")
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

    /**
     * Identificador de negocio del cliente (viene del sistema origen).
     * Permite agrupar múltiples predicciones del mismo cliente.
     */
    @Column(name = "customer_id", length = 50, nullable = false)
    @NotNull(message = "El customer_id es obligatorio")
    private String customerId;

    @Column(name = "prediction_result", nullable = false)
    private Boolean predictionResult;

    @Column(name = "probability", precision = 5, scale = 4, nullable = false)
    private BigDecimal probability;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Metadata JSON con snapshot completo de los datos del cliente
     * en el momento de la predicción (para análisis estadístico e histórico).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_metadata", columnDefinition = "json")
    private String customerMetadata;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeatureImportance> featureImportances = new ArrayList<>();
}
