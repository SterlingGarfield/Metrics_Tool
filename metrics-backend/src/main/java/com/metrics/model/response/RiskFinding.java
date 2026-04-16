package com.metrics.model.response;

public record RiskFinding(
    String level,
    String scope,
    String target,
    String message
) {}
