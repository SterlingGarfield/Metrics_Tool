package com.metrics.model.response;

public record DesignMetricsSummary(
    boolean available,
    String diagramType,
    int classCount,
    int relationshipCount,
    int useCaseCount,
    int actorCount,
    int flowNodeCount,
    double relationshipDensity,
    double useCasesPerActor,
    boolean imageProvided,
    String notes
) {
    public static DesignMetricsSummary empty() {
        return new DesignMetricsSummary(false, "", 0, 0, 0, 0, 0, 0.0, 0.0, false, "");
    }
}
