package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.CustomerMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.mapper.PredictionMapper;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para persistencia automática de predicciones y gestión del historial.
 */
@Slf4j
@Service
public class PredictionPersistenceService {

    private final PredictionRepository predictionRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PredictionMapper predictionMapper;
    private final ObjectMapper objectMapper;

    public PredictionPersistenceService(
            PredictionRepository predictionRepository,
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            PredictionMapper predictionMapper,
            ObjectMapper objectMapper) {
        this.predictionRepository = predictionRepository;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.predictionMapper = predictionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Guarda automáticamente una predicción junto con los datos del cliente.
     */
    @Transactional
    public Prediction savePrediction(PredictionRequestDTO request, PredictionResponseDTO response) {
        log.debug("Iniciando persistencia de predicción");

        // 1. Obtener o crear Customer usando mapper
        Customer customer = getOrCreateCustomer(request);

        // 2. Serializar metadata como JSON
        String metadata = serializeMetadata(request);

        // 3. Determinar resultado de predicción
        Boolean predictionResult = determinePredictionResult(response.forecast());

        // 4. Crear Prediction usando mapper
        Prediction prediction = predictionMapper.toEntity(
                customer,
                predictionResult,
                BigDecimal.valueOf(response.probability()),
                metadata
        );

        Prediction saved = predictionRepository.save(prediction);
        log.info("Predicción guardada: ID={}, Customer={}, Result={}, Probability={}",
                saved.getId(), customer.getCustomerId(), predictionResult, response.probability());

        return saved;
    }

    @Transactional
    public Prediction saveMlPrediction(PredictionRequestDTO request, MLPredictionResponseDTO mlResponse) {
        log.debug("Persistencia ML real: forecast={}, probability={}",
                mlResponse.forecast(), mlResponse.probability());
        PredictionResponseDTO response = mlResponse.toPredictionResponseDTO();
        return savePrediction(request, response);  // Reusa lógica
    }

    /**
     * Obtiene o crea un Customer usando MapStruct.
     */
    private Customer getOrCreateCustomer(PredictionRequestDTO request) {
        String customerId = UUID.randomUUID().toString();

        return customerRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    // Usa el mapper en vez de builder manual
                    Customer newCustomer = customerMapper.toEntity(request, customerId);
                    return customerRepository.save(newCustomer);
                });
    }

    private String serializeMetadata(PredictionRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar metadata: {}", e.getMessage());
            return "{}";
        }
    }

    private Boolean determinePredictionResult(String forecast) {
        if (forecast == null) return false;
        String lower = forecast.toLowerCase();
        return lower.startsWith("va a cancelar");
    }


    @Transactional(readOnly = true)
    public List<Prediction> getCustomerHistory(Long customerId) {
        log.debug("Consultando historial para customer ID: {}", customerId);
        return predictionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Object[] getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo estadísticas entre {} y {}", startDate, endDate);
        return predictionRepository.getStatisticsByDateRange(startDate, endDate);
    }
}
