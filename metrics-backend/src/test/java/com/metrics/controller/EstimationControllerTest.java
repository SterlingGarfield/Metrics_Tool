package com.metrics.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EstimationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzeManualEstimateReturnsEstimationSection() throws Exception {
        mockMvc.perform(post("/api/metrics/estimation/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "loc": 1200,
                      "staffCount": 6,
                      "devMonths": 2,
                      "cost": 12000.0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estimationMetrics.available").value(true))
            .andExpect(jsonPath("$.estimationMetrics.staffCount").value(6))
            .andExpect(jsonPath("$.estimationMetrics.workloadPersonMonths").value(12.0))
            .andExpect(jsonPath("$.codeMetrics.available").value(false));
    }

    @Test
    void analyzeUseCasePointsReturnsEstimationSection() throws Exception {
        mockMvc.perform(post("/api/metrics/estimation/use-case-points")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "simpleActors": 1,
                      "averageActors": 2,
                      "complexActors": 1,
                      "simpleUseCases": 2,
                      "averageUseCases": 1,
                      "complexUseCases": 1,
                      "technicalComplexityFactor": 1.1,
                      "environmentalComplexityFactor": 0.9
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estimationMetrics.available").value(true))
            .andExpect(jsonPath("$.estimationMetrics.mode").value("use-case-points"))
            .andExpect(jsonPath("$.estimationMetrics.useCasePoints.uaw").value(8))
            .andExpect(jsonPath("$.estimationMetrics.useCasePoints.uucw").value(35))
            .andExpect(jsonPath("$.estimationMetrics.useCasePoints.ucp").value(42.57))
            .andExpect(jsonPath("$.codeMetrics.available").value(false));
    }
}
