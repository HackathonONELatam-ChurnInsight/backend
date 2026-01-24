package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de persistencia de FeatureImportance.
 */
@Repository
public interface FeatureImportanceRepository extends JpaRepository<FeatureImportance, Long> {

    /**
     * Encuentra todas las feature importances asociadas a una predicción específica.
     * Ordenadas por rank_position de forma ascendente (1, 2, 3...).
     *
     * @param predictionId ID de la predicción
     * @return Lista de feature importances ordenadas por ranking
     */
    @Query("SELECT f FROM FeatureImportance f WHERE f.prediction.id = :predictionId ORDER BY f.rankPosition ASC")
    List<FeatureImportance> findByPredictionIdOrderByRankPosition(@Param("predictionId") Long predictionId);

    /**
     * Encuentra las top N feature importances de una predicción específica.
     * Útil para obtener solo las 3 más relevantes.
     *
     * @param predictionId ID de la predicción
     * @param limit Número máximo de features a retornar (ej: 3)
     * @return Lista de las top N feature importances
     */
    @Query(value = "SELECT * FROM prediction_feature_importance WHERE prediction_record_id = :predictionId " +
            "ORDER BY rank_position ASC LIMIT :limit", nativeQuery = true)
    List<FeatureImportance> findTopNByPredictionId(@Param("predictionId") Long predictionId,
                                                   @Param("limit") int limit);

    /**
     * Elimina todas las feature importances asociadas a una predicción específica.
     * Útil para limpieza o actualización de datos.
     *
     * @param predictionId ID de la predicción
     */
    void deleteByPredictionId(Long predictionId);
}
