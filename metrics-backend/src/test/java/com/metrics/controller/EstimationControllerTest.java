package com.metrics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metrics.model.response.FunctionPointBreakdown;
import com.metrics.model.response.EstimationBasis;
import com.metrics.model.response.ProjectEstimation;
import com.metrics.service.EstimationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EstimationController.class)
class EstimationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstimationService estimationService;

    @Test
    void estimateProjectRejectsMissingDiagramType() throws Exception {
        mockMvc.perform(post("/api/estimate/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"totalLoc\":3200,\"classCount\":18}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void estimateProjectRejectsInvalidDiagramType() throws Exception {
        mockMvc.perform(post("/api/estimate/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"diagramType\": \"Class\",
                      \"totalLoc\": 3200,
                      \"classCount\": 18,
                      \"relationshipCount\": 26,
                      \"useCaseCount\": 0,
                      \"decisionNodeCount\": 0,
                      \"costRatePerPersonMonth\": 15000
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void estimateProjectReturnsWorkloadCostScheduleStaffingAndBasis() throws Exception {
        ProjectEstimation estimation = new ProjectEstimation(
            3.6,
            54000.0,
            2.0,
            2,
            new EstimationBasis(
                "Heuristic blend of code and diagram complexity.",
                "Uses totalLoc, classCount, relationshipCount, useCaseCount, and decisionNodeCount."
            ),
            null,
            null
        );
        when(estimationService.estimate(any())).thenReturn(estimation);

        mockMvc.perform(post("/api/estimate/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"diagramType\": \"class\",
                      \"totalLoc\": 3200,
                      \"classCount\": 18,
                      \"relationshipCount\": 26,
                      \"useCaseCount\": 0,
                      \"decisionNodeCount\": 0,
                      \"costRatePerPersonMonth\": 15000
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectEstimation.workloadPersonMonths").value(3.6))
            .andExpect(jsonPath("$.projectEstimation.cost").value(54000.0))
            .andExpect(jsonPath("$.projectEstimation.scheduleMonths").value(2.0))
            .andExpect(jsonPath("$.projectEstimation.suggestedStaffing").value(2))
            .andExpect(jsonPath("$.projectEstimation.basis.summary").value("Heuristic blend of code and diagram complexity."))
            .andExpect(jsonPath("$.projectEstimation.basis.details").value("Uses totalLoc, classCount, relationshipCount, useCaseCount, and decisionNodeCount."));

        verify(estimationService).estimate(any());
    }

    @Test
    void estimateProjectReturnsFunctionPointBreakdownWhenProvided() throws Exception {
        ProjectEstimation estimation = new ProjectEstimation(
            14.85,
            222750.0,
            3.1,
            5,
            new EstimationBasis(
                "Function Point estimation using direct transactional and data-function counts.",
                "Function Point path=direct."
            ),
            null,
            new FunctionPointBreakdown(true, 12, 8, 5, 4, 2, 162.0, 1.1, 178.2, 14.85)
        );
        when(estimationService.estimate(any())).thenReturn(estimation);

        mockMvc.perform(post("/api/estimate/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"estimationMethod\": \"function_point\",
                      \"diagramType\": \"class\",
                      \"totalLoc\": 3200,
                      \"classCount\": 18,
                      \"relationshipCount\": 26,
                      \"useCaseCount\": 7,
                      \"decisionNodeCount\": 12,
                      \"externalInputCount\": 12,
                      \"externalOutputCount\": 8,
                      \"externalInquiryCount\": 5,
                      \"internalLogicalFileCount\": 4,
                      \"externalInterfaceFileCount\": 2,
                      \"valueAdjustmentFactor\": 1.1,
                      \"costRatePerPersonMonth\": 15000
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectEstimation.functionPointBreakdown.directInputUsed").value(true))
            .andExpect(jsonPath("$.projectEstimation.functionPointBreakdown.adjustedFunctionPoints").value(178.2))
            .andExpect(jsonPath("$.projectEstimation.functionPointBreakdown.workloadPersonMonths").value(14.85));
    }
}
