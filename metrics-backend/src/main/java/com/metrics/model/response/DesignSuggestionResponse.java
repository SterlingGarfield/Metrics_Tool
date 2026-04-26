package com.metrics.model.response;

import java.util.List;

public record DesignSuggestionResponse(
    boolean available,
    String diagramType,
    List<String> recognizedText,
    SuggestedDesignMetrics suggestedMetrics,
    double confidence,
    List<String> warnings
) {}
