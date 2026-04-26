package com.metrics.service;

import com.metrics.model.request.DesignAnalyzeRequest;
import com.metrics.model.response.DesignMetricsSummary;
import org.springframework.stereotype.Service;

@Service
public class DesignMetricsService {

    public DesignMetricsSummary analyze(DesignAnalyzeRequest request) {
        double relationshipDensity = request.classCount() > 0
            ? (double) request.relationshipCount() / request.classCount()
            : 0.0;
        double useCasesPerActor = request.actorCount() > 0
            ? (double) request.useCaseCount() / request.actorCount()
            : 0.0;

        return new DesignMetricsSummary(
            true,
            request.diagramType(),
            request.classCount(),
            request.relationshipCount(),
            request.useCaseCount(),
            request.actorCount(),
            request.flowNodeCount(),
            relationshipDensity,
            useCasesPerActor,
            request.imageProvided(),
            request.notes()
        );
    }
}
