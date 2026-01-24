package com.one.hackathonlatam.dic25equipo69.churninsight.service;

import com.one.hackathonlatam.dic25equipo69.churninsight.dto.request.PredictionRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICSVParserService {

    List<PredictionRequestDTO> parseCSV(MultipartFile file) throws Exception;

    void validateCSVFormat(MultipartFile file) throws Exception;

}
