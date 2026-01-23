package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

    /**
     * Crea una entidad Prediction con los datos básicos.
     * El customerMetadata (JSON) se setea posteriormente en el Service.
     *
     * @param customerId ID de negocio del cliente
     * @param predictionResult Resultado de la predicción (true = cancelará)
     * @param probability Probabilidad de cancelación
     * @return Entidad Prediction lista para persistir
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "customerMetadata", ignore = true)
    @Mapping(target = "featureImportances", ignore = true)
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "predictionResult", target = "predictionResult")
    @Mapping(source = "probability", target = "probability")
    Prediction toEntity(String customerId, Boolean predictionResult, BigDecimal probability);
}
