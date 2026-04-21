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
        assertThat(response.codeMetrics().lkMetrics()).isNotNull();
        assertThat(response.codeMetrics().lkMetrics().classCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkMetrics().methodCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkMetrics().attributeCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkMetrics().relationshipCount()).isEqualTo(0);
        assertThat(response.codeMetrics().lkMetrics().averageMethodsPerClass()).isEqualTo(1.0);
        assertThat(response.codeMetrics().lkMetrics().averageAttributesPerClass()).isEqualTo(1.0);
        assertThat(response.codeMetrics().lkMetrics().relationDensity()).isEqualTo(0.0);
        assertThat(response.codeMetrics().lkMetrics().inheritanceDepthDistribution()).containsExactly(0);
        assertThat(response.codeMetrics().lkPresentation()).isNotNull();
        assertThat(response.codeMetrics().lkPresentation().classCount()).isEqualTo(response.codeMetrics().lkMetrics().classCount());
        assertThat(response.codeMetrics().lkPresentation().methodCount()).isEqualTo(response.codeMetrics().lkMetrics().methodCount());
        assertThat(response.codeMetrics().lkPresentation().attributeCount()).isEqualTo(response.codeMetrics().lkMetrics().attributeCount());
        assertThat(response.codeMetrics().lkPresentation().relationshipCount()).isEqualTo(response.codeMetrics().lkMetrics().relationshipCount());
        assertThat(response.codeMetrics().lkPresentation().averageMethodsPerClass()).isEqualTo(response.codeMetrics().lkMetrics().averageMethodsPerClass());
        assertThat(response.codeMetrics().lkPresentation().averageAttributesPerClass()).isEqualTo(response.codeMetrics().lkMetrics().averageAttributesPerClass());
        assertThat(response.codeMetrics().lkPresentation().relationDensity()).isEqualTo(response.codeMetrics().lkMetrics().relationDensity());
        assertThat(response.codeMetrics().lkPresentation().inheritanceDepthDistribution()).isEqualTo(response.codeMetrics().lkMetrics().inheritanceDepthDistribution());
        assertThat(response.codeMetrics().lkPresentation().classCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkPresentation().methodCount()).isEqualTo(1);
        assertThat(response.codeMetrics().lkPresentation().relationshipCount()).isEqualTo(0);
        assertThat(response.codeMetrics().lkPresentation().relationDensity()).isEqualTo(0.0);
    }
}
