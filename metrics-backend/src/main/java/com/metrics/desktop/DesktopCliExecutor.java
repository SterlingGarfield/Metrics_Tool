package com.metrics.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrics.model.SourceInput;
import com.metrics.model.request.DesignAnalyzeRequest;
import com.metrics.model.request.ManualEstimateRequest;
import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.request.UseCasePointRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.CodeMetricsSummary;
import com.metrics.model.response.DesignMetricsSummary;
import com.metrics.model.response.EstimationSummary;
import com.metrics.service.MetricsAnalysisService;
import com.metrics.service.DesignMetricsService;
import com.metrics.service.DesignSuggestionService;
import com.metrics.service.EstimationService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DesktopCliExecutor {

    private final ObjectMapper objectMapper;
    private final MetricsAnalysisService metricsAnalysisService;
    private final DesignMetricsService designMetricsService;
    private final EstimationService estimationService;
    private final DesignSuggestionService designSuggestionService;

    public DesktopCliExecutor(
        ObjectMapper objectMapper,
        MetricsAnalysisService metricsAnalysisService,
        DesignMetricsService designMetricsService,
        EstimationService estimationService,
        DesignSuggestionService designSuggestionService
    ) {
        this.objectMapper = objectMapper;
        this.metricsAnalysisService = metricsAnalysisService;
        this.designMetricsService = designMetricsService;
        this.estimationService = estimationService;
        this.designSuggestionService = designSuggestionService;
    }

    public int execute(InputStream inputStream, OutputStream outputStream) throws IOException {
        DesktopCliResponse response;
        int exitCode = 0;

        try {
            DesktopCliRequest request = objectMapper.readValue(inputStream, DesktopCliRequest.class);
            response = handle(request);
            exitCode = "ok".equals(response.status()) ? 0 : 1;
        } catch (Exception exception) {
            response = DesktopCliResponse.error(resolveErrorMessage(exception));
            exitCode = 1;
        }

        objectMapper.writeValue(outputStream, response);
        return exitCode;
    }

    private DesktopCliResponse handle(DesktopCliRequest request) {
        String command = requireText(request.command(), "Desktop command");

        return switch (command) {
            case "getAppStatus" -> DesktopCliResponse.status(buildStatusPayload());
            case "analyzeText" -> DesktopCliResponse.success(
                metricsAnalysisService.analyzeText(
                    new TextAnalyzeRequest(
                        requireText(request.fileName(), "fileName"),
                        requireText(request.sourceCode(), "sourceCode")
                    )
                )
            );
            case "analyzeSources" -> DesktopCliResponse.success(
                metricsAnalysisService.analyzeSources(mapSources(request.sources()))
            );
            case "analyzeDesign" -> DesktopCliResponse.success(
                buildDesignAnalysisResponse(request)
            );
            case "analyzeEstimation" -> DesktopCliResponse.success(
                buildEstimationAnalysisResponse(request)
            );
            case "analyzeUseCasePoints" -> DesktopCliResponse.success(
                buildUseCasePointAnalysisResponse(request)
            );
            case "suggestDesignMetrics" -> DesktopCliResponse.payload(
                designSuggestionService.suggest(
                    requireText(request.diagramType(), "diagramType"),
                    request.imageName(),
                    requireBytes(request.imageBytes(), "imageBytes")
                )
            );
            case "suggestDesignMetricsFromScan" -> DesktopCliResponse.payload(
                designSuggestionService.suggestFromScan(
                    requireText(request.diagramType(), "diagramType"),
                    new DesignSuggestionService.OcrScanResult(
                        booleanOrFalse(request.ocrAvailable()),
                        listOrEmpty(request.ocrRecognizedText()),
                        doubleOrZero(request.ocrAverageConfidence()),
                        listOrEmpty(request.ocrWarnings()),
                        mapOcrTokens(request.ocrTokens()),
                        request.ocrImageWidth(),
                        request.ocrImageHeight()
                    )
                )
            );
            default -> throw new IllegalArgumentException("Unsupported desktop command: " + command);
        };
    }

    private Map<String, Object> buildStatusPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("mode", "desktop-cli");
        payload.put("transport", "stdio");
        return payload;
    }

    private List<SourceInput> mapSources(List<DesktopCliRequest.DesktopSourcePayload> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("At least one source is required for analyzeSources");
        }

        return sources.stream()
            .map(source -> new SourceInput(
                requireText(source.fileName(), "source.fileName"),
                requireText(source.sourceCode(), "source.sourceCode")
            ))
            .toList();
    }

    private AnalysisResponse buildDesignAnalysisResponse(DesktopCliRequest request) {
        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            designMetricsService.analyze(new DesignAnalyzeRequest(
                requireText(request.diagramType(), "diagramType"),
                intOrZero(request.classCount()),
                intOrZero(request.relationshipCount()),
                intOrZero(request.useCaseCount()),
                intOrZero(request.actorCount()),
                intOrZero(request.flowNodeCount()),
                booleanOrFalse(request.imageProvided()),
                defaultText(request.notes())
            )),
            EstimationSummary.empty(),
            List.of(),
            List.of(),
            false
        );
    }

    private AnalysisResponse buildEstimationAnalysisResponse(DesktopCliRequest request) {
        var summary = estimationService.summarize(new ManualEstimateRequest(
            intOrZero(request.loc()),
            intOrZero(request.staffCount()),
            intOrZero(request.devMonths()),
            doubleOrZero(request.cost())
        ));

        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            DesignMetricsSummary.empty(),
            summary,
            estimationService.buildManualRiskFindings(summary),
            List.of(),
            false
        );
    }

    private AnalysisResponse buildUseCasePointAnalysisResponse(DesktopCliRequest request) {
        return new AnalysisResponse(
            CodeMetricsSummary.empty(),
            DesignMetricsSummary.empty(),
            estimationService.summarizeUseCasePoints(new UseCasePointRequest(
                intOrZero(request.simpleActors()),
                intOrZero(request.averageActors()),
                intOrZero(request.complexActors()),
                intOrZero(request.simpleUseCases()),
                intOrZero(request.averageUseCases()),
                intOrZero(request.complexUseCases()),
                doubleOrDefault(request.technicalComplexityFactor(), 1.0),
                doubleOrDefault(request.environmentalComplexityFactor(), 1.0)
            )),
            List.of(),
            List.of(),
            false
        );
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value;
    }

    private byte[] requireBytes(byte[] value, String fieldName) {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value;
    }

    private int intOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double doubleOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private double doubleOrDefault(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }

    private boolean booleanOrFalse(Boolean value) {
        return value != null && value;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private List<String> listOrEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private List<DesignSuggestionService.OcrToken> mapOcrTokens(List<DesktopCliRequest.OcrTokenPayload> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }

        List<DesignSuggestionService.OcrToken> tokens = new ArrayList<>();
        for (DesktopCliRequest.OcrTokenPayload token : value) {
            if (token == null) {
                continue;
            }

            tokens.add(new DesignSuggestionService.OcrToken(
                token.text(),
                token.confidence() == null ? 0.0 : token.confidence(),
                token.bbox() == null ? List.of() : List.copyOf(token.bbox()),
                token.polygon() == null
                    ? List.of()
                    : token.polygon().stream()
                        .map(point -> point == null ? List.<Double>of() : List.copyOf(point))
                        .toList()
            ));
        }

        return List.copyOf(tokens);
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return exception.getClass().getSimpleName();
    }
}
