package com.metrics.model.response;

public record ProjectSummary(
    int totalFiles,
    int totalClasses,
    int totalMethods,
    int totalLoc,
    int blankLines,
    int commentLines,
    double commentRatio,
    int highRiskClasses,
    int highRiskMethods
) {}
