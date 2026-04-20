package com.metrics.model.response;

public record ProjectEstimation(
    Double workloadPersonMonths,
    Double cost,
    Double scheduleMonths,
    Integer suggestedStaffing,
    EstimationBasis basis,
    UcpBreakdown ucpBreakdown
) {}
