package com.one.hackathonlatam.dic25equipo69.churninsight.controller;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.BatchPredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.response.batch.*;
import com.one.hackathonlatam.dic25equipo69.churninsight.entity.BatchPredictionJob;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.IBatchPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller para predicciones batch.
 */
@RestController
@RequestMapping("/predict/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch Prediction", description = "Endpoints para predicciones masivas")
public class BatchPredictionController {

    private final IBatchPredictionService batchPredictionService;

    /**
     * Inicia un batch de predicciones desde CSV.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Iniciar predicción batch",
            description = "Sube un archivo CSV con clientes y procesa predicciones asíncronamente"
    )
    public ResponseEntity<BatchPredictionResponseDTO> batchPredict(
            @Parameter(description = "Archivo CSV con datos de clientes")
            @RequestParam("file") MultipartFile csvFile
    ) throws Exception {

        BatchPredictionRequestDTO request = new BatchPredictionRequestDTO(
                "v1",
                csvFile
        );

        BatchPredictionResponseDTO response = batchPredictionService.predictBatch(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el estado de un batch.
     */
    @GetMapping("/{batchId}")
    @Operation(
            summary = "Obtener estado del batch",
            description = "Consulta el progreso y estado de un batch en proceso"
    )
    public ResponseEntity<BatchStatusResponseDTO> getBatchStatus(
            @PathVariable String batchId) {

        log.info("GET /api/v1/predict/batch/{}", batchId);

        BatchPredictionJob job = batchPredictionService.getBatchStatus(batchId);
        BatchStatusResponseDTO response = BatchStatusResponseDTO.from(job);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los resultados de un batch completado.
     */
    @GetMapping("/{batchId}/results")
    @Operation(
            summary = "Obtener resultados del batch",
            description = "Retorna las predicciones de un batch completado con paginación"
    )
    public ResponseEntity<BatchResultsResponseDTO> getBatchResults(
            @PathVariable String batchId,

            @Parameter(description = "Página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Solo resultados exitosos")
            @RequestParam(defaultValue = "false") boolean successOnly,

            @Parameter(description = "Solo resultados con error")
            @RequestParam(defaultValue = "false") boolean errorsOnly) {

        log.info("GET /api/v1/predict/batch/{}/results?page={}&size={}", batchId, page, size);

        BatchResultsResponseDTO response = batchPredictionService.getBatchResults(batchId, page, size, successOnly, errorsOnly);

        return ResponseEntity.ok(response);
    }
}
