package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

public record PaginationInfo(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {}
