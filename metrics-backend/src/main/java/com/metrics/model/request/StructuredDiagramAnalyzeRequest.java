package com.metrics.model.request;

import com.metrics.model.DiagramType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StructuredDiagramAnalyzeRequest(
    @NotNull DiagramType diagramType,
    @NotBlank String fileName,
    @NotBlank String sourceSuffix,
    @NotBlank String source
) {}
