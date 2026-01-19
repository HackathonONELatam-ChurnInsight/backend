package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio para traducir features técnicos a nombres legibles y determinar impacto.
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
     * Si no existe traducción, retorna el nombre original.
     */
    public String translateFeatureName(String technicalName) {
        return FEATURE_DISPLAY_NAMES.getOrDefault(technicalName, technicalName);
    }

    /**
     * Determina la dirección del impacto basado en el valor de importancia.
     * Positivo si importanceValue > 0, Negativo si <= 0
     */
    public ImpactDirection determineImpact(Double importanceValue) {
        return importanceValue > 0 ? ImpactDirection.POSITIVE : ImpactDirection.NEGATIVE;
    }

    /**
     * Convierte ImpactDirection a string para el DTO de respuesta.
     */
    public String impactToString(ImpactDirection impact) {
        return impact == ImpactDirection.POSITIVE ? "positivo" : "negativo";
    }
}
