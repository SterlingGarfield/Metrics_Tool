package com.metrics.model.response;

public record RecognitionIssue(
    String level,
    String code,
    String message
) {}
