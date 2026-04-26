package com.metrics.controller;

import com.metrics.model.request.DesignAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.CodeMetricsSummary;
import com.metrics.model.response.DesignSuggestionResponse;
import com.metrics.model.response.EstimationSummary;
import com.metrics.service.DesignMetricsService;
import com.metrics.service.DesignSuggestionService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics/design")
public class DesignMetricsController {

    private final DesignMetricsService service;
    private final DesignSuggestionService suggestionService;

    public DesignMetricsController(DesignMetricsService service, DesignSuggestionService suggestionService) {
        this.service = service;
        this.suggestionService = suggestionService;
    }

    @PostMapping("/analyze")
    public AnalysisResponse analyze(@Valid @RequestBody DesignAnalyzeRequest request) {
        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            service.analyze(request),
            EstimationSummary.empty(),
            List.of(),
            List.of(),
            false
        );
    }

    @PostMapping(value = "/suggest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DesignSuggestionResponse suggest(
        @RequestParam("diagramType") String diagramType,
        @RequestPart("image") MultipartFile image
    ) throws IOException {
        return suggestionService.suggest(
            diagramType,
            image.getOriginalFilename(),
            image.getBytes()
        );
    }
}
