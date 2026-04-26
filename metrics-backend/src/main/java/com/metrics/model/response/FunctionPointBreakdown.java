package com.metrics.model.response;

public record FunctionPointBreakdown(
    boolean directInputUsed,
    Integer externalInputCount,
    Integer externalOutputCount,
    Integer externalInquiryCount,
    Integer internalLogicalFileCount,
    Integer externalInterfaceFileCount,
    Double unadjustedFunctionPoints,
    Double valueAdjustmentFactor,
    Double adjustedFunctionPoints,
    Double workloadPersonMonths
) {}
