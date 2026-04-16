package com.metrics.model.response;

public record DiagramMetricValue(
    String name,
    Double value,
    String unit,
    String description
) {}
