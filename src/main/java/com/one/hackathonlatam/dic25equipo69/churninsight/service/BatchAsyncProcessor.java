package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.client.ModelClientService;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLBatchPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.MLPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.MLPredictionFullResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.MLBatchPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionResult;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.Prediction;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.BatchPredictionJobRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.repository.BatchPredictionResultRepository;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.impl.PredictionPersistenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class BatchAsyncProcessor {

    private final ModelClientService modelClientService;
    private final BatchPredictionJobRepository jobRepository;
    private final BatchPredictionResultRepository resultRepository;
    private final PredictionPersistenceService predictionPersistenceService;

    /**
     * Procesa el batch de forma asíncrona.
     */
    @Async("batchTaskExecutor")
    //@Transactional
    public void processBatchAsync(String batchId, List<PredictionRequestDTO> customers) {

        log.info("Starting async batch processing for: {}", batchId);

        BatchPredictionJob job = jobRepository.findById(batchId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + batchId));

        // Actualizar estado a PROCESSING
        job.setStatus(BatchPredictionJob.BatchStatus.PROCESSING);
        job.setStartTime(LocalDateTime.now());
        jobRepository.save(job);

        int processed = 0;
        int successful = 0;
        int failed = 0;

        int chunkSize = 50;
        try {
            for (int start = 0; start < customers.size(); start += chunkSize) {

                int end = Math.min(start + chunkSize, customers.size());
                List<PredictionRequestDTO> chunkCustomers = customers.subList(start, end);

                // 1. Mapear a ML DTOs
                MLBatchPredictionRequestDTO mlChunkCustomers = new MLBatchPredictionRequestDTO(
                        "v1",
                        chunkCustomers.stream()
                                .map(MLPredictionRequestDTO::from)
                                .toList()
                );

                // 2. Llamar UNA VEZ al modelo
                //List<MLPredictionFullResponseDTO>
                MLBatchPredictionResponseDTO mlChunkPredictions = modelClientService.predictBatch(mlChunkCustomers);

                System.out.println("ok");
                saveSuccessResult2(batchId, start + 1, chunkCustomers, mlChunkPredictions.predictions());

                processed += mlChunkCustomers.customers().size();
                System.out.println(processed);

                updateProgress(batchId, processed, processed, failed);
                //updateProgress(batchId, processed, successful, failed);
            }

            // Actualizar estado final
            job.setStatus(failed == 0 ? BatchPredictionJob.BatchStatus.COMPLETED : BatchPredictionJob.BatchStatus.PARTIAL);
            job.setProcessedRecords(processed);
            job.setSuccessfulPredictions(processed); // successful
            job.setFailedPredictions(failed);
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);

            log.info("Batch {} completed: {} successful, {} failed", batchId, successful, failed);

        } catch (Exception e) {
            log.error("Fatal error processing batch {}: {}", batchId, e.getMessage(), e);

            job.setStatus(BatchPredictionJob.BatchStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setEndTime(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    private void saveSuccessResult(
            String batchId,
            int rowNumber,
            PredictionRequestDTO customer,
            MLPredictionFullResponseDTO mlResponse) {

        try {
            Prediction prediction = predictionPersistenceService.saveMlPredictionWithFeatures(customer, mlResponse);

            BatchPredictionResult result = BatchPredictionResult.builder()
                    .batchId(batchId)
                    .rowNumber(rowNumber)
                    .predictionId(prediction.getId())
                    .isSuccess(true)
                    .build();

            resultRepository.save(result);

        } catch (Exception e) {
            log.error("Error saving success result: {}", e.getMessage());
        }
    }

    private void saveSuccessResult2(
            String batchId,
            int rowNumber,
            List<PredictionRequestDTO> customers,
            List<MLPredictionFullResponseDTO> mlPredictions) {

        try {
            predictionPersistenceService.saveBatch(customers, mlPredictions, batchId, rowNumber);

        } catch (Exception e) {
            log.error("Error saving success result: {}", e.getMessage());
        }
    }

    private void saveErrorResult(
            String batchId,
            int rowNumber,
            PredictionRequestDTO customer,
            String errorMessage) {

        try {
            BatchPredictionResult result = BatchPredictionResult.builder()
                    .batchId(batchId)
                    .rowNumber(rowNumber)
                    .errorMessage(errorMessage)
                    .isSuccess(false)
                    .build();

            resultRepository.save(result);

        } catch (Exception e) {
            log.error("Error saving error result: {}", e.getMessage());
        }
    }

    private void updateProgress(String batchId, int processed, int successful, int failed) {
        jobRepository.findById(batchId).ifPresent(job -> {
            job.setProcessedRecords(processed);
            job.setSuccessfulPredictions(successful);
            job.setFailedPredictions(failed);
            jobRepository.save(job);
        });
    }
}
