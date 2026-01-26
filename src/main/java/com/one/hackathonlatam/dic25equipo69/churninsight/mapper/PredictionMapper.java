package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.stats.HighRiskCustomer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * Mapper para conversión de predicciones.
 */
@Mapper(componentModel = "spring", uses = {FeatureImportanceMapper.class})
public interface PredictionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "predictionResult", source = "predictionResult")
    @Mapping(target = "probability", source = "probability")
    @Mapping(target = "createdAt", ignore = true)
    Prediction toEntity(Customer customer, Boolean predictionResult, BigDecimal probability, String metadata);

    @Mapping(target = "predictionResult", source = "forecast")
    Prediction toEntity(MLPredictionFullResponseDTO mlResponse);

    default Boolean map(Integer forecast) {
        if (forecast == null) {
            return null;
        }
        return forecast == 1;
    }

    default BigDecimal map(Double probability) {
        if (probability == null) {
            return null;
        }
        return BigDecimal.valueOf(probability);
    }

    @Mapping(target = "forecast", source = "predictionResult")
    @Mapping(target = "topFeatures", source = "featureImportances")
    PredictionFullResponseDTO toPredictionFullResponseDTO(Prediction prediction);

    default String map(Boolean predictionResult) {
        if (predictionResult == null) {
            return null;
        }
        return predictionResult
                ? "Va a cancelar"
                : "No va a cancelar";
    }

    @Mapping(target = "customerId", source = "prediction.customer.customerId")
    @Mapping(target = "predictedAt", source = "createdAt")
    @Mapping(target = "geography", source = "prediction.customer.geography")
    HighRiskCustomer toHighRiskCustomer(Prediction prediction);
}
