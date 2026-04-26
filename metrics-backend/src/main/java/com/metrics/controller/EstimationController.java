package com.metrics.controller;

import com.metrics.model.request.ManualEstimateRequest;
import com.metrics.model.request.UseCasePointRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.CodeMetricsSummary;
import com.metrics.model.response.DesignMetricsSummary;
import com.metrics.service.EstimationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics/estimation")
public class EstimationController {

    private final EstimationService service;

    public EstimationController(EstimationService service) {
        this.service = service;
    }

    @PostMapping("/manual")
    public AnalysisResponse summarize(@Valid @RequestBody ManualEstimateRequest request) {
        var summary = service.summarize(request);
        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            DesignMetricsSummary.empty(),
            summary,
            service.buildManualRiskFindings(summary),
            List.of(),
            false
        );
    }

    @PostMapping("/use-case-points")
    public AnalysisResponse summarizeUseCasePoints(@Valid @RequestBody UseCasePointRequest request) {
        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            DesignMetricsSummary.empty(),
            service.summarizeUseCasePoints(request),
            List.of(),
            List.of(),
            false
        );
    }
}
