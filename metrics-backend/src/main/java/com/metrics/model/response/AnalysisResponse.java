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
            CodeMetricsResult.fromMetrics(projectSummary, classMetrics, methodMetrics, parseIssues, partial),
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
}
