package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * Mapper para conversión de predicciones.
 */
@Mapper(componentModel = "spring")
public interface PredictionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "predictionResult", source = "predictionResult")
    @Mapping(target = "probability", source = "probability")
    @Mapping(target = "customerMetadata", source = "metadata")
    @Mapping(target = "createdAt", ignore = true)
    Prediction toEntity(Customer customer, Boolean predictionResult, BigDecimal probability, String metadata);
}
