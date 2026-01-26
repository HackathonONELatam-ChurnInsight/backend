package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.BatchPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.BatchPredictionResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.BatchResultsResponseDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionResult;
import org.springframework.data.domain.Page;

public interface IBatchPredictionService {

    BatchPredictionResponseDTO predictBatch(BatchPredictionRequestDTO request) throws Exception;

    BatchPredictionJob getBatchStatus(String batchId);

    Page<BatchPredictionResult> getBatchResultsPaginated(String batchId, int page, int size, boolean successOnly, boolean errorsOnly);

    BatchResultsResponseDTO getBatchResults(String batchId, int page, int size, boolean successOnly, boolean errorsOnly);
}
