package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prediction_customer")
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

    // Identificador de negocio del cliente
    @Column(name = "customer_id", length = 50, nullable = false)
    private String customerId;

    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "geography", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Geography geography;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "gender", length = 10, nullable = true)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "age", nullable = false)
    private Integer age;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "credit_score", nullable = false)
    private Integer creditScore;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "tenure", nullable = true)
    private Integer tenure;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "estimated_salary", precision = 15, scale = 2, nullable = true)
    private BigDecimal estimatedSalary;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "num_of_products", nullable = false)
    private Integer numOfProducts;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "satisfaction_score", nullable = false)
    private Integer satisfactionScore;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "is_active_member", nullable = false)
    private Boolean isActiveMember;
    
    // Campo opcional según PredictionRequestDTO
    @Column(name = "has_cr_card", nullable = true)
    private Boolean hasCrCard;
    
    // Campo obligatorio según PredictionRequestDTO
    @Column(name = "complain", nullable = false)
    private Boolean complain;
}
