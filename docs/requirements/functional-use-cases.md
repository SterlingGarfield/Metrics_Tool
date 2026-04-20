# Functional Use Cases

## Scope

The tool supports an end-to-end software quality workflow for:

- Java source metrics
- structured diagram analysis (PlantUML/Mermaid)
- image diagram recognition (class/flow/usecase)
- project estimation (workload/cost/schedule/staffing)

## Primary Actors

- student analyst
- teaching assistant/reviewer

## Use Cases

### UC-01 Analyze Java Source Code

- Actor uploads code by text, file, file set, or folder.
- System parses Java AST and computes project/class/method metrics.
- System returns CK metrics, traditional metrics, risk findings, and LK presentation fields.

### UC-02 Analyze Structured Diagram

- Actor uploads `.puml` or `.mmd` and selects `class|flow|usecase`.
- System parses structured source and extracts diagram elements and relationships.
- System computes diagram-specific metrics and confidence/issue output.

### UC-03 Analyze Image Diagram

- Actor uploads `.png/.jpg` and selects `class|flow|usecase`.
- System executes image pipeline: preprocess, OCR, detect, recover, metrics, confidence.
- System returns normalized diagram analysis payload with issues and confidence.

### UC-04 Estimate Project Entity Indicators

- Actor submits size/complexity inputs (LoC, class count, relationship count, etc.).
- System computes workload person-months, cost, schedule months, and suggested staffing.
- System returns estimation basis summary/details for report traceability.

### UC-05 Export and Report

- Actor exports CSV/Markdown from code metrics workspace.
- Actor captures diagram/estimation outputs for project design and implementation report.

