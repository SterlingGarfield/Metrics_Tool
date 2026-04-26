package com.metrics.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metrics.model.response.DesignSuggestionResponse;
import com.metrics.model.response.SuggestedDesignMetrics;
import com.metrics.service.DesignSuggestionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc
class DesignMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignSuggestionService designSuggestionService;

    @Test
    void analyzeDesignReturnsDesignMetricsSection() throws Exception {
        mockMvc.perform(post("/api/metrics/design/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "diagramType": "use-case",
                      "classCount": 2,
                      "relationshipCount": 5,
                      "useCaseCount": 3,
                      "actorCount": 4,
                      "flowNodeCount": 6,
                      "imageProvided": true,
                      "notes": "Registration flow"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.designMetrics.available").value(true))
            .andExpect(jsonPath("$.designMetrics.useCaseCount").value(3))
            .andExpect(jsonPath("$.designMetrics.actorCount").value(4))
            .andExpect(jsonPath("$.designMetrics.relationshipDensity").value(2.5))
            .andExpect(jsonPath("$.designMetrics.useCasesPerActor").value(0.75))
            .andExpect(jsonPath("$.codeMetrics.available").value(false));
    }

    @Test
    void suggestDesignReturnsRecognizedTextAndHeuristicMetrics() throws Exception {
        given(designSuggestionService.suggest("use-case", "design.png", "diagram".getBytes()))
            .willReturn(new DesignSuggestionResponse(
                true,
                "use-case",
                java.util.List.of("User", "Register Account"),
                new SuggestedDesignMetrics(null, null, 1, 1, null),
                0.82,
                java.util.List.of("以下建议基于 OCR 文本启发式推断，请在提交前确认。")
            ));

        MockMultipartFile image = new MockMultipartFile(
            "image",
            "design.png",
            "image/png",
            "diagram".getBytes()
        );

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/metrics/design/suggest")
                    .file(image)
                    .param("diagramType", "use-case")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.diagramType").value("use-case"))
            .andExpect(jsonPath("$.recognizedText[0]").value("User"))
            .andExpect(jsonPath("$.suggestedMetrics.useCaseCount").value(1))
            .andExpect(jsonPath("$.suggestedMetrics.actorCount").value(1));
    }
}
