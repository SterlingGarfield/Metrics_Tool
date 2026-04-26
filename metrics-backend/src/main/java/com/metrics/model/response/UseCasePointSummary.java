package com.metrics.model.response;

public record UseCasePointSummary(
    int uaw,
    int uucw,
    int uucp,
    double ucp
) {
    public static UseCasePointSummary empty() {
        return new UseCasePointSummary(0, 0, 0, 0.0);
    }
}
