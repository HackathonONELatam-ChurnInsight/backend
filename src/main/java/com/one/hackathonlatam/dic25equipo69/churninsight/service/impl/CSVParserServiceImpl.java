package com.one.hackathonlatam.dic25equipo69.churninsight.service.impl;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Gender;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.enums.Geography;
import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import com.one.hackathonlatam.dic25equipo69.churninsight.service.ICSVParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para parsear archivos CSV.
 */
@Service
@Slf4j
public class CSVParserServiceImpl implements ICSVParserService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final int MAX_RECORDS = 5000;

    /**
     * Parsea un archivo CSV y convierte a lista de Customer.
     */
    @Override
    public List<PredictionRequestDTO> parseCSV(MultipartFile file) throws Exception {

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed (%d MB)", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        log.info("Parsing CSV file: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());

        List<PredictionRequestDTO> customers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

            try (CSVParser csvParser = new CSVParser(reader, csvFormat)) {

                int rowNumber = 0;
                for (CSVRecord record : csvParser) {
                    rowNumber++;

                    if (rowNumber > MAX_RECORDS) {
                        log.warn("Maximum records limit reached: {}", MAX_RECORDS);
                        break;
                    }

                    try {
                        PredictionRequestDTO customer = parseRecord(record);
                        customers.add(customer);
                    } catch (Exception e) {
                        //logger.error("Error parsing row {}: {}", rowNumber, e.getMessage());
                        throw new IllegalArgumentException(
                                String.format("Error in row %d: %s", rowNumber, e.getMessage())
                        );
                    }
                }
            }
        }

        log.info("Successfully parsed {} customer records", customers.size());
        return customers;
    }

    @Override
    public void validateCSVFormat(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        // Validar headers
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            validateHeaders(headerLine);
        }
    }

    private void validateHeaders(String headerLine) {
        String[] requiredHeaders = {
                "geography", "gender", "age", "creditScore", "balance",
                "estimatedSalary", "tenure", "numOfProducts", "satisfactionScore",
                "isActiveMember", "hasCrCard", "complain"
        };

        String headerLower = headerLine.toLowerCase();
        for (String header : requiredHeaders) {
            if (!headerLower.contains(header.toLowerCase())) {
                throw new IllegalArgumentException("Missing required header: " + header);
            }
        }
    }

    private PredictionRequestDTO parseRecord(CSVRecord record) {
        return PredictionRequestDTO.builder()
                .customerId(record.isMapped("customerId")?  record.get("customerId").trim() : null)
                .geography(Geography.from(record.get("geography").trim()))
                .gender(Gender.from(record.get("gender").trim()))
                .age(Integer.parseInt(record.get("age").trim()))
                .creditScore(Integer.parseInt(record.get("creditScore").trim()))
                .balance(Double.parseDouble(record.get("balance").trim()))
                .estimatedSalary(Double.parseDouble(record.get("estimatedSalary").trim()))
                .tenure(Integer.parseInt(record.get("tenure").trim()))
                .numOfProducts(Integer.parseInt(record.get("numOfProducts").trim()))
                .satisfactionScore(Integer.parseInt(record.get("satisfactionScore").trim()))
                .isActiveMember(Boolean.parseBoolean(record.get("isActiveMember").trim()))
                .hasCrCard(Boolean.parseBoolean(record.get("hasCrCard").trim()))
                .complain(Boolean.parseBoolean(record.get("complain").trim()))
                .build();
    }
}
