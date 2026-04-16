# Java Metrics Tool Design

## Context

This project follows the "software metrics automation tool" track from the course guidance and uses the implementation direction captured in `项目实现.md`.

The tool is intended for local demonstration, report writing, and classroom presentation. It should feel like a complete course project rather than a minimal prototype.

## Goal

Build a local web-based Java software metrics tool that can:

- analyze pasted Java code, uploaded Java files, multiple Java files, and whole source folders
- compute project-level, class-level, and method-level metrics
- visualize results and highlight high-risk code objects
- export analysis results in report-friendly formats
- run locally with a simple demo startup flow

## Non-Goals

- cloud deployment
- non-Java language support in v1
- database persistence
- IDE plugin packaging
- full enterprise-scale static analysis

## Users

- course project team members building and demonstrating the tool
- instructors reviewing functionality, engineering quality, and report completeness
- classmates or evaluators who need to inspect Java code quality through a simple UI

## Success Criteria

- the system can analyze a folder containing Java source files in one action
- the system returns stable metrics for project, class, and method scopes
- the UI clearly presents metrics, charts, tables, and actionable risk summaries
- the project can be run locally for development and also packaged into a one-command demo build
- the repository contains enough documentation and report support material for the course submission

## Requirements Mapping

### Functional requirements

- accept Java source through text, file, multi-file, and folder input modes
- compute project-level, class-level, and method-level metrics
- display metrics in cards, charts, and tables
- identify and present high-risk classes and methods
- export report-friendly results

### Data requirements

- store uploaded source only for in-memory analysis during a run
- preserve file names and relative paths for folder analysis results
- represent metrics in structured project, class, and method result models
- retain parse warnings and partial-computation flags in the response model

### Non-functional requirements

- local startup must be simple enough for classroom demonstration
- analysis should remain responsive for small and medium-sized teaching examples
- individual file parse failures must not crash the whole analysis batch
- the UI should be understandable without specialist tooling knowledge
- the codebase should be modular enough to support testing and later metric expansion

## Technical Direction

### Recommended architecture

Use a frontend-backend separated architecture during development and a single packaged backend for demo delivery.

- frontend: Vue 3 + Vite + Axios + ECharts
- backend: Java 17 + Spring Boot + Maven + Eclipse JDT ASTParser
- packaging: Vite build output copied into Spring Boot static resources for demo mode

This direction matches the implementation note and also fits the course guidance, which explicitly recommends AST-based Java analysis.

### Repository structure

```text
.
├── docs/
│   ├── report-outline.md
│   ├── metric-definitions.md
│   └── superpowers/
│       └── specs/
│           └── 2026-04-16-java-metrics-tool-design.md
├── metrics-backend/
│   ├── pom.xml
│   └── src/
├── metrics-frontend/
│   ├── package.json
│   └── src/
├── samples/
│   └── demo-projects/
└── scripts/
    ├── dev.ps1
    ├── build-demo.ps1
    └── run-demo.ps1
```

## Functional Scope

### Supported inputs

The first version should support four input modes:

1. pasted Java code
2. single `.java` file upload
3. multiple `.java` file upload
4. source folder scan through browser folder selection

Folder scanning should be implemented through browser-side directory selection instead of letting the backend read arbitrary local filesystem paths. This keeps the demo safe, portable, and easy to explain.

### Metric scope

The tool should produce three levels of results.

#### Project-level metrics

- total file count
- total class and interface count
- total method count
- total LOC
- blank line count
- comment line count
- comment ratio
- average LOC per class
- average LOC per method
- number of high-risk classes
- number of high-risk methods

#### Class-level metrics

Primary course-facing metrics:

- WMC
- CBO
- RFC
- LCOM
- DIT
- NOC

Supporting object-oriented and size metrics:

- NOM
- NOA
- public method count
- class LOC
- average method LOC
- class comment ratio

#### Method-level metrics

- cyclomatic complexity
- method LOC
- parameter count
- maximum nesting depth
- branch count

### Analysis notes

- metrics that rely on cross-class relationships such as DIT, NOC, and some coupling calculations are most accurate in multi-file or folder mode
- single-snippet mode should still return partial results where meaningful and explicitly mark metrics that are incomplete
- parse failures in one file must not abort the full batch analysis

## Product Behavior

### Main workflow

1. user selects one input mode
2. frontend validates file type and size
3. frontend sends normalized input payload to backend
4. backend parses Java source into AST structures
5. backend computes method metrics, class metrics, and project aggregates
6. backend returns structured JSON results with warnings and parse errors
7. frontend renders cards, charts, tables, and risk summaries
8. user optionally exports CSV or Markdown reports

### Output areas

The results page should include:

- overview cards for key project metrics
- chart area for metric distribution and top-risk objects
- class table with sorting and filtering
- method table with sorting and filtering
- risk panel listing recommended improvement targets
- export actions for CSV and Markdown

### Risk summaries

To support the "evaluation and improvement" part of the course report, the UI should generate interpretable findings such as:

- methods with very high cyclomatic complexity
- classes with high WMC
- classes with unusually high coupling
- classes that may be "God Class" candidates
- files that failed to parse and require manual inspection

## Frontend Design

### Pages and modules

- upload and input workspace
- analysis dashboard
- metric explanation drawer or panel
- export actions

### Input experience

The upload workspace should support:

- code textarea for quick demonstrations
- file picker for one or more Java files
- folder picker for project-like analysis
- sample project shortcuts using bundled demo inputs

### Visualization

Use ECharts for:

- top 10 most complex methods
- class WMC distribution
- coupling distribution
- comment ratio composition

Tables should allow sorting by key metrics so the user can quickly identify risky objects during demos.

## Backend Design

### Major modules

- controller layer for input endpoints and result delivery
- preprocessing layer to normalize text, file, and folder inputs
- parser layer based on Eclipse JDT
- metric calculators for project, class, and method scopes
- aggregation layer for summaries, warnings, and export data

### Proposed package structure

```text
com.metrics
├── MetricsApplication
├── controller
│   └── MetricsController
├── service
│   └── MetricsAnalysisService
├── parser
│   ├── JavaSourceParser
│   └── ParseResult
├── analyzer
│   ├── ProjectMetricsAnalyzer
│   ├── ClassMetricsAnalyzer
│   ├── MethodMetricsAnalyzer
│   ├── ComplexityCalculator
│   └── CKMetricsCalculator
├── model
│   ├── request
│   ├── response
│   └── metrics
└── util
    └── SourceNormalizationUtils
```

### API shape

The system should expose a small set of analysis endpoints.

- `POST /api/metrics/analyze/text`
- `POST /api/metrics/analyze/files`
- `POST /api/metrics/analyze/folder`
- `GET /api/metrics/health`

The response should include:

- project summary
- class metrics list
- method metrics list
- chart-ready aggregates
- warnings
- parse errors
- export-ready metadata

## Error Handling

- reject non-Java uploads with clear UI feedback
- continue batch processing when one file fails
- return per-file parse errors in a readable form
- mark cross-file metrics as partial when the input is insufficient
- handle empty input, oversized input, and invalid encoding gracefully

## Testing Strategy

### Backend testing

- JUnit 5 unit tests for each metric calculator
- AST parsing tests against small Java fixtures
- regression tests for project, class, and method result consistency
- MockMvc integration tests for text, file, multi-file, and folder upload APIs

### Frontend testing

- Vitest unit tests for upload flow and result rendering logic
- component tests for overview cards, charts, and tables
- error state tests for parse failures, empty input, and invalid file types

### Fixtures

The repository should include demo fixture code for:

- a simple well-structured class
- a high-complexity class
- a high-coupling class
- a small multi-file inheritance example

These fixtures should be reused in tests, screenshots, and demo rehearsals.

## Documentation and Report Support

To satisfy the course report requirements, the repository should include:

- `README.md` with setup, run steps, and feature summary
- `docs/metric-definitions.md` with metric meaning, formulas, and interpretation
- `docs/report-outline.md` mapped to the course report sections
- exported Markdown report support from the application
- sample screenshots and sample inputs for the final write-up

The report structure should clearly cover:

- requirement analysis
- data and non-functional requirements
- system architecture
- key module design
- metric algorithms
- implementation results
- accuracy, usability, and effectiveness analysis
- improvement suggestions

The final report should also compare tool output on bundled sample cases against manually expected values for selected metrics. This gives the project a clear way to discuss accuracy rather than only showing screenshots.

## Local Run and Demo Packaging

### Development mode

- backend runs on `http://localhost:8080`
- frontend runs on `http://localhost:5173`
- frontend proxies API requests to backend during development

### Demo mode

- build the frontend static files
- copy the built frontend into Spring Boot static resources
- run one packaged backend jar
- open `http://localhost:8080` for the full demo

### Environment baseline

- JDK 17
- Maven 3.9 or newer
- Node.js 18 or 20
- modern Chromium-based browser for folder selection support

## Quality Constraints

- no database dependency in v1
- clear separation between parsing, metric calculation, and presentation
- testable metric calculators with deterministic fixture inputs
- concise local startup steps for classroom demonstration
- portable local execution across common Windows development environments

## Open Decisions Resolved

- project track: software metrics automation tool
- deployment target: local runnable demo
- first version input scope: includes file upload and folder scanning
- implementation style: enhanced edition, not minimal edition

## Implementation Boundaries

The first implementation should prioritize a complete, credible course project over an overly broad platform. If time pressure appears during implementation, the order of protection should be:

1. reliable Java parsing and core metrics
2. clear dashboard and risk summaries
3. exportable report support
4. extended visual polish

## Design Summary

The chosen design produces a local Java metrics platform that matches the course theme, uses the AST-based technical path recommended by the assignment, and extends the original implementation note into a stronger project deliverable through richer metrics, folder analysis, visual explanation, testing, and report-oriented outputs.
