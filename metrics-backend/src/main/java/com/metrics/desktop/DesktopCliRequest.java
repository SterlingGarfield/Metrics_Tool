package com.metrics.desktop;

import java.util.List;

public record DesktopCliRequest(
    String command,
    String fileName,
    String sourceCode,
    List<DesktopSourcePayload> sources,
    String diagramType,
    Integer classCount,
    Integer relationshipCount,
    Integer useCaseCount,
    Integer actorCount,
    Integer flowNodeCount,
    Boolean imageProvided,
    String notes,
    Integer loc,
    Integer staffCount,
    Integer devMonths,
    Double cost,
    Integer simpleActors,
    Integer averageActors,
    Integer complexActors,
    Integer simpleUseCases,
    Integer averageUseCases,
    Integer complexUseCases,
    Double technicalComplexityFactor,
    Double environmentalComplexityFactor,
    byte[] imageBytes,
    String imageName,
    String imageType,
    Boolean ocrAvailable,
    List<String> ocrRecognizedText,
    Double ocrAverageConfidence,
    List<String> ocrWarnings,
    List<OcrTokenPayload> ocrTokens,
    Integer ocrImageWidth,
    Integer ocrImageHeight
) {
    public record DesktopSourcePayload(
        String fileName,
        String sourceCode
    ) {}

    public record OcrTokenPayload(
        String text,
        Double confidence,
        List<Double> bbox,
        List<List<Double>> polygon
    ) {}
}
