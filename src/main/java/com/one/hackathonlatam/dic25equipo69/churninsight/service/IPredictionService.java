package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;

/**
 * Servicio para realizar predicciones de churn.
 */
public interface IPredictionService {

    /**
     * Realiza predicción de churn.
     * Retorna forecast, probability y top_features (si el modelo las retorna).
     *
     * @param request Datos del cliente para predicción
     * @return Respuesta con predicción y features opcionales
     */
    PredictionFullResponseDTO predict(PredictionRequestDTO request);
}
