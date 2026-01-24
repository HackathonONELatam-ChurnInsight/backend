package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PaginationInfo;

import java.util.List;

/**
 * Respuesta con clientes de alto riesgo.
 */
public record HighRiskCustomersResponseDTO(
        List<HighRiskCustomer> predictions,
        PaginationInfo pagination,
        PeriodInfo period
) {
}

