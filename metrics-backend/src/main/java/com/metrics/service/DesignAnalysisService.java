package com.metrics.service;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DesignAnalysisService {

    private final RecognitionServiceClient recognitionServiceClient;

    public DesignAnalysisService(RecognitionServiceClient recognitionServiceClient) {
        this.recognitionServiceClient = recognitionServiceClient;
    }

    public AnalysisResponse analyzeStructured(StructuredDiagramAnalyzeRequest request) {
        return AnalysisResponse.withDiagramAnalysis(recognitionServiceClient.analyzeStructured(request));
    }

    public AnalysisResponse analyzeImage(String diagramType, MultipartFile file) {
        if (diagramType == null || file == null) {
            throw new IllegalArgumentException("diagramType and file are required");
        }
        return AnalysisResponse.withDiagramAnalysis(recognitionServiceClient.analyzeImage(diagramType, file));
    }
}
