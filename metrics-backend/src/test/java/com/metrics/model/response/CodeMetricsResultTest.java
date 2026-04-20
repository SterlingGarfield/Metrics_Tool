package com.metrics.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CodeMetricsResultTest {

    @Test
    void buildLkPresentationIncludesRelationshipCountAndDensity() {
        ProjectSummary summary = new ProjectSummary(1, 3, 6, 100, 5, 8, 0.08, 0, 0);
        List<ClassMetrics> classMetrics = List.of(
            new ClassMetrics("A.java", "A", 30, 5, 2, 5, 0, 0, 0, 2, 1, 2, 0.1, false),
            new ClassMetrics("B.java", "B", 35, 6, 3, 6, 0, 1, 0, 2, 2, 1, 0.1, false),
            new ClassMetrics("C.java", "C", 35, 4, 1, 4, 0, 2, 0, 2, 1, 1, 0.1, false)
        );

        CodeMetricsResult result = CodeMetricsResult.fromMetrics(summary, classMetrics, List.of(), List.of(), false);

        assertThat(result.lkPresentation().relationshipCount()).isEqualTo(6);
        assertThat(result.lkPresentation().relationDensity()).isEqualTo(1.0);
        assertThat(result.lkPresentation().averageMethodsPerClass()).isEqualTo(2.0);
        assertThat(result.lkPresentation().averageAttributesPerClass()).isEqualTo(1.3333333333333333);
    }
}
