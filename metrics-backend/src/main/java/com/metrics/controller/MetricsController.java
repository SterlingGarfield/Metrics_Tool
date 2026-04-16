package com.metrics.controller;

import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.service.MetricsAnalysisService;
import com.metrics.util.SourceNormalizationUtils;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/metrics/analyze")
public class MetricsController {

    private final MetricsAnalysisService service;
    private final SourceNormalizationUtils normalizationUtils;

    public MetricsController(MetricsAnalysisService service, SourceNormalizationUtils normalizationUtils) {
        this.service = service;
        this.normalizationUtils = normalizationUtils;
    }

    @PostMapping("/text")
    public AnalysisResponse analyzeText(@Valid @RequestBody TextAnalyzeRequest request) {
        return service.analyzeText(request);
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeFiles(@RequestPart("files") List<MultipartFile> files) throws IOException {
        return service.analyzeSources(normalizationUtils.normalizeFiles(files));
    }

    @PostMapping(value = "/folder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyzeFolder(@RequestPart("files") List<MultipartFile> files, @RequestPart("relativePaths") String relativePaths) throws IOException {
        return service.analyzeSources(normalizationUtils.normalizeFolder(files, relativePaths));
    }
}
