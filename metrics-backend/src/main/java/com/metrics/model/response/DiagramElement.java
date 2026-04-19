package com.metrics.model.response;

public record DiagramElement(
    String type,
    String name,
    String stereotype,
    Double confidence
) {}
