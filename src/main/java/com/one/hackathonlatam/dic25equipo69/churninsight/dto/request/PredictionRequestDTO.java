package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

public record PredictionRequestDTO(
    boolean complain,
    int age,
    Double estimatedSalary,
    int numOfProducts,
    Double balance,
    String gender,
    boolean activeMember
) {}
