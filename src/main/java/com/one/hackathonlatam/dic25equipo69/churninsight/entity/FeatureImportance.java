package com.one.hackathonlatam.dic25equipo69.churninsight.entity;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feature_importance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FeatureImportance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    // FK a la tabla prediction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_record_id", nullable = false)
    private Prediction prediction;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "feature_value", nullable = false, length = 100)
    private String featureValue;

    @Column(name = "impact_direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImpactDirection impactDirection;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;
}
