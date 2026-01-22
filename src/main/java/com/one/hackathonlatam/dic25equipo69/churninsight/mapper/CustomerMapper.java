package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para conversión entre PredictionRequestDTO y Customer.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "geography", source = "dto.geography")
    @Mapping(target = "gender", source = "dto.gender")
    @Mapping(target = "age", source = "dto.age")
    @Mapping(target = "creditScore", source = "dto.creditScore")
    @Mapping(target = "balance", source = "dto.balance")
    @Mapping(target = "estimatedSalary", source = "dto.estimatedSalary")
    @Mapping(target = "tenure", source = "dto.tenure")
    @Mapping(target = "numOfProducts", source = "dto.numOfProducts")
    @Mapping(target = "satisfactionScore", source = "dto.satisfactionScore")
    @Mapping(target = "isActiveMember", source = "dto.isActiveMember")
    @Mapping(target = "hasCrCard", source = "dto.hasCrCard")
    @Mapping(target = "complain", source = "dto.complain")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "predictions", ignore = true)
    Customer toEntity(PredictionRequestDTO dto, String customerId);
}
