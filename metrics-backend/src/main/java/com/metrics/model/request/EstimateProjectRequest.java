package com.metrics.model.request;

import com.metrics.model.DiagramType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EstimateProjectRequest(
    @NotNull DiagramType diagramType,
    @PositiveOrZero Integer totalLoc,
    @PositiveOrZero Integer classCount,
    @PositiveOrZero Integer relationshipCount,
    @PositiveOrZero Integer useCaseCount,
    @PositiveOrZero Integer decisionNodeCount,
    @Positive Double costRatePerPersonMonth,
    @Positive Double targetScheduleMonths
) {}
