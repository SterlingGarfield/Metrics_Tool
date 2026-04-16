package com.metrics.controller;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.DiagramAnalysisResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/api/design/analyze")
public class DesignAnalysisController {

    private final RecognitionServiceClient recognitionServiceClient;

    public DesignAnalysisController(RecognitionServiceClient recognitionServiceClient) {
        this.recognitionServiceClient = recognitionServiceClient;
    }

    @PostMapping(value = "/structured", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeStructured(
        @RequestPart("file") MultipartFile file,
        @RequestPart("diagramType") @NotBlank String diagramType
    ) {
        StructuredDiagramAnalyzeRequest request = toStructuredRequest(file, diagramType);
        DiagramAnalysisResponse diagramAnalysis = recognitionServiceClient.analyzeStructured(request);
        return AnalysisResponse.withDiagramAnalysis(diagramAnalysis);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeImage(
        @RequestPart("file") MultipartFile file,
        @RequestPart("diagramType") @NotBlank String diagramType
    ) {
        DiagramAnalysisResponse diagramAnalysis = recognitionServiceClient.analyzeImage(diagramType, file);
        return AnalysisResponse.withDiagramAnalysis(diagramAnalysis);
    }

    private StructuredDiagramAnalyzeRequest toStructuredRequest(MultipartFile file, String diagramType) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Structured diagram file must not be empty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Structured diagram file name is required");
        }
        String suffix = extractSuffix(fileName);
        try {
            String source = new String(file.getBytes(), StandardCharsets.UTF_8);
            return new StructuredDiagramAnalyzeRequest(diagramType, fileName, suffix, source);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read structured diagram file", ex);
        }
    }

    private String extractSuffix(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "txt";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
