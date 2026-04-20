package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.DiagramType;
import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.response.ProjectEstimation;
import org.junit.jupiter.api.Test;

class EstimationServiceBehaviorTest {

    private final EstimationService service = new EstimationService();

    @Test
    void flowDiagramYieldsHigherWorkloadThanClassForSameInputs() {
        EstimateProjectRequest classRequest = new EstimateProjectRequest(
            DiagramType.CLASS,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null
        );
        EstimateProjectRequest flowRequest = new EstimateProjectRequest(
            DiagramType.FLOW,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null
        );

        ProjectEstimation classEstimation = service.estimate(classRequest);
        ProjectEstimation flowEstimation = service.estimate(flowRequest);

        assertThat(flowEstimation.workloadPersonMonths()).isGreaterThan(classEstimation.workloadPersonMonths());
        assertThat(flowEstimation.basis().summary()).contains("Heuristic blend");
    }
}
