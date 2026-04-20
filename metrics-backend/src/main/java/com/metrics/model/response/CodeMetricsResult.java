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

    public static CodeMetricsResult fromMetrics(
        ProjectSummary projectSummary,
        List<ClassMetrics> classMetrics,
        List<MethodMetrics> methodMetrics,
        List<ParseIssue> parseIssues,
        boolean partial
    ) {
        List<ClassMetrics> safeClassMetrics = classMetrics == null ? List.of() : List.copyOf(classMetrics);
        List<MethodMetrics> safeMethodMetrics = methodMetrics == null ? List.of() : List.copyOf(methodMetrics);
        List<ParseIssue> safeParseIssues = parseIssues == null ? List.of() : List.copyOf(parseIssues);
        return new CodeMetricsResult(
            projectSummary,
            safeClassMetrics,
            safeMethodMetrics,
            buildLkPresentation(projectSummary, safeClassMetrics, safeMethodMetrics),
            safeParseIssues,
            partial
        );
    }

    private static LkPresentation buildLkPresentation(
        ProjectSummary projectSummary,
        List<ClassMetrics> classMetrics,
        List<MethodMetrics> methodMetrics
    ) {
        Integer classCount = projectSummary != null ? projectSummary.totalClasses() : classMetrics.size();
        Integer methodCount = projectSummary != null ? projectSummary.totalMethods() : methodMetrics.size();
        Integer attributeCount = classMetrics.stream().mapToInt(ClassMetrics::noa).sum();
        Integer relationshipCount = classMetrics.stream().mapToInt(ClassMetrics::cbo).sum();
        Double averageMethodsPerClass = classCount == null || classCount == 0
            ? null
            : (double) methodCount / classCount;
        Double averageAttributesPerClass = classCount == null || classCount == 0
            ? null
            : (double) attributeCount / classCount;
        Double relationDensity = null;
        if (classCount != null && classCount > 1) {
            double denominator = (double) classCount * (classCount - 1);
            relationDensity = round3(relationshipCount / denominator);
        } else if (classCount != null && classCount == 1) {
            relationDensity = 0.0;
        }
        List<Integer> inheritanceDepthDistribution = classMetrics.stream()
            .map(ClassMetrics::dit)
            .sorted()
            .toList();

        return new LkPresentation(
            classCount,
            methodCount,
            attributeCount,
            relationshipCount,
            averageMethodsPerClass,
            averageAttributesPerClass,
            relationDensity,
            inheritanceDepthDistribution
        );
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
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
