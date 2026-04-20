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
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
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
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        ProjectEstimation classEstimation = service.estimate(classRequest);
        ProjectEstimation flowEstimation = service.estimate(flowRequest);

        assertThat(flowEstimation.workloadPersonMonths()).isGreaterThan(classEstimation.workloadPersonMonths());
        assertThat(flowEstimation.basis().summary()).contains("Heuristic blend");
        assertThat(flowEstimation.ucpBreakdown()).isNotNull();
        assertThat(flowEstimation.ucpBreakdown().standardInputUsed()).isFalse();
    }

    @Test
    void standardUcpInputsUseClassicFormulaWhenProvided() {
        EstimateProjectRequest request = new EstimateProjectRequest(
            DiagramType.USECASE,
            2500,
            10,
            12,
            9,
            4,
            15000.0,
            null,
            2,
            3,
            1,
            4,
            3,
            1,
            1.05,
            0.95
        );

        ProjectEstimation estimation = service.estimate(request);

        assertThat(estimation.ucpBreakdown()).isNotNull();
        assertThat(estimation.ucpBreakdown().standardInputUsed()).isTrue();
        assertThat(estimation.ucpBreakdown().uaw()).isEqualTo(11.0);
        assertThat(estimation.ucpBreakdown().uucw()).isEqualTo(65.0);
        assertThat(estimation.ucpBreakdown().uucp()).isEqualTo(76.0);
        assertThat(estimation.ucpBreakdown().ucp()).isEqualTo(75.81);
        assertThat(estimation.workloadPersonMonths()).isEqualTo(9.48);
        assertThat(estimation.basis().summary()).contains("standard UCP");
    }

    @Test
    void estimationFallsBackToSimplifiedUcpWhenStandardFieldsMissing() {
        EstimateProjectRequest request = new EstimateProjectRequest(
            DiagramType.CLASS,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        ProjectEstimation estimation = service.estimate(request);

        assertThat(estimation.ucpBreakdown()).isNotNull();
        assertThat(estimation.ucpBreakdown().standardInputUsed()).isFalse();
        assertThat(estimation.ucpBreakdown().averageUseCaseCount()).isGreaterThanOrEqualTo(1);
        assertThat(estimation.ucpBreakdown().ucp()).isGreaterThan(0.0);
        assertThat(estimation.basis().summary()).contains("simplified UCP");
    }
}
