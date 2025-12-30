package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para endpoint de predicción de churn
 * POST /api/v1/predict
 */
@RestController
@RequestMapping("/api/v1")
public class PredictionController {

    private final IPredictionService predictionService;

    public PredictionController(IPredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Endpoint principal para predicción de churn
     *
     * @param request Datos del cliente para predicción
     * @return Predicción con probabilidad
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResponseDTO> predict(@Valid @RequestBody PredictionRequestDTO request) {
        PredictionResponseDTO response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }
}
