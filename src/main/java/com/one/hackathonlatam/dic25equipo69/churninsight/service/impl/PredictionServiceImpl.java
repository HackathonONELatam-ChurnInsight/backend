package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.*;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.FeatureExtractionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.FeatureImportanceRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.FeatureExplainerService;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("prod")
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;
    private final PredictionPersistenceService persistenceService; // Lógica del Equipo
    private final FeatureImportanceRepository featureImportanceRepository;
    private final FeatureExplainerService featureExplainerService;
    private final PredictionRepository predictionRepository;
    // CustomerRepository eliminado porque persistenceService ya maneja el guardado

    public PredictionServiceImpl(
            ModelClientService modelClientService,
            PredictionPersistenceService persistenceService,
            FeatureImportanceRepository featureImportanceRepository,
            FeatureExplainerService featureExplainerService,
            PredictionRepository predictionRepository) {
        this.modelClientService = modelClientService;
        this.persistenceService = persistenceService;
        this.featureImportanceRepository = featureImportanceRepository;
        this.featureExplainerService = featureExplainerService;
        this.predictionRepository = predictionRepository;
    }

    @Override
    @Transactional
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        log.info("Iniciando predicción integrada para cliente...");
        try {
            // 1. Convertir request
            MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

            // 2. Llamar al modelo DS (Python)
            MLPredictionResponseDTO mlResponse = modelClientService.predict(mlRequest);

            // 3. PERSISTENCIA (CÓDIGO DEL EQUIPO)
            // Usamos su servicio para guardar Boolean/BigDecimal en BD. Limpio y sin duplicados.
            Prediction savedPrediction = persistenceService.saveMlPrediction(request, mlResponse);

            log.info("Predicción persistida exitosamente con ID={}", savedPrediction.getId());

            // 4. TRADUCCIÓN (TU CÓDIGO DE BACKUP)
            // Adaptamos la respuesta del equipo al formato de tu Frontend.

            String forecastText = (savedPrediction.getPredictionResult() != null && savedPrediction.getPredictionResult())
                    ? "Va a cancelar"
                    : "No va a cancelar";

            Double probabilityValue = (savedPrediction.getProbability() != null)
                    ? savedPrediction.getProbability().doubleValue()
                    : 0.0;

            // Retornamos TU DTO limpio
            return new PredictionResponseDTO(
                    savedPrediction.getCustomer().getCustomerId(),
                    forecastText,
                    probabilityValue,
                    savedPrediction.getCreatedAt() != null ? savedPrediction.getCreatedAt().toString() : LocalDateTime.now().toString()
            );

        } catch (Exception e) {
            log.error("Error al procesar predicción: {}", e.getMessage(), e);
            throw e; // Lanza la excepción tal cual (asumiendo RuntimeException)
        }
    }

    @Override
    public PredictionFullResponseDTO predictWithExplanation(PredictionRequestDTO request){
        log.info("Iniciando predicción con explicabilidad...");
        try {
            MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);
            MLPredictionFullResponseDTO mlFullResponse = modelClientService.predictWithFeatures(mlRequest);
            Prediction savedPrediction = persistenceService.saveMlPredictionWithFeatures(request, mlFullResponse);

            // Corregido: Eliminada la doble llamada a buildFullResponse
            return buildFullResponse(savedPrediction);

        } catch (Exception e) {
            log.error("Error al procesar predicción con explicabilidad: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<PredictionResponseDTO> getRecentHistory() {
        return predictionRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(p -> {
                    // Traducción BD (Boolean) -> Frontend (String)
                    String forecast = (p.getPredictionResult() != null && p.getPredictionResult())
                            ? "Va a cancelar"
                            : "No va a cancelar";

                    Double prob = (p.getProbability() != null)
                            ? p.getProbability().doubleValue()
                            : 0.0;

                    return new PredictionResponseDTO(
                            p.getCustomer().getCustomerId(),
                            forecast,
                            prob,
                            p.getCreatedAt().toString()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public StatsDTO getStats() {
        long total = predictionRepository.count();
        // OPTIMIZACIÓN: Usamos el método nativo del repositorio en vez de cargar toda la lista en memoria
        long riskCount = predictionRepository.countByPredictionResult(true);
        double rate = total > 0 ? (double) riskCount / total : 0.0;

        return new StatsDTO(total, riskCount, rate);
    }

    public PredictionFullResponseDTO buildFullResponse(Prediction savedPrediction) {
        try {
            List<FeatureImportance> topFeatures = featureImportanceRepository.findTopNByPredictionId(
                    savedPrediction.getId(), 3);

            if (topFeatures == null || topFeatures.isEmpty()) {
                throw new FeatureExtractionException(
                        "No se encontraron feature importances para ID: " + savedPrediction.getId());
            }

            List<FeatureImportanceResponseDTO> topFeaturesDTO = topFeatures.stream()
                    .map(feature -> new FeatureImportanceResponseDTO(
                            feature.getDisplayName(),
                            feature.getFeatureValue(),
                            featureExplainerService.impactToString(feature.getImpactDirection())
                    ))
                    .collect(Collectors.toList());

            String forecast = Boolean.TRUE.equals(savedPrediction.getPredictionResult()) ? "Va a cancelar" : "No va a cancelar";

            return new PredictionFullResponseDTO(
                    forecast,
                    savedPrediction.getProbability().doubleValue(),
                    topFeaturesDTO
            );
        } catch (FeatureExtractionException e) {
            throw e;
        } catch (Exception e) {
            throw new FeatureExtractionException("Error al construir respuesta", e);
        }
    }
}