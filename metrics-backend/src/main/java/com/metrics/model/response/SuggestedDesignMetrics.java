package com.metrics.model.response;

public record SuggestedDesignMetrics(
    Integer classCount,
    Integer relationshipCount,
    Integer useCaseCount,
    Integer actorCount,
    Integer flowNodeCount
) {}
