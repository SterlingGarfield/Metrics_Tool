package com.metrics.model.response;

public record ConfidenceSummary(
    Double overall,
    boolean directlyMeasurable
) {}
