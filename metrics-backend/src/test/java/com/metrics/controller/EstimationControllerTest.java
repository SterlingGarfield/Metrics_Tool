package com.metrics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.model.response.EstimationBasis;
import com.metrics.model.response.ProjectEstimation;
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
    private RecognitionServiceClient recognitionServiceClient;

    @Test
    void estimateProjectRejectsMissingDiagramType() throws Exception {
        mockMvc.perform(post("/api/estimate/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"totalLoc\":3200,\"classCount\":18}"))
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
            )
        );
        when(recognitionServiceClient.estimateProject(any())).thenReturn(estimation);

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
    }
}
