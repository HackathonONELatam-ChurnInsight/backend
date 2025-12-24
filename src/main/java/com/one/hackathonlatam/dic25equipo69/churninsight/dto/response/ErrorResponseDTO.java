package com.one.hackathonlatam.dic25equipo69.churninsight.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseDTO {
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
}
