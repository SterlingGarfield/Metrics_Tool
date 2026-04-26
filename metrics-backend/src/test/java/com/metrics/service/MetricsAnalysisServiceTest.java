package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.request.TextAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MetricsAnalysisServiceTest {

    @Autowired
    private MetricsAnalysisService service;

    @Test
    void analyzeTextComputesProjectAndMethodMetrics() {
        String source = """
            public class Sample {
                private int total;

                public int sum(int a, int b) {
                    if (a > b) {
                        return a + b;
                    }
                    return b;
                }
            }
            """;

        AnalysisResponse response = service.analyzeText(new TextAnalyzeRequest("Sample.java", source));

        assertThat(response.codeMetrics()).isNotNull();
        assertThat(response.designMetrics()).isNotNull();
        assertThat(response.estimationMetrics()).isNotNull();
        assertThat(response.codeMetrics().projectSummary().totalFiles()).isEqualTo(1);
        assertThat(response.codeMetrics().projectSummary().totalClasses()).isEqualTo(1);
        assertThat(response.codeMetrics().projectSummary().totalMethods()).isEqualTo(1);
        assertThat(response.codeMetrics().methodMetrics()).hasSize(1);
        assertThat(response.codeMetrics().methodMetrics().get(0).cyclomaticComplexity()).isEqualTo(2);
        assertThat(response.codeMetrics().lkSummary()).isNotNull();
        assertThat(response.codeMetrics().lkSummary().inheritanceClassCount()).isEqualTo(0);
        assertThat(response.parseIssues()).isEmpty();
        assertThat(response.designMetrics().available()).isFalse();
        assertThat(response.estimationMetrics().available()).isFalse();
    }
}
