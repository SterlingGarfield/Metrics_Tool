package com.metrics.model.response;

public record LkMetricsSummary(
    boolean available,
    double averageAddedMethodCount,
    double averageOverriddenMethodCount,
    double maxSpecializationIndex,
    int inheritanceClassCount
) {
    public static LkMetricsSummary empty() {
        return new LkMetricsSummary(false, 0.0, 0.0, 0.0, 0);
    }
}
