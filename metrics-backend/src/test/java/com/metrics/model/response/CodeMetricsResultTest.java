package com.metrics.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CodeMetricsResultTest {

    @Test
    void buildLkPresentationIncludesFullLkParity() {
        ProjectSummary summary = new ProjectSummary(1, 3, 6, 100, 5, 8, 0.08, 0, 0);
        List<ClassMetrics> classMetrics = List.of(
            new ClassMetrics("A.java", "A", 30, 5, 2, 5, 0, 0, 0, 2, 1, 2, 0.1, false),
            new ClassMetrics("B.java", "B", 35, 6, 3, 6, 0, 1, 0, 2, 2, 1, 0.1, false),
            new ClassMetrics("C.java", "C", 35, 4, 1, 4, 0, 2, 0, 2, 1, 1, 0.1, false)
        );

        CodeMetricsResult result = CodeMetricsResult.fromMetrics(summary, classMetrics, List.of(), List.of(), false);

        assertThat(result.lkMetrics()).isNotNull();
        assertThat(result.lkMetrics().classCount()).isEqualTo(3);
        assertThat(result.lkMetrics().methodCount()).isEqualTo(6);
        assertThat(result.lkMetrics().attributeCount()).isEqualTo(4);
        assertThat(result.lkMetrics().relationshipCount()).isEqualTo(6);
        assertThat(result.lkMetrics().averageMethodsPerClass()).isEqualTo(2.0);
        assertThat(result.lkMetrics().averageAttributesPerClass()).isEqualTo(1.3333333333333333);
        assertThat(result.lkMetrics().relationDensity()).isEqualTo(1.0);
        assertThat(result.lkMetrics().inheritanceDepthDistribution()).containsExactly(0, 1, 2);
        assertThat(result.lkPresentation()).isNotNull();
        assertThat(result.lkPresentation().classCount()).isEqualTo(result.lkMetrics().classCount());
        assertThat(result.lkPresentation().methodCount()).isEqualTo(result.lkMetrics().methodCount());
        assertThat(result.lkPresentation().attributeCount()).isEqualTo(result.lkMetrics().attributeCount());
        assertThat(result.lkPresentation().relationshipCount()).isEqualTo(result.lkMetrics().relationshipCount());
        assertThat(result.lkPresentation().averageMethodsPerClass()).isEqualTo(result.lkMetrics().averageMethodsPerClass());
        assertThat(result.lkPresentation().averageAttributesPerClass()).isEqualTo(result.lkMetrics().averageAttributesPerClass());
        assertThat(result.lkPresentation().relationDensity()).isEqualTo(result.lkMetrics().relationDensity());
        assertThat(result.lkPresentation().inheritanceDepthDistribution()).isEqualTo(result.lkMetrics().inheritanceDepthDistribution());
        assertThat(result.lkPresentation().relationshipCount()).isEqualTo(6);
        assertThat(result.lkPresentation().relationDensity()).isEqualTo(1.0);
        assertThat(result.lkPresentation().averageMethodsPerClass()).isEqualTo(2.0);
        assertThat(result.lkPresentation().averageAttributesPerClass()).isEqualTo(1.3333333333333333);
    }

    @Test
    void directConstructionNormalizesLkPresentationToLkMetrics() {
        CodeMetricsResult.LkMetrics lkMetrics = new CodeMetricsResult.LkMetrics(
            3,
            6,
            6,
            6,
            2.0,
            2.0,
            1.0,
            List.of(1, 1, 2)
        );
        CodeMetricsResult.LkPresentation lkPresentation = new CodeMetricsResult.LkPresentation(
            99,
            98,
            97,
            96,
            95.0,
            94.0,
            93.0,
            List.of(9, 8, 7)
        );

        CodeMetricsResult result = new CodeMetricsResult(
            new ProjectSummary(1, 3, 6, 100, 5, 8, 0.08, 0, 0),
            List.of(),
            List.of(),
            lkMetrics,
            lkPresentation,
            List.of(),
            false
        );

        assertThat(result.lkPresentation().classCount()).isEqualTo(result.lkMetrics().classCount());
        assertThat(result.lkPresentation().methodCount()).isEqualTo(result.lkMetrics().methodCount());
        assertThat(result.lkPresentation().attributeCount()).isEqualTo(result.lkMetrics().attributeCount());
        assertThat(result.lkPresentation().relationshipCount()).isEqualTo(result.lkMetrics().relationshipCount());
        assertThat(result.lkPresentation().averageMethodsPerClass()).isEqualTo(result.lkMetrics().averageMethodsPerClass());
        assertThat(result.lkPresentation().averageAttributesPerClass()).isEqualTo(result.lkMetrics().averageAttributesPerClass());
        assertThat(result.lkPresentation().relationDensity()).isEqualTo(result.lkMetrics().relationDensity());
        assertThat(result.lkPresentation().inheritanceDepthDistribution()).isEqualTo(result.lkMetrics().inheritanceDepthDistribution());
    }

    @Test
    void directConstructionNormalizesLkMetricsFromPresentationOnly() {
        CodeMetricsResult.LkPresentation lkPresentation = new CodeMetricsResult.LkPresentation(
            4,
            8,
            10,
            12,
            2.0,
            2.5,
            1.0,
            List.of(0, 1, 2, 3)
        );

        CodeMetricsResult result = new CodeMetricsResult(
            new ProjectSummary(1, 4, 8, 120, 6, 9, 0.05, 0, 0),
            List.of(),
            List.of(),
            null,
            lkPresentation,
            List.of(),
            false
        );

        assertThat(result.lkMetrics()).isNotNull();
        assertThat(result.lkMetrics().classCount()).isEqualTo(4);
        assertThat(result.lkMetrics().methodCount()).isEqualTo(8);
        assertThat(result.lkMetrics().attributeCount()).isEqualTo(10);
        assertThat(result.lkMetrics().relationshipCount()).isEqualTo(12);
        assertThat(result.lkMetrics().averageMethodsPerClass()).isEqualTo(2.0);
        assertThat(result.lkMetrics().averageAttributesPerClass()).isEqualTo(2.5);
        assertThat(result.lkMetrics().relationDensity()).isEqualTo(1.0);
        assertThat(result.lkMetrics().inheritanceDepthDistribution()).containsExactly(0, 1, 2, 3);
        assertThat(result.lkPresentation().classCount()).isEqualTo(result.lkMetrics().classCount());
        assertThat(result.lkPresentation().methodCount()).isEqualTo(result.lkMetrics().methodCount());
        assertThat(result.lkPresentation().attributeCount()).isEqualTo(result.lkMetrics().attributeCount());
        assertThat(result.lkPresentation().relationshipCount()).isEqualTo(result.lkMetrics().relationshipCount());
        assertThat(result.lkPresentation().averageMethodsPerClass()).isEqualTo(result.lkMetrics().averageMethodsPerClass());
        assertThat(result.lkPresentation().averageAttributesPerClass()).isEqualTo(result.lkMetrics().averageAttributesPerClass());
        assertThat(result.lkPresentation().relationDensity()).isEqualTo(result.lkMetrics().relationDensity());
        assertThat(result.lkPresentation().inheritanceDepthDistribution()).isEqualTo(result.lkMetrics().inheritanceDepthDistribution());
    }
}
