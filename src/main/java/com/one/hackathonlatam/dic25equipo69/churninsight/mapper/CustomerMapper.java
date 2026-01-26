package com.one.hackathonlatam.dic25equipo69.churninsight.mapper;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

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
    Customer toEntity(PredictionRequestDTO dto, String customerId);


    Customer toEntity(PredictionRequestDTO request);

    default String map(String customerId) {
        if (customerId == null) {
            return UUID.randomUUID().toString();
        }
        return customerId;
    }
}
