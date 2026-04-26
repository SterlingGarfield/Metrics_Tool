package com.metrics.model.request;

public record ManualEstimateRequest(
    int loc,
    int staffCount,
    int devMonths,
    double cost
) {}
