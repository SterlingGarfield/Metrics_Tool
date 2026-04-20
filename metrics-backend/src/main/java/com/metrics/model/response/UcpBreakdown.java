package com.metrics.model.response;

public record UcpBreakdown(
    boolean standardInputUsed,
    Integer simpleActorCount,
    Integer averageActorCount,
    Integer complexActorCount,
    Integer simpleUseCaseCount,
    Integer averageUseCaseCount,
    Integer complexUseCaseCount,
    Double uaw,
    Double uucw,
    Double uucp,
    Double technicalComplexityFactor,
    Double environmentalFactor,
    Double ucp,
    Double workloadPersonMonths
) {}
