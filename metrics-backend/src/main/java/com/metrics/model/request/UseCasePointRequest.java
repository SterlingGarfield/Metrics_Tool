package com.metrics.model.request;

public record UseCasePointRequest(
    int simpleActors,
    int averageActors,
    int complexActors,
    int simpleUseCases,
    int averageUseCases,
    int complexUseCases,
    double technicalComplexityFactor,
    double environmentalComplexityFactor
) {}
