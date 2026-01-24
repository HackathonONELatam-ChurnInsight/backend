package com.one.hackathonlatam.dic25equipo69.churninsight.client;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.FeatureExtractionException;
import com.one.hackathonlatam.dic25equipo69.churninsight.exception.ModelServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para microservicio Python DS (http://localhost:8000/predict)
 * Implementado con RestTemplate para compatibilidad con Spring Web MVC.
 */
@Profile("prod")
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

    /**
     * Llama al endpoint básico del modelo sin feature importances.
     * Endpoint: POST /predict
     */
    public MLPredictionFullResponseDTO predict(MLPredictionRequestDTO request) {
        log.debug("Enviando petición al modelo DS en la URL: {}", modelServiceUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MLPredictionRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            log.info("🔍 request.class={}, request={}", request.getClass().getSimpleName(), request);

            MLPredictionFullResponseDTO response = restTemplate.postForObject(
                    modelServiceUrl + "/predict",
                    entity,
                    MLPredictionFullResponseDTO.class
            );
            log.debug("Respuesta recibida del modelo: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error al comunicar con el servicio de modelo: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Llama al endpoint del modelo ML solicitando feature importances para explicabilidad.
     * Valida que la respuesta contenga el array feature_importances antes de retornar.
     *
     * @param request datos del cliente en formato ML (snake_case)
     * @return respuesta completa con forecast, probability y feature_importances
     * @throws ModelServiceException si hay error de conexión o la respuesta es vacía
     * @throws FeatureExtractionException si el modelo no retorna feature_importances
     */
    public MLPredictionFullResponseDTO predictWithFeatures(MLPredictionRequestDTO request) {
        log.info("Enviando petición al modelo DS para predicción con explicabilidad");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MLPredictionRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            MLPredictionFullResponseDTO response = restTemplate.postForObject(
                    modelServiceUrl + "/predict",
                    entity,
                    MLPredictionFullResponseDTO.class
            );

            // Validar que la respuesta no sea nula
            if (response == null) {
                throw new ModelServiceException("El modelo retornó una respuesta vacía");
            }

            // Validar que tenga feature importances
            if (response.featureImportances() == null || response.featureImportances().isEmpty()) {
                throw new FeatureExtractionException(
                        "El modelo no retornó feature_importances. Verifique que el endpoint del modelo esté configurado correctamente.");
            }

            log.info("Respuesta completa recibida del modelo: forecast={}, probability={}, features={}",
                    response.forecast(),
                    response.probability(),
                    response.featureImportances().size());
            return response;
        } catch (FeatureExtractionException | ModelServiceException e) {
            // Re-lanzar excepciones propias sin envolver
            throw e;
        } catch (RestClientException e) {
            log.error("Error al comunicarse con el modelo DS: {}", e.getMessage());
            throw new ModelServiceException(
                    "No se pudo conectar con el servicio de modelo ML en: " + modelServiceUrl, e);
        } catch (Exception e) {
            log.error("Error inesperado al llamar al modelo: {}", e.getMessage(), e);
            throw new ModelServiceException("Error inesperado al procesar predicción con explicabilidad", e);
        }
    }

}
