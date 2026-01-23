package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "prediction",
        indexes = {
                @Index(name = "idx_customer_id", columnList = "customer_id"),
                @Index(name = "idx_prediction_created_at", columnList = "created_at"),
                @Index(name = "idx_prediction_result", columnList = "prediction_result"),
                @Index(name = "idx_customer_created", columnList = "customer_id, created_at")
        },
        uniqueConstraints = {
                // ✅ NUEVO: Constraint único para prevenir duplicados absolutos
                @UniqueConstraint(
                        name = "uk_customer_metadata_hash",
                        columnNames = {"customer_id", "metadata_hash"}
                )
        }
)
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

    @Column(name = "customer_id", length = 50, nullable = false)
    @NotNull(message = "El customer_id es obligatorio")
    private String customerId;

    // ✅ NUEVO: Hash SHA-256 de la metadata para detectar duplicados
    @Column(name = "metadata_hash", length = 64, nullable = false)
    @NotNull(message = "El metadata_hash es obligatorio")
    private String metadataHash;

    @Column(name = "prediction_result", nullable = false)
    private Boolean predictionResult;

    @Column(name = "probability", precision = 5, scale = 4, nullable = false)
    private BigDecimal probability;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_metadata", columnDefinition = "json")
    private String customerMetadata;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeatureImportance> featureImportances = new ArrayList<>();

    // ✅ NUEVO: Método para calcular el hash automáticamente al setear metadata
    public void setCustomerMetadata(String customerMetadata) {
        this.customerMetadata = customerMetadata;
        this.metadataHash = calculateMetadataHash(customerMetadata);
    }

    // ✅ NUEVO: Cálculo del hash SHA-256
    private String calculateMetadataHash(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            throw new IllegalArgumentException("customerMetadata no puede ser null o vacío");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(metadata.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular metadata hash: SHA-256 no disponible", e);
        }
    }
}
