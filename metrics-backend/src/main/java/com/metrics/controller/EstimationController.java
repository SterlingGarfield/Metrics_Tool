package com.metrics.controller;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ProjectEstimation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/estimate")
public class EstimationController {

    private final RecognitionServiceClient recognitionServiceClient;

    public EstimationController(RecognitionServiceClient recognitionServiceClient) {
        this.recognitionServiceClient = recognitionServiceClient;
    }

    @PostMapping("/project")
    public AnalysisResponse estimateProject(@Valid @RequestBody EstimateProjectRequest request) {
        ProjectEstimation estimation = recognitionServiceClient.estimateProject(request);
        return AnalysisResponse.withProjectEstimation(estimation);
    }
}
