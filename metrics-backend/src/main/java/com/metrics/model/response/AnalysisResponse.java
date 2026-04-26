package com.metrics.model.response;

import java.util.List;

public record AnalysisResponse(
    CodeMetricsSummary codeMetrics,
    DesignMetricsSummary designMetrics,
    EstimationSummary estimationMetrics,
    List<RiskFinding> riskFindings,
    List<ParseIssue> parseIssues,
    boolean partial
) {}
