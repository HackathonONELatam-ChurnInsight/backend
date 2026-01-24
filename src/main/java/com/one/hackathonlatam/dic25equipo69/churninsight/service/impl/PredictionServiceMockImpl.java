package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementación Mock del servicio de predicción para desarrollo/testing.
 * Genera predicciones aleatorias sin llamar al modelo ML real.
 */
@Service
@Profile("dev")
@Slf4j
public class PredictionServiceMockImpl implements IPredictionService {

    private final Random random = new Random();

    @Override
    public PredictionFullResponseDTO predict(PredictionRequestDTO request) {
        log.info("MOCK Generando predicción mock para customerId {}", request.customerId());

        int forecast;
        double probability;

        if (request.satisfactionScore() != null && request.satisfactionScore() <= 2) {
            forecast = 1;  // Va a cancelar
            probability = 0.85;
        } else {
            forecast = 0;  // No cancela
            probability = 0.25;
        }

        String forecastText = forecast == 1 ? "Va a cancelar" : "No va a cancelar";

        // Generar features mock (Top 3)
        List<FeatureImportanceResponseDTO> topFeatures = generateMockFeatures(request);

        log.info("MOCK Predicción generada forecast={}, probability={}, features={}",
                forecastText, probability, topFeatures.size());

        return new PredictionFullResponseDTO(forecastText, probability, topFeatures);
    }


    /**
     * Genera features mock basadas en los datos del request.
     */
    private List<FeatureImportanceResponseDTO> generateMockFeatures(PredictionRequestDTO request) {
        List<FeatureImportanceResponseDTO> features = new ArrayList<>();

        // Feature 1: Age (siempre la más importante en el mock)
        features.add(new FeatureImportanceResponseDTO(
                "Edad",
                String.valueOf(request.age()),
                request.age() > 40 ? "positivo" : "negativo"
        ));

        // Feature 2: IsActiveMember
        features.add(new FeatureImportanceResponseDTO(
                "Es miembro activo",
                request.isActiveMember() ? "1" : "0",
                request.isActiveMember() ? "negativo" : "positivo"
        ));

        // Feature 3: Complain
        features.add(new FeatureImportanceResponseDTO(
                "Tiene quejas",
                request.complain() ? "1" : "0",
                request.complain() ? "positivo" : "negativo"
        ));

        return features;
    }
}
