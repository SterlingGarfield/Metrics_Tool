package com.metrics.controller;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.DiagramAnalysisResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/api/design/analyze")
public class DesignAnalysisController {

    private final RecognitionServiceClient recognitionServiceClient;

    public DesignAnalysisController(RecognitionServiceClient recognitionServiceClient) {
        this.recognitionServiceClient = recognitionServiceClient;
    }

    @PostMapping("/structured")
    public AnalysisResponse analyzeStructured(@Valid @RequestBody StructuredDiagramAnalyzeRequest request) {
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
}
