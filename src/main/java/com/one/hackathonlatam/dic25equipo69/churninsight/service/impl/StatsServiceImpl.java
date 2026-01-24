package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PaginationInfo;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.*;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IStatsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StatsServiceImpl implements IStatsService {

    private final PredictionRepository predictionRepository;
    private final PredictionMapper predictionMapper;

    /**
     * Obtiene estadísticas generales de un período.
     *
     * @param period Período en días (ej: 7, 30, 90) o null para todos los datos
     * @return Estadísticas agregadas
     */
    @Override
    @Transactional(readOnly = true)
    public StatsResponseDTO getStats(Integer period) {

        log.info("Calculando estadísticas para período: {} días", period);

        // Calcular rango de fechas
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = period != null
                ? endDate.minusDays(period)
                : LocalDateTime.of(2000, 1, 1, 0, 0); // Fecha muy antigua para "todos"

        // 1. Total de predicciones
        Long totalPredictions = predictionRepository.countByDateRange(startDate, endDate);

        if (totalPredictions == 0) {
            return createEmptyStats(startDate, endDate, period);
        }

        // 2. Contar churns
        Long churnCount = predictionRepository.countChurnByDateRange(startDate, endDate);
        Double churnRate = calculateChurnRate(totalPredictions, churnCount);

        // 3. Promedio de probabilidad
        Double avgProbability = predictionRepository.avgProbabilityByDateRange(startDate, endDate);
        avgProbability = avgProbability != null ? avgProbability : 0.0;

        // Información del período
        PeriodInfo periodInfo = new PeriodInfo(
                startDate,
                endDate,
                period != null ? period + " días" : "Histórico completo"
        );

        log.info("Estadísticas calculadas: total={}, churnRate={}",
                totalPredictions, String.format("%.2f%%", churnRate * 100));

        return new StatsResponseDTO(
                totalPredictions,
                churnRate,
                avgProbability,
                periodInfo
        );
    }

    /**
     * Obtiene clientes de alto riesgo.
     */
    @Override
    @Transactional(readOnly = true)
    public HighRiskCustomersResponseDTO getHighRiskCustomers(Integer period, int page, int size) {
        log.info("Obteniendo clientes de alto riesgo: período={}", period);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = period != null
                ? endDate.minusDays(period)
                : endDate.minusDays(30);

        Pageable pageable = PageRequest.of(page, Math.min(size, 1000));
        Page<Prediction> records = predictionRepository.findHighRiskCustomers(
                startDate,
                endDate,
                pageable
        );

        List<HighRiskCustomer> predictions = records.stream()
                .map(record -> predictionMapper.toHighRiskCustomer(record))
                .collect(Collectors.toList());

        //log.info("Encontrados {} clientes de alto riesgo (total: {})",
        //        customers.size(), totalHighRisk);

        // Información del período
        PeriodInfo periodInfo = new PeriodInfo(
                startDate,
                endDate,
                period != null ? period + " días" : "Histórico completo"
        );

        PaginationInfo pagination = new PaginationInfo(
                records.getNumber(),
                records.getSize(),
                records.getTotalElements(),
                records.getTotalPages()
        );

        return new HighRiskCustomersResponseDTO(
                predictions,
                pagination,
                periodInfo
        );
    }

    private Double calculateChurnRate(Long total, Long churnCount) {
        return total > 0 ? (churnCount.doubleValue() / total.doubleValue()) : 0.0;
    }

    private StatsResponseDTO createEmptyStats(
            LocalDateTime startDate, LocalDateTime endDate, Integer period) {

        return new StatsResponseDTO(
                0L,
                0.0,
                0.0,
                new PeriodInfo(startDate, endDate,
                        period != null ? period + " días" : "Histórico completo")
        );
    }
}
