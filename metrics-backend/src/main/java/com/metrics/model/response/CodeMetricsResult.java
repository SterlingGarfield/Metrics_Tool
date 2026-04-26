package com.metrics.model.response;

import java.util.List;

public record CodeMetricsResult(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    LkMetrics lkMetrics,
    LkPresentation lkPresentation,
    List<ParseIssue> parseIssues,
    boolean partial
) {
    public CodeMetricsResult {
        classMetrics = classMetrics == null ? List.of() : List.copyOf(classMetrics);
        methodMetrics = methodMetrics == null ? List.of() : List.copyOf(methodMetrics);
        parseIssues = parseIssues == null ? List.of() : List.copyOf(parseIssues);
        lkMetrics = lkMetrics != null ? lkMetrics : lkPresentation != null ? new LkMetrics(lkPresentation) : null;
        lkPresentation = lkMetrics == null ? null : new LkPresentation(lkMetrics);
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
        LkMetrics lkMetrics = buildLkMetrics(projectSummary, safeClassMetrics, safeMethodMetrics);
        return new CodeMetricsResult(
            projectSummary,
            safeClassMetrics,
            safeMethodMetrics,
            lkMetrics,
            null,
            safeParseIssues,
            partial
        );
    }

    private static LkMetrics buildLkMetrics(
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

        return new LkMetrics(
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

    public record LkMetrics(
        Integer classCount,
        Integer methodCount,
        Integer attributeCount,
        Integer relationshipCount,
        Double averageMethodsPerClass,
        Double averageAttributesPerClass,
        Double relationDensity,
        List<Integer> inheritanceDepthDistribution
    ) {
        public LkMetrics {
            inheritanceDepthDistribution = inheritanceDepthDistribution == null
                ? List.of()
                : List.copyOf(inheritanceDepthDistribution);
        }

        public LkMetrics(LkPresentation source) {
            this(
                source == null ? null : source.classCount(),
                source == null ? null : source.methodCount(),
                source == null ? null : source.attributeCount(),
                source == null ? null : source.relationshipCount(),
                source == null ? null : source.averageMethodsPerClass(),
                source == null ? null : source.averageAttributesPerClass(),
                source == null ? null : source.relationDensity(),
                source == null ? null : source.inheritanceDepthDistribution()
            );
        }
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

        public LkPresentation(LkMetrics source) {
            this(
                source == null ? null : source.classCount(),
                source == null ? null : source.methodCount(),
                source == null ? null : source.attributeCount(),
                source == null ? null : source.relationshipCount(),
                source == null ? null : source.averageMethodsPerClass(),
                source == null ? null : source.averageAttributesPerClass(),
                source == null ? null : source.relationDensity(),
                source == null ? null : source.inheritanceDepthDistribution()
            );
        }
    }
}
