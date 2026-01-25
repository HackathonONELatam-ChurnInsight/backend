package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.StatsDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Customer;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.CustomerRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import org.springframework.context.annotation.Profile;
import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de predicción
 */
@Profile("prod")
@Service
public class PredictionServiceImpl implements IPredictionService {

    private final ModelClientService modelClientService;
    private final PredictionRepository predictionRepository;
    private final CustomerRepository customerRepository;

    public PredictionServiceImpl(ModelClientService modelClientService,
                                 PredictionRepository predictionRepository,
                                 CustomerRepository customerRepository) {
        this.modelClientService = modelClientService;
        this.predictionRepository = predictionRepository;
        this.customerRepository = customerRepository;
    }
    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO request) {

        MLPredictionRequestDTO mlRequest = MLPredictionRequestDTO.from(request);

        // Llama al cliente del modelo DS (Python)
        MLPredictionResponseDTO mlResponse = modelClientService.predict(mlRequest);

        PredictionResponseDTO responseDTO = mlResponse.toPredictionResponseDTO();

        // 2. Guardar Cliente en BD (Mapeo manual simple)
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());

        customer.setGeography(request.geography());
        customer.setGender(request.gender());
        customer.setAge(request.age());
        customer.setCreditScore(request.creditScore());
        customer.setBalance(BigDecimal.valueOf(request.balance())); // Conversión

        if (request.estimatedSalary() != null) {
            customer.setEstimatedSalary(BigDecimal.valueOf(request.estimatedSalary()));
        }

        customer.setTenure(request.tenure());
        customer.setNumOfProducts(request.numOfProducts());
        customer.setSatisfactionScore(request.satisfactionScore());
        customer.setIsActiveMember(request.isActiveMember());
        customer.setHasCrCard(request.hasCrCard());
        customer.setComplain(request.complain());

        customer = customerRepository.save(customer); // Guardamos y obtenemos el ID generado

        // 3. Guardar Predicción en BD
        Prediction prediction = new Prediction();
        prediction.setPredictionResult(responseDTO.forecast());
        prediction.setProbability(responseDTO.probability());
        prediction.setCustomer(customer); // Relación

        predictionRepository.save(prediction);

        // RETORNO ACTUALIZADO CON DATOS REALES
        return new PredictionResponseDTO(
                customer.getCustomerId(), // ID Real (UUID)
                responseDTO.forecast(),
                responseDTO.probability(),
                prediction.getCreatedAt() != null ? prediction.getCreatedAt().toString() : LocalDateTime.now().toString()
        );
    }

    @Override
    public List<PredictionResponseDTO> getRecentHistory() {
        return predictionRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(p -> new PredictionResponseDTO(
                        p.getCustomer().getCustomerId(), // Sacamos el ID de la relación con Customer
                        p.getPredictionResult(),
                        p.getProbability(),
                        p.getCreatedAt().toString() // Fecha real de la BD
                ))
                .collect(Collectors.toList());
    }

    @Override
    public StatsDTO getStats() { // Cambiado a StatsDTO
        long total = predictionRepository.count();
        long riskCount = predictionRepository.countChurnRisks();
        double rate = total > 0 ? (double) riskCount / total : 0.0;

        return new StatsDTO(total, riskCount, rate);
    }
}