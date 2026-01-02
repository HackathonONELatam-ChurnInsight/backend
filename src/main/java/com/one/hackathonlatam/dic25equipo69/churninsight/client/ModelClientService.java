package com.one.hackathonlatam.dic25equipo69.churninsight.client;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para microservicio Python DS (http://localhost:8000/predict)
 * Implementado con RestTemplate para compatibilidad con Spring Web MVC.
 */
@Slf4j
@Component
public class ModelClientService {

    private final RestTemplate restTemplate;
    private final String modelServiceUrl;

    public ModelClientService(
            @Value("${model.service.url}") String modelServiceUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.modelServiceUrl = modelServiceUrl;
    }

    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        log.debug("Enviando petición al modelo DS en la URL: {}", modelServiceUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PredictionRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            PredictionResponseDTO response = restTemplate.postForObject(
                    modelServiceUrl,
                    entity,
                    PredictionResponseDTO.class
            );
            log.debug("Respuesta recibida del modelo: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error al comunicar con el servicio de modelo: {}", e.getMessage());
            throw e;
        }
    }
}