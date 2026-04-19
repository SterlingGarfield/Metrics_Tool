package com.metrics.model.response;

import java.util.List;

public record DiagramAnalysisResponse(
    String diagramType,
    String sourceType,
    List<DiagramElement> elements,
    List<DiagramRelation> relations,
    List<DiagramMetricValue> metrics,
    ConfidenceSummary confidence,
    List<RecognitionIssue> issues
) {
    public DiagramAnalysisResponse {
        elements = elements == null ? List.of() : List.copyOf(elements);
        relations = relations == null ? List.of() : List.copyOf(relations);
        metrics = metrics == null ? List.of() : List.copyOf(metrics);
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
