package com.metrics.model.request;

import jakarta.validation.constraints.NotBlank;

public record StructuredDiagramAnalyzeRequest(
    @NotBlank String diagramType,
    @NotBlank String source
) {}
