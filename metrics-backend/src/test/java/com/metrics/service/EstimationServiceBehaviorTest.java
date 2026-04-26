package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.EstimationMethod;
import com.metrics.model.DiagramType;
import com.metrics.model.request.EstimateProjectRequest;
import com.metrics.model.response.ProjectEstimation;
import org.junit.jupiter.api.Test;

class EstimationServiceBehaviorTest {

    private final EstimationService service = new EstimationService();

    @Test
    void flowDiagramYieldsHigherWorkloadThanClassForSameInputs() {
        EstimateProjectRequest classRequest = new EstimateProjectRequest(
            null, // estimationMethod
            DiagramType.CLASS,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null, // targetScheduleMonths
            null, // simpleActorCount
            null, // averageActorCount
            null, // complexActorCount
            null, // simpleUseCaseCount
            null, // averageUseCaseCount
            null, // complexUseCaseCount
            null, // technicalComplexityFactor
            null, // environmentalFactor
            null, // externalInputCount
            null, // externalOutputCount
            null, // externalInquiryCount
            null, // internalLogicalFileCount
            null, // externalInterfaceFileCount
            null  // valueAdjustmentFactor
        );
        EstimateProjectRequest flowRequest = new EstimateProjectRequest(
            null, // estimationMethod
            DiagramType.FLOW,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null, // targetScheduleMonths
            null, // simpleActorCount
            null, // averageActorCount
            null, // complexActorCount
            null, // simpleUseCaseCount
            null, // averageUseCaseCount
            null, // complexUseCaseCount
            null, // technicalComplexityFactor
            null, // environmentalFactor
            null, // externalInputCount
            null, // externalOutputCount
            null, // externalInquiryCount
            null, // internalLogicalFileCount
            null, // externalInterfaceFileCount
            null  // valueAdjustmentFactor
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
            null, // estimationMethod
            DiagramType.USECASE,
            2500,
            10,
            12,
            9,
            4,
            15000.0,
            null, // targetScheduleMonths
            2,
            3,
            1,
            4,
            3,
            1,
            1.05,
            0.95,
            null, // externalInputCount
            null, // externalOutputCount
            null, // externalInquiryCount
            null, // internalLogicalFileCount
            null, // externalInterfaceFileCount
            null  // valueAdjustmentFactor
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
            null, // estimationMethod
            DiagramType.CLASS,
            3200,
            18,
            26,
            0,
            12,
            15000.0,
            null, // targetScheduleMonths
            null, // simpleActorCount
            null, // averageActorCount
            null, // complexActorCount
            null, // simpleUseCaseCount
            null, // averageUseCaseCount
            null, // complexUseCaseCount
            null, // technicalComplexityFactor
            null, // environmentalFactor
            null, // externalInputCount
            null, // externalOutputCount
            null, // externalInquiryCount
            null, // internalLogicalFileCount
            null, // externalInterfaceFileCount
            null  // valueAdjustmentFactor
        );

        ProjectEstimation estimation = service.estimate(request);

        assertThat(estimation.ucpBreakdown()).isNotNull();
        assertThat(estimation.ucpBreakdown().standardInputUsed()).isFalse();
        assertThat(estimation.ucpBreakdown().averageUseCaseCount()).isGreaterThanOrEqualTo(1);
        assertThat(estimation.ucpBreakdown().ucp()).isGreaterThan(0.0);
        assertThat(estimation.basis().summary()).contains("simplified UCP");
    }

    @Test
    void functionPointMethodUsesDirectCountsWhenRequested() {
        EstimateProjectRequest request = new EstimateProjectRequest(
            EstimationMethod.FUNCTION_POINT,
            DiagramType.CLASS,
            3200,
            18,
            26,
            7,
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
            null,
            12,
            8,
            5,
            4,
            2,
            1.10
        );

        ProjectEstimation estimation = service.estimate(request);

        assertThat(estimation.ucpBreakdown()).isNull();
        assertThat(estimation.functionPointBreakdown()).isNotNull();
        assertThat(estimation.functionPointBreakdown().directInputUsed()).isTrue();
        assertThat(estimation.functionPointBreakdown().unadjustedFunctionPoints()).isEqualTo(162.0);
        assertThat(estimation.functionPointBreakdown().adjustedFunctionPoints()).isEqualTo(178.2);
        assertThat(estimation.functionPointBreakdown().workloadPersonMonths()).isEqualTo(14.85);
        assertThat(estimation.workloadPersonMonths()).isEqualTo(14.85);
        assertThat(estimation.basis().summary()).contains("Function Point");
    }
}
