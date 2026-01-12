package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
}
