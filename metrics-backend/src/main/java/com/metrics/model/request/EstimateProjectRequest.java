package com.metrics.model.request;

import com.metrics.model.DiagramType;
import com.metrics.model.EstimationMethod;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EstimateProjectRequest(
    EstimationMethod estimationMethod,
    @NotNull DiagramType diagramType,
    @PositiveOrZero Integer totalLoc,
    @PositiveOrZero Integer classCount,
    @PositiveOrZero Integer relationshipCount,
    @PositiveOrZero Integer useCaseCount,
    @PositiveOrZero Integer decisionNodeCount,
    @Positive Double costRatePerPersonMonth,
    @Positive Double targetScheduleMonths,
    @PositiveOrZero Integer simpleActorCount,
    @PositiveOrZero Integer averageActorCount,
    @PositiveOrZero Integer complexActorCount,
    @PositiveOrZero Integer simpleUseCaseCount,
    @PositiveOrZero Integer averageUseCaseCount,
    @PositiveOrZero Integer complexUseCaseCount,
    @DecimalMin("0.60") @DecimalMax("1.40") Double technicalComplexityFactor,
    @DecimalMin("0.60") @DecimalMax("1.40") Double environmentalFactor,
    @PositiveOrZero Integer externalInputCount,
    @PositiveOrZero Integer externalOutputCount,
    @PositiveOrZero Integer externalInquiryCount,
    @PositiveOrZero Integer internalLogicalFileCount,
    @PositiveOrZero Integer externalInterfaceFileCount,
    @DecimalMin("0.65") @DecimalMax("1.35") Double valueAdjustmentFactor
) {}
