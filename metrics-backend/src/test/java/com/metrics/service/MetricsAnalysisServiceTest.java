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

        assertThat(response.projectSummary().totalFiles()).isEqualTo(1);
        assertThat(response.projectSummary().totalClasses()).isEqualTo(1);
        assertThat(response.projectSummary().totalMethods()).isEqualTo(1);
        assertThat(response.methodMetrics()).hasSize(1);
        assertThat(response.methodMetrics().get(0).cyclomaticComplexity()).isEqualTo(2);
        assertThat(response.parseIssues()).isEmpty();
        assertThat(response.codeMetrics()).isNotNull();
        assertThat(response.codeMetrics().lkPresentation()).isNotNull();
        assertThat(response.codeMetrics().lkPresentation().classCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkPresentation().methodCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkPresentation().relationshipCount()).isNull();
        assertThat(response.codeMetrics().lkPresentation().relationDensity()).isNull();
    }
}
