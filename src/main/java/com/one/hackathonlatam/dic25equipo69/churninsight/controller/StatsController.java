package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.HighRiskCustomersResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.StatsResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@Slf4j
@Tag(name = "Statistics", description = "Endpoints para estadísticas y métricas")
public class StatsController {

    private final IStatsService statsService;

    public StatsController(IStatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Obtiene estadísticas generales.
     */
    @GetMapping
    @Operation(
            summary = "Obtener estadísticas generales",
            description = "Retorna métricas agregadas de predicciones de churn para un período específico"
    )
    public ResponseEntity<StatsResponseDTO> getStats(
            @Parameter(description = "Período en días (ej: 7, 30, 90). Null para histórico completo")
            @RequestParam(required = false) Integer period) {

        log.info("GET /api/v1/stats?period={}", period);

        StatsResponseDTO stats = statsService.getStats(period);
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene clientes de alto riesgo.
     */
    @GetMapping("/high-risk")
    @Operation(
            summary = "Obtener clientes de alto riesgo",
            description = "Retorna lista de clientes con mayor probabilidad de churn"
    )
    public ResponseEntity<HighRiskCustomersResponseDTO> getHighRiskCustomers(
            @Parameter(description = "Período en días")
            @RequestParam(defaultValue = "30") Integer period,

            @Parameter(description = "Página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/stats/high-risk?period={}", period);

        HighRiskCustomersResponseDTO customers =
                statsService.getHighRiskCustomers(period, page, size);

        return ResponseEntity.ok(customers);
    }
}
