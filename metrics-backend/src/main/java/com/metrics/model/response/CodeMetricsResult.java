package com.metrics.model.response;

import java.util.List;

public record CodeMetricsResult(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    LkPresentation lkPresentation,
    List<ParseIssue> parseIssues,
    boolean partial
) {
    public CodeMetricsResult {
        classMetrics = classMetrics == null ? List.of() : List.copyOf(classMetrics);
        methodMetrics = methodMetrics == null ? List.of() : List.copyOf(methodMetrics);
        parseIssues = parseIssues == null ? List.of() : List.copyOf(parseIssues);
    }

    public record LkPresentation(
        Integer classCount,
        Integer methodCount,
        Integer attributeCount,
        Integer relationshipCount,
        Double averageMethodsPerClass,
        Double averageAttributesPerClass,
        Double relationDensity,
        List<Integer> inheritanceDepthDistribution
    ) {
        public LkPresentation {
            inheritanceDepthDistribution = inheritanceDepthDistribution == null
                ? List.of()
                : List.copyOf(inheritanceDepthDistribution);
        }
    }
}
