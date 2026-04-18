package com.metrics.service;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.exception.FeatureNotReadyException;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DesignAnalysisService {

    private static final String IMAGE_PIPELINE_NOT_READY_MESSAGE = "Image recognition pipeline is not implemented yet";

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
        throw new FeatureNotReadyException(IMAGE_PIPELINE_NOT_READY_MESSAGE);
    }
}
