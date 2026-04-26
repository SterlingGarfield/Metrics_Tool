package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.request.ManualEstimateRequest;
import org.junit.jupiter.api.Test;

class EstimationServiceTest {

    private final EstimationService service = new EstimationService();

    @Test
    void computesEffortCostScheduleAndStaffingFromManualInputs() {
        var summary = service.summarize(new ManualEstimateRequest(1200, 6, 2, 12000.0));

        assertThat(summary.available()).isTrue();
        assertThat(summary.loc()).isEqualTo(1200);
        assertThat(summary.staffCount()).isEqualTo(6);
        assertThat(summary.devMonths()).isEqualTo(2);
        assertThat(summary.cost()).isEqualTo(12000.0);
        assertThat(summary.workloadPersonMonths()).isEqualTo(12.0);
        assertThat(summary.productivityPerPersonMonth()).isEqualTo(100.0);
        assertThat(summary.costPerLoc()).isEqualTo(10.0);
    }

    @Test
    void computesUseCasePointsFromWeightedActorsAndUseCases() {
        var summary = service.summarizeUseCasePoints(new com.metrics.model.request.UseCasePointRequest(
            1,
            2,
            1,
            2,
            1,
            1,
            1.1,
            0.9
        ));

        assertThat(summary.mode()).isEqualTo("use-case-points");
        assertThat(summary.useCasePoints()).isNotNull();
        assertThat(summary.useCasePoints().uaw()).isEqualTo(8);
        assertThat(summary.useCasePoints().uucw()).isEqualTo(35);
        assertThat(summary.useCasePoints().uucp()).isEqualTo(43);
        assertThat(summary.useCasePoints().ucp()).isEqualTo(42.57);
    }
}
