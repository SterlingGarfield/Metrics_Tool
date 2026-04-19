package com.metrics.controller;

import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ProjectEstimation;
import com.metrics.service.EstimationService;
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

    private final EstimationService estimationService;

    public EstimationController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @PostMapping("/project")
    public AnalysisResponse estimateProject(@Valid @RequestBody EstimateProjectRequest request) {
        ProjectEstimation estimation = estimationService.estimate(request);
        return AnalysisResponse.withProjectEstimation(estimation);
    }
}
