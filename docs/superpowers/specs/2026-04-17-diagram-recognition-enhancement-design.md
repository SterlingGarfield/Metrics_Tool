# Diagram Recognition Enhancement Design

## Context

The current project already delivers a local Java software metrics tool with:

- Java source analysis through Eclipse JDT ASTParser
- code upload, multi-file upload, and folder scan
- core CK metrics and traditional code metrics
- a Vue frontend with charts, tables, and exports

It does not yet fully satisfy the course requirements that ask for:

- broader object-oriented metrics coverage, including stronger LK-related points
- software entity metrics beyond LoC, such as workload, cost, development time, and staffing
- design-diagram input support for class diagrams, flowcharts, and use case diagrams
- automation around design-oriented measurement, not only source-code measurement

## Goal

Extend the existing system into a dual-track metrics platform that supports both code metrics and design-diagram metrics, including image recognition and project estimation.

The enhanced platform must:

- continue to analyze Java code with ASTParser-based metrics
- accept structured design files and image-based design diagrams
- recognize class diagrams, flowcharts, and use case diagrams from images
- compute diagram metrics and combine them with code metrics
- estimate workload, cost, schedule, and suggested staffing from measured project size and complexity
- remain locally runnable under `D:\Projects\SQA\Metrics_Tool`

## Non-Goals

- cloud deployment
- general-purpose recognition for arbitrary whiteboard photos beyond first-version engineering limits
- replacement of the existing Java code analysis path
- precision claims equivalent to industrial estimation tools

## Success Criteria

- users can upload Java code and still receive the current code metrics workflow
- users can upload PlantUML or Mermaid files for class, flow, and use case analysis
- users can upload PNG or JPG design diagrams and receive recognized elements, relationships, confidence, and derived metrics
- the system can produce project estimation outputs for workload, cost, schedule, and staffing
- the frontend presents code metrics, diagram metrics, and estimation results in a unified workflow
- all runtime assets, models, caches, builds, and deployment steps stay inside `D:\Projects\SQA\Metrics_Tool`

## Current Gap Analysis

### Already covered

- Java code input, single-file upload, multi-file upload, and folder scan
- CK metrics: WMC, CBO, RFC, LCOM, DIT, NOC
- traditional metrics: LoC, cyclomatic complexity, branch count, nesting depth, parameter count
- ASTParser-based Java analysis

### Missing or incomplete

- diagram inputs are not supported
- LK-related metrics are not expressed strongly enough for course presentation
- workload, cost, schedule, and staffing are not computed
- design-oriented metrics for class diagrams, flowcharts, and use case diagrams are missing
- structured and image-based design analysis flows do not exist

## Recommended Architecture

Use a dual-backend architecture with clear responsibility boundaries.

### Java backend

Keep Spring Boot as the primary application backend.

Responsibilities:

- Java AST-based code metrics
- unified API surface for the frontend
- result aggregation across code metrics, diagram metrics, and estimations
- report exports and risk summaries
- internal orchestration when calling the Python recognition service

### Python recognition service

Add a separate Python service dedicated to design-diagram recognition.

Responsibilities:

- image preprocessing
- OCR for mixed Chinese and English diagram text
- visual element detection
- relationship recovery
- transformation of recognized diagrams into normalized graph JSON
- extraction of diagram metrics

### Frontend

Extend the existing Vue application.

Responsibilities:

- code metrics input and display
- design-diagram upload and analysis display
- recognition preview and issue feedback
- estimation parameter input and result display
- integrated export of the full project analysis

## Deployment Model

### Development mode

- Java backend runs locally on `localhost:8080`
- Python recognition service runs locally on a dedicated port such as `localhost:8090`
- Vue frontend runs on `localhost:5173`

### Demo mode

- frontend is built and copied into Spring Boot static resources
- Java backend serves the packaged frontend
- Java backend calls the Python service for diagram-recognition requests
- model files are cached under `D:\Projects\SQA\Metrics_Tool\models`

### Model strategy

The first run may download OCR and recognition models. Later runs must work offline as long as the model cache remains in the project directory.

## Supported Inputs

### Code inputs

- pasted Java code
- single Java file upload
- multiple Java file upload
- Java source folder scan

### Structured design inputs

- PlantUML class diagrams
- PlantUML flow-like activity definitions
- PlantUML use case diagrams
- Mermaid class diagrams
- Mermaid flowcharts
- Mermaid use case-like diagram definitions if represented in the supported grammar subset

### Image design inputs

- PNG class diagrams
- JPG class diagrams
- PNG flowcharts
- JPG flowcharts
- PNG use case diagrams
- JPG use case diagrams

### User parameters

- cost rate per person-month
- nominal productivity assumptions
- team capacity assumptions
- optional calibration factors for estimation

## Functional Scope

### Code metrics track

Retain and extend the existing metrics.

#### Code project metrics

- total files
- total classes
- total methods
- total LoC
- blank lines
- comment lines
- comment ratio
- high-risk class count
- high-risk method count

#### Code class metrics

- WMC
- CBO
- RFC
- LCOM
- DIT
- NOC
- NOM
- NOA
- public method count
- class LoC
- class comment ratio

#### Code method metrics

- cyclomatic complexity
- method LoC
- parameter count
- maximum nesting depth
- branch count

### LK-related presentation metrics

To strengthen course alignment, the enhanced system should explicitly present a grouped set of LK-related points based on currently derivable object-oriented structure signals.

Recommended presentation fields:

- class count
- method count
- attribute count
- relationship count
- average methods per class
- average attributes per class
- inheritance depth distribution
- class relation density

These may be shown as a dedicated “LK-related view” in the UI and report rather than as a separate academic implementation claim.

### Diagram metrics track

#### Class-diagram metrics

- class count
- attribute count
- method count
- inheritance relation count
- association count
- dependency count
- aggregation count
- composition count
- relationship density
- object design complexity score

#### Flowchart metrics

- node count
- decision node count
- terminal node count
- path count estimate
- control-flow complexity score
- estimated cyclomatic complexity from the flow structure
- maximum branch fan-out

#### Use-case metrics

- actor count
- use case count
- actor-use case association count
- include relation count
- extend relation count
- generalization relation count
- use case size score
- interaction complexity score

### Estimation track

The system must add project-entity estimation outputs.

#### Estimation outputs

- estimated workload
- estimated cost
- estimated development time
- suggested staffing level
- estimation basis summary

#### Estimation basis

Use a transparent heuristic model that combines:

- code size indicators such as LoC and class count
- diagram size indicators such as use case count, decision-node count, and relation count
- user-provided productivity and cost parameters
- configurable weighting constants stored in project configuration

The system must explain what inputs contributed to the estimate so that report writing remains defensible.

## Recognition Pipeline

### Pipeline stages

The Python recognition service must follow a structured six-stage pipeline.

1. image preprocessing
2. OCR for mixed Chinese and English text
3. diagram-element detection
4. relation recovery
5. normalized graph construction
6. metric extraction

### Image preprocessing

- grayscale normalization
- denoising
- thresholding or binarization
- line enhancement
- skew correction
- optional region segmentation

### OCR

- mixed Chinese and English recognition
- coordinate-preserving output
- confidence score for each text block
- fallback issue reporting when OCR quality is low

### Diagram-element detection

Detect diagram-specific elements such as:

- rectangles and compartments for class diagrams
- diamonds, process blocks, and terminal shapes for flowcharts
- stick-figure actors and ellipse use cases for use case diagrams
- arrows, connectors, and edge directions
- text blocks associated with elements

### Relation recovery

Recover graph structure from geometry and OCR results.

Examples:

- parent-child inheritance in class diagrams
- association and dependency edges between classes
- branch transitions in flowcharts
- actor-to-use-case associations
- include and extend relations in use case diagrams

### Graph output

The Python service must output a normalized graph JSON object with fields such as:

- `diagramType`
- `elements`
- `relations`
- `textBlocks`
- `metrics`
- `confidence`
- `issues`

This normalized form is the only contract the Java backend consumes.

## Stability Rules

Image recognition must not silently produce low-confidence metrics.

### Required behavior

- reject or flag images with poor confidence
- report unresolved relations explicitly
- include warnings for overlapping nodes, unclear arrows, or low OCR quality
- avoid pretending complete certainty when the recognition graph is incomplete

### Acceptance rule

The system should only mark the image-analysis result as “directly measurable” when confidence and graph completeness pass configured thresholds.

## Service Integration

### Java to Python contract

The Java backend calls the Python recognition service through internal HTTP APIs.

Recommended endpoints:

- `POST /recognition/analyze/image`
- `POST /recognition/analyze/structured`
- `GET /recognition/health`
- `GET /recognition/models/status`

### Unified frontend response

The Java backend should return a combined response with four major sections:

- `codeMetrics`
- `diagramAnalysis`
- `projectEstimation`
- `riskFindings`

This keeps the frontend simple and maintains one primary application contract.

## Frontend Enhancements

### Navigation

Split the main interface into three primary analysis areas:

- code metrics
- design-diagram metrics
- project estimation

### Code metrics page

Retain the existing upload modes and result presentation.

### Design-diagram page

The page should let the user:

- choose diagram category: class, flow, or use case
- choose input type: structured file or image file
- upload the input
- preview recognized structure
- inspect diagram metrics and issues

### Estimation page

The page should let the user:

- review measured code and diagram size signals
- enter productivity and cost assumptions
- run estimation
- inspect workload, cost, schedule, and staffing outputs

### Recognition preview

The design-diagram page should visually expose:

- detected elements
- recovered relationships
- recognized labels
- confidence level
- issue list

This is important for explainability during defense and report writing.

## Data Model Additions

### Diagram analysis model

Add a new result model representing:

- diagram type
- source type: structured or image
- recognized elements
- recognized relations
- derived metrics
- confidence summary
- issues and warnings

### Estimation model

Add a model representing:

- workload value and units
- cost value and currency assumptions
- development-time estimate
- suggested staffing size
- parameter inputs used
- explanation text for the estimate basis

### Unified report model

Extend export generation so the final report can include:

- code-metrics section
- design-metrics section
- estimation section
- risk and issue section

## Error Handling

### Structured inputs

- reject unsupported grammar variants clearly
- return parse errors with line hints where possible
- classify unsupported diagram constructs separately from malformed input

### Image inputs

- reject unsupported file types
- reject empty images or unreadable files
- return low-confidence warnings
- return partial recognition with issue lists when only some nodes or edges are recoverable

### Estimation

- require essential user parameters before final estimation
- provide safe defaults only where academically defensible
- label estimates as heuristic outputs, not factual project commitments

## Testing Strategy

### Java backend tests

- unit tests for code metrics analyzers
- integration tests for unified response assembly
- endpoint tests for structured-diagram and image-analysis orchestration
- estimation tests for parameter handling and output consistency

### Python service tests

- OCR pipeline tests on mixed Chinese-English fixtures
- diagram-element detection tests for class, flow, and use case images
- graph-recovery tests
- metric extraction tests from normalized graph output

### Frontend tests

- upload flow tests for code and diagram modes
- diagram preview rendering tests
- estimation-page interaction tests
- report export tests

### Fixtures

Add stable sample assets under project samples for:

- structured class diagrams
- structured flowcharts
- structured use case diagrams
- image class diagrams
- image flowcharts
- image use case diagrams
- mixed-language labels

## Documentation And Report Support

Add or extend project docs so the course report can directly reference them.

Required documentation additions:

- functional requirements description using use cases or data-flow views
- data requirements using ER or UML class diagrams
- non-functional requirements in structured text
- system architecture diagrams
- detailed module descriptions
- UML description of the automated measurement and recognition workflow
- estimation-method explanation
- recognition limitations and evaluation notes

## Implementation Boundaries

This enhancement is large. To keep it implementable, protect scope in this order:

1. structured-diagram parsing and unified data model
2. Python recognition service skeleton with stable API contract
3. image recognition pipeline with confidence-aware outputs
4. project estimation module
5. frontend integration and export extension

## Design Summary

The enhanced design turns the current Java metrics tool into a multi-input software-metrics platform that combines AST-based code analysis, structured-diagram analysis, image-based diagram recognition, and transparent project estimation. It closes the major course-requirement gaps while preserving the current local deployment model under `D:\Projects\SQA\Metrics_Tool`.
