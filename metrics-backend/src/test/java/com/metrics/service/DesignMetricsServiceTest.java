package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.metrics.model.request.DesignAnalyzeRequest;
import org.junit.jupiter.api.Test;

class DesignMetricsServiceTest {

    private final DesignMetricsService service = new DesignMetricsService();

    @Test
    void summarizesClassUseCaseAndFlowInputs() {
        var summary = service.analyze(new DesignAnalyzeRequest(
            "uml-class",
            8,
            12,
            3,
            6,
            4,
            true,
            "Core module design"
        ));

        assertThat(summary.available()).isTrue();
        assertThat(summary.diagramType()).isEqualTo("uml-class");
        assertThat(summary.classCount()).isEqualTo(8);
        assertThat(summary.relationshipCount()).isEqualTo(12);
        assertThat(summary.useCaseCount()).isEqualTo(3);
        assertThat(summary.actorCount()).isEqualTo(6);
        assertThat(summary.flowNodeCount()).isEqualTo(4);
        assertThat(summary.imageProvided()).isTrue();
        assertThat(summary.relationshipDensity()).isEqualTo(1.5);
        assertThat(summary.useCasesPerActor()).isEqualTo(0.5);
    }
}
