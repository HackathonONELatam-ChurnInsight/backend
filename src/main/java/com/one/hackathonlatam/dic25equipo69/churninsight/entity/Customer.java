package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Customer {
    
    // Clave primaria técnica - autoincremental
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;
    
    // Identificador de negocio del cliente - único
    @Column(name = "customer_id", length = 50, nullable = false, unique = true)
    @NotNull(message = "El customer_id es obligatorio")
    private String customerId;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "geography", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "El campo 'geography' es obligatorio")
    private Geography geography;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "gender", length = 10, nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "age", nullable = false)
    @NotNull(message = "El campo 'age' es obligatorio")
    @Min(value = 18, message = "La edad mínima permitida es 18 años")
    @Max(value = 100, message = "La edad máxima permitida es 100 años")
    private Integer age;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "credit_score", nullable = false)
    @NotNull(message = "El campo 'creditScore' es obligatorio")
    @Min(value = 100, message = "El creditScore mínimo es 100")
    @Max(value = 1000, message = "El creditScore máximo es 1000")
    private Integer creditScore;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "tenure", nullable = true)
    @Min(value = 0, message = "La antigüedad (tenure) mínima es 0")
    @Max(value = 20, message = "La antigüedad (tenure) máxima es 20 años")
    private Integer tenure;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    @NotNull(message = "El campo 'balance' es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El balance no puede ser negativo")
    private BigDecimal balance;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "estimated_salary", precision = 15, scale = 2, nullable = true)
    @DecimalMin(value = "0.0", inclusive = true, message = "El salario estimado no puede ser negativo")
    private BigDecimal estimatedSalary;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "num_of_products", nullable = false)
    @NotNull(message = "El campo 'numOfProducts' es obligatorio")
    @Min(value = 1, message = "El número mínimo de productos es 1")
    @Max(value = 4, message = "El número máximo de productos es 4")
    private Integer numOfProducts;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "satisfaction_score", nullable = false)
    @NotNull(message = "El campo 'satisfactionScore' es obligatorio")
    @Min(value = 1, message = "El satisfactionScore mínimo es 1")
    @Max(value = 5, message = "El satisfactionScore máximo es 5")
    private Integer satisfactionScore;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "is_active_member", nullable = false)
    @NotNull(message = "El campo 'isActiveMember' es obligatorio")
    private Boolean isActiveMember;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "has_cr_card", nullable = true)
    private Boolean hasCrCard;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "complain", nullable = false)
    @NotNull(message = "El campo 'complain' es obligatorio")
    private Boolean complain;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prediction> predictions;
}
