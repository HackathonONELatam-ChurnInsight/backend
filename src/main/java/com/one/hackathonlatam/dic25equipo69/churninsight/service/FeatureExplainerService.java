package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio para traducir features técnicas a nombres legibles en español
 * y determinar su impacto en la predicción de churn.
 */
@Service
public class FeatureExplainerService {

    private static final Map<String, String> FEATURE_DISPLAY_NAMES = Map.ofEntries(
            Map.entry("tenure", "Tiempo como cliente"),
            Map.entry("balance", "Saldo en cuenta"),
            Map.entry("geography", "Ubicación geográfica"),
            Map.entry("gender", "Género"),
            Map.entry("age", "Edad"),
            Map.entry("credit_score", "Puntaje de crédito"),
            Map.entry("estimated_salary", "Salario estimado"),
            Map.entry("num_of_products", "Número de productos"),
            Map.entry("has_cr_card", "Tiene tarjeta de crédito"),
            Map.entry("is_active_member", "Es miembro activo"),
            Map.entry("complain", "Tiene quejas"),
            Map.entry("satisfaction_score", "Puntuación de satisfacción")
    );

    /**
     * Traduce el nombre técnico de una feature a su nombre legible en español.
     *
     * @param technicalName nombre técnico de la feature (ej: "age", "balance")
     * @return nombre traducido en español (ej: "Edad", "Saldo en cuenta") o null si el parámetro es null
     */
    public String translateFeatureName(String technicalName) {
        if (technicalName == null) {
            return null;
        }
        return FEATURE_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
    }

    /**
     * Determina la dirección del impacto basado en el valor de importancia del modelo.
     *
     * @param importanceValue valor de importancia retornado por el modelo ML
     * @return POSITIVE si aumenta probabilidad de churn, NEGATIVE si la reduce
     */
    public ImpactDirection determineImpact(Double importanceValue) {
        return importanceValue != null && importanceValue > 0
                ? ImpactDirection.POSITIVE
                : ImpactDirection.NEGATIVE;
    }

    /**
     * Convierte enum ImpactDirection a string en español para respuesta API.
     *
     * @param impact dirección del impacto (POSITIVE o NEGATIVE)
     * @return "positivo" si aumenta churn, "negativo" si lo reduce
     */
    public String impactToString(ImpactDirection impact) {
        return impact == ImpactDirection.POSITIVE ? "positivo" : "negativo";
    }
}
