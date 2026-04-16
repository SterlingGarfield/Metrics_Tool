package com.metrics.model.response;

import java.util.List;

public record AnalysisResponse(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    List<RiskFinding> riskFindings,
    List<ParseIssue> parseIssues,
    boolean partial,
    CodeMetricsResult codeMetrics,
    DiagramAnalysisResponse diagramAnalysis,
    ProjectEstimation projectEstimation
) {

    public AnalysisResponse {
        classMetrics = classMetrics == null ? List.of() : List.copyOf(classMetrics);
        methodMetrics = methodMetrics == null ? List.of() : List.copyOf(methodMetrics);
        riskFindings = riskFindings == null ? List.of() : List.copyOf(riskFindings);
        parseIssues = parseIssues == null ? List.of() : List.copyOf(parseIssues);
        if (codeMetrics == null
            && (projectSummary != null || !classMetrics.isEmpty() || !methodMetrics.isEmpty() || !parseIssues.isEmpty() || partial)) {
            codeMetrics = new CodeMetricsResult(
                projectSummary,
                classMetrics,
                methodMetrics,
                buildLkPresentation(projectSummary, classMetrics, methodMetrics),
                parseIssues,
                partial
            );
        }
    }

    public AnalysisResponse(
        ProjectSummary projectSummary,
        List<ClassMetrics> classMetrics,
        List<MethodMetrics> methodMetrics,
        List<RiskFinding> riskFindings,
        List<ParseIssue> parseIssues,
        boolean partial
    ) {
        this(
            projectSummary,
            classMetrics,
            methodMetrics,
            riskFindings,
            parseIssues,
            partial,
            null,
            null,
            null
        );
    }

    public static AnalysisResponse withDiagramAnalysis(DiagramAnalysisResponse diagramAnalysis) {
        return new AnalysisResponse(null, null, null, List.of(), List.of(), false, null, diagramAnalysis, null);
    }

    public static AnalysisResponse withProjectEstimation(ProjectEstimation projectEstimation) {
        return new AnalysisResponse(null, null, null, List.of(), List.of(), false, null, null, projectEstimation);
    }

    private static CodeMetricsResult.LkPresentation buildLkPresentation(
        ProjectSummary projectSummary,
        List<ClassMetrics> classMetrics,
        List<MethodMetrics> methodMetrics
    ) {
        int classCount = projectSummary != null && projectSummary.totalClasses() > 0
            ? projectSummary.totalClasses()
            : classMetrics.size();
        int methodCount = projectSummary != null && projectSummary.totalMethods() > 0
            ? projectSummary.totalMethods()
            : methodMetrics.size();
        int attributeCount = classMetrics.stream()
            .mapToInt(ClassMetrics::noa)
            .sum();
        int relationshipCount = classMetrics.stream()
            .mapToInt(ClassMetrics::cbo)
            .sum();
        double averageMethodsPerClass = classCount == 0
            ? 0.0
            : (double) methodCount / classCount;
        double averageAttributesPerClass = classCount == 0
            ? 0.0
            : (double) attributeCount / classCount;
        double relationDensity = classCount <= 1
            ? 0.0
            : (double) relationshipCount / (classCount * (classCount - 1));
        List<Integer> inheritanceDepthDistribution = classMetrics.stream()
            .map(ClassMetrics::dit)
            .sorted()
            .toList();
        return new CodeMetricsResult.LkPresentation(
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
}
