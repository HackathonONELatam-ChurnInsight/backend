package com.one.hackathonlatam.dic25equipo69.churninsight.dto.request;

public record PredictionRequestDTO(
    int tenure,
    String gender,
    int age,
    int numOfProducts,
    int satisfactionScore,
    Double balance
) {}
