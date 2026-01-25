package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.ImpactDirection;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación mock del servicio de predicción para desarrollo.
 * Retorna predicciones simuladas y las persiste en H2.
 */
@Slf4j
@Profile("dev")
@Service
public class PredictionServiceMockImpl implements IPredictionService {

    private final PredictionPersistenceService persistenceService;

    public PredictionServiceMockImpl(PredictionPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        log.debug("Usando predicción MOCK para desarrollo");

        // Predicción simulada basada en satisfactionScore
        boolean willChurn = request.satisfactionScore() < 3;
        double probability = willChurn ? 0.85 : 0.25;
        String forecast = willChurn ? "Va a cancelar" : "No va a cancelar";

        PredictionResponseDTO response = new PredictionResponseDTO(forecast, probability);

        // Persistir predicción mock
        Prediction savedPrediction = persistenceService.savePrediction(request, response);
        log.info("Predicción MOCK persistida con ID={}", savedPrediction.getId());

        return response;
    }

    /**
     * Genera predicción mock con explicabilidad para desarrollo en H2.
     * Simula el comportamiento del modelo ML basándose en satisfactionScore.
     * Retorna top 3 features simuladas con impacto positivo/negativo.
     *
     * @param request datos del cliente
     * @return predicción mock con forecast, probabilidad y top 3 features simuladas
     */
    @Override
    public PredictionFullResponseDTO predictWithExplanation(PredictionRequestDTO request) {
        log.debug("Usando predicción MOCK CON EXPLICABILIDAD para desarrollo");

        // Predicción simulada basada en satisfactionScore
        boolean willChurn = request.satisfactionScore() < 3;
        double probability = willChurn ? 0.85 : 0.25;
        String forecast = willChurn ? "Va a cancelar" : "No va a cancelar";

        // Generar top 3 features mock basados en los datos del request
        List<FeatureImportanceResponseDTO> topFeatures = generateMockTopFeatures(request, willChurn);

        PredictionFullResponseDTO response = new PredictionFullResponseDTO(
                forecast,
                probability,
                topFeatures
        );

        log.info("Predicción MOCK con explicabilidad generada: forecast={}, features={}",
                forecast, topFeatures.size());

        // NOTA: No persistimos automáticamente aquí porque no tenemos MLPredictionFullResponseDTO real
        // En desarrollo, podrías agregar persistencia mock si lo necesitas

        return response;
    }

    @Override
    public PredictionFullResponseDTO buildFullResponse(Prediction savedPrediction) {
        return null;
    }

    /**
     * Genera top 3 features mock para desarrollo basándose en los datos del request.
     * La lógica simula qué variables serían más relevantes.
     */
    private List<FeatureImportanceResponseDTO> generateMockTopFeatures(
            PredictionRequestDTO request,
            boolean willChurn) {

        List<FeatureImportanceResponseDTO> features = new ArrayList<>();

        // Feature 1: Puntuación de satisfacción (siempre la más importante en nuestro mock)
        features.add(new FeatureImportanceResponseDTO(
                "Puntuación de satisfacción",
                String.valueOf(request.satisfactionScore()),
                request.satisfactionScore() < 3 ? "positivo" : "negativo"
        ));

        // Feature 2: Tiene quejas (si está presente)
        if (request.complain() != null) {
            features.add(new FeatureImportanceResponseDTO(
                    "Tiene quejas",
                    request.complain() ? "Sí" : "No",
                    request.complain() ? "positivo" : "negativo"
            ));
        } else {
            // Si no hay dato de quejas, usar edad
            features.add(new FeatureImportanceResponseDTO(
                    "Edad",
                    String.valueOf(request.age()),
                    request.age() > 50 ? "positivo" : "negativo"
            ));
        }

        // Feature 3: Es miembro activo
        features.add(new FeatureImportanceResponseDTO(
                "Es miembro activo",
                request.isActiveMember() ? "Sí" : "No",
                request.isActiveMember() ? "negativo" : "positivo"
        ));

        log.debug("Generadas {} features mock para explicabilidad", features.size());
        return features;
    }
    // METODO 2: Historial
    @Override
    public List<PredictionResponseDTO> getRecentHistory() {
        // CORRECCIÓN: Agregamos ID y Fecha a los datos falsos
        return List.of(
                new PredictionResponseDTO("MOCK-001", "Va a cancelar", 0.92, LocalDateTime.now().minusDays(1).toString()),
                new PredictionResponseDTO("MOCK-002", "No va a cancelar", 0.12, LocalDateTime.now().minusDays(2).toString()),
                new PredictionResponseDTO("MOCK-003", "Va a cancelar", 0.78, LocalDateTime.now().minusHours(5).toString())
        );
    }

    // METODO 3: Estadísticas con batch
    @Override
    public StatsDTO getStats() {
        // Retornamos datos estáticos para que no falle la compilación
        return new StatsDTO(100L, 25L, 0.25);
    }
}
