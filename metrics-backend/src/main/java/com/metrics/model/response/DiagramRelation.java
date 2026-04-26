package com.metrics.model.response;

public record DiagramRelation(
    String source,
    String target,
    String type,
    Double confidence
) {}
