package com.metrics.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EstimateProjectRequest(
    @NotBlank String diagramType,
    @PositiveOrZero Integer totalLoc,
    @PositiveOrZero Integer classCount,
    @PositiveOrZero Integer relationshipCount,
    @PositiveOrZero Integer useCaseCount,
    @PositiveOrZero Integer decisionNodeCount,
    @Positive Double costRatePerPersonMonth,
    @Positive Double targetScheduleMonths
) {}
