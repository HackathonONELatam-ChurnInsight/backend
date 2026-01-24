package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.HighRiskCustomersResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.StatsResponseDTO;

public interface IStatsService {

    StatsResponseDTO getStats(Integer period);

    HighRiskCustomersResponseDTO getHighRiskCustomers(Integer period, int page, int size);
}
