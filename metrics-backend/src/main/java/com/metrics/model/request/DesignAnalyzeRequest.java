package com.metrics.model.request;

public record DesignAnalyzeRequest(
    String diagramType,
    int classCount,
    int relationshipCount,
    int useCaseCount,
    int actorCount,
    int flowNodeCount,
    boolean imageProvided,
    String notes
) {}
