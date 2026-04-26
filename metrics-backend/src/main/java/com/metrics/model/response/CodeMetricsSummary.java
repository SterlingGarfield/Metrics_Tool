package com.metrics.model.response;

import java.util.List;

public record CodeMetricsSummary(
    boolean available,
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    LkMetricsSummary lkSummary
) {
    public static CodeMetricsSummary empty() {
        return new CodeMetricsSummary(false, ProjectSummary.empty(), List.of(), List.of(), LkMetricsSummary.empty());
    }
}
