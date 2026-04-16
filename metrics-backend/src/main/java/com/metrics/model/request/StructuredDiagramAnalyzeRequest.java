package com.metrics.model.request;

import jakarta.validation.constraints.NotBlank;

public record StructuredDiagramAnalyzeRequest(
    @NotBlank String diagramType,
    @NotBlank String fileName,
    @NotBlank String sourceSuffix,
    @NotBlank String source
) {}
