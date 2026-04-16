package com.metrics.model.request;

import jakarta.validation.constraints.NotBlank;

public record TextAnalyzeRequest(
    @NotBlank String fileName,
    @NotBlank String sourceCode
) {}
