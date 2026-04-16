package com.metrics.model.response;

import java.util.List;

public record AnalysisResponse(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    List<RiskFinding> riskFindings,
    List<ParseIssue> parseIssues,
    boolean partial
) {}
