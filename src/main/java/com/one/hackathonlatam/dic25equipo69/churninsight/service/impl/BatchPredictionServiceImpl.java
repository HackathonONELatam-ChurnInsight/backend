package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.BatchPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PaginationInfo;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.PredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.BatchPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.BatchResultItem;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.BatchResultsResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionResult;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.BatchPredictionJobRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.BatchPredictionResultRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.PredictionRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.BatchAsyncProcessor;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IBatchPredictionService;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.ICSVParserService;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IPredictionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class BatchPredictionServiceImpl implements IBatchPredictionService {

    private final ICSVParserService csvParserService;
    private final BatchPredictionJobRepository jobRepository;
    private final BatchPredictionResultRepository resultRepository;
    private final BatchAsyncProcessor batchAsyncProcessor;
    private final IPredictionService predictionService;
    private final PredictionRepository predictionRepository;

    /**
    * Inicia un job de predicción batch.
    */
    @Override
    //@transactional
    public BatchPredictionResponseDTO predictBatch(BatchPredictionRequestDTO request) throws Exception {

        // Validar formato CSV
        csvParserService.validateCSVFormat(request.csvFile());

        // Parsear CSV
        List<PredictionRequestDTO> customers = csvParserService.parseCSV(request.csvFile());

        if (customers.isEmpty()) {
            throw new IllegalArgumentException("CSV file contains no valid records");
        }

        // Crear job
        String batchId = generateBatchId();
        BatchPredictionJob job = BatchPredictionJob.builder()
                .id(batchId)
                .status(BatchPredictionJob.BatchStatus.PENDING)
                .filename(request.csvFile().getOriginalFilename())
                .totalRecords(customers.size())
                .processedRecords(0)
                .successfulPredictions(0)
                .failedPredictions(0)
                .build();

        job = jobRepository.save(job);

        log.info("Batch job created: {} with {} records", batchId, customers.size());

        // Procesar asíncronamente
        batchAsyncProcessor.processBatchAsync(batchId, customers);

        BatchPredictionResponseDTO response = new BatchPredictionResponseDTO(
                job.getId(),
                job.getStatus().name(),
                job.getTotalRecords(),
                "/api/v1/predict/batch/" + job.getId(),
                "/api/v1/predict/batch/" + job.getId() + "/results",
                job.getStartTime()
        );

        return response;
    }

    /**
     * Obtiene el estado de un job.
     */
    @Override
    //@Transactional(readOnly = true)
    public BatchPredictionJob getBatchStatus(String batchId) {
        return jobRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));
    }

    /**
     * Obtiene los resultados de un batch.
     */
    /*@Transactional(readOnly = true)
    public List<BatchPredictionResult> getBatchResults(String batchId) {
        return resultRepository.findByBatchIdAndIsSuccessTrue(batchId);
    }*/

    /**
     * Obtiene los resultados con paginación.
     */
    //@Transactional(readOnly = true)
    public Page<BatchPredictionResult> getBatchResultsPaginated(
            String batchId,
            int page,
            int size,
            boolean successOnly,
            boolean errorsOnly) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 1000));

        if (successOnly) {
            return resultRepository.findByBatchIdAndIsSuccessTrueOrderByRowNumber(batchId, pageable);
        } else if (errorsOnly) {
            return resultRepository.findByBatchIdAndIsSuccessFalseOrderByRowNumber(batchId, pageable);
        } else {
            return resultRepository.findByBatchIdOrderByRowNumber(batchId, pageable);
        }
    }

    public BatchResultsResponseDTO getBatchResults(String batchId, int page, int size, boolean successOnly, boolean errorsOnly) {

        // Verificar que el batch existe
        BatchPredictionJob job = getBatchStatus(batchId);

        // Obtener resultados con paginación
        Page<BatchPredictionResult> resultsPage =
                getBatchResultsPaginated(
                        batchId, page, size, successOnly, errorsOnly
                );

        // Convertir a DTO
        List<BatchResultItem> items = resultsPage.getContent().stream()
                .map(result -> {
                    Long predictionId = result.getPredictionId();

                    Prediction prediction = predictionRepository.findById(predictionId).orElse(null);

                    PredictionFullResponseDTO predictionFullResponseDTO = predictionService.buildFullResponse(prediction);

                    return BatchResultItem.builder()
                            .rowNumber(result.getRowNumber())
                            .prediction(predictionFullResponseDTO)
                            .isSuccess(result.getIsSuccess())
                            .errorMessage(result.getErrorMessage())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        PaginationInfo pagination = new PaginationInfo(
                resultsPage.getNumber(),
                resultsPage.getSize(),
                resultsPage.getTotalElements(),
                resultsPage.getTotalPages()
        );

        BatchResultsResponseDTO response = new BatchResultsResponseDTO(
                batchId,
                job.getStatus().name(),
                items,
                items.size(),
                job.getSuccessfulPredictions(),
                job.getFailedPredictions(),
                pagination
        );

        return response;
    }

    private String generateBatchId() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("batch-%s-%s", timestamp, uuid);
    }
}
