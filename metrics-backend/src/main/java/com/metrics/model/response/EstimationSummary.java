package com.metrics.model.response;

public record EstimationSummary(
    boolean available,
    String mode,
    int loc,
    int staffCount,
    int devMonths,
    double cost,
    double workloadPersonMonths,
    double productivityPerPersonMonth,
    double costPerLoc,
    UseCasePointSummary useCasePoints
) {
    public static EstimationSummary empty() {
        return new EstimationSummary(false, "", 0, 0, 0, 0.0, 0.0, 0.0, 0.0, UseCasePointSummary.empty());
    }
}
