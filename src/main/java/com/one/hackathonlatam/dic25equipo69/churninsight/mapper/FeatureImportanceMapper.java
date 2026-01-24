package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.FeatureImportanceResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLFeatureImportanceDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.FeatureImportance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureImportanceMapper {

    @Mapping(target = "value", source = "featureValue")
    @Mapping(target = "impact", source = "impactDirection")
    FeatureImportanceResponseDTO toFeatureImportanceResponseDTO(FeatureImportance featureImportance);

}
