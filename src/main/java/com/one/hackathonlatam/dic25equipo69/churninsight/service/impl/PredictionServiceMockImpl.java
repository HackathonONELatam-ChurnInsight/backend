package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de predicción
 */
@Profile("dev")
@Service
public class PredictionServiceMockImpl implements IPredictionService {

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        // SIMULACIÓN (MOCK)
        // Simulamos una probabilidad lógica basada en la edad para que parezca real

        String forecast = (request.age() > 50) ? "CHURN" : "NO_CHURN";
        Double probability = (request.age() > 50) ? 0.85 : 0.15;

        return new PredictionResponseDTO(
                UUID.randomUUID().toString(), // ID Simulado
                forecast,
                probability,
                LocalDateTime.now().toString()
        );
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
