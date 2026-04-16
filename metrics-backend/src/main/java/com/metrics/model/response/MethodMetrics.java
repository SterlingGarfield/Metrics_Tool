package com.metrics.model.response;

public record MethodMetrics(
    String fileName,
    String className,
    String methodName,
    int loc,
    int cyclomaticComplexity,
    int parameterCount,
    int maxNestingDepth,
    int branchCount
) {}
