package com.one.hackathonlatam.dic25equipo69.churninsight.repository;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Para obtener las últimas 10 predicciones (para la tabla del dashboard)
    List<Prediction> findTop10ByOrderByCreatedAtDesc();

    // Query optimizada para contar cuántos "Va a cancelar" (o el string que devuelva Python) hay
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.predictionResult = 'Va a cancelar' OR p.predictionResult = 'CHURN'")
    long countChurnRisks();
}