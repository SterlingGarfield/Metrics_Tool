package com.metrics.model.response;

public record ClassMetrics(
    String fileName,
    String className,
    int loc,
    int wmc,
    int cbo,
    int rfc,
    int lcom,
    int dit,
    int noc,
    int nom,
    int noa,
    int publicMethodCount,
    double commentRatio,
    boolean partial
) {}
