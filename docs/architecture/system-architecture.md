# System Architecture

## Components

- `metrics-frontend` (Vue): unified workbench UI
- `metrics-backend` (Spring Boot): code metrics + orchestration + estimation
- `diagram-recognition-service` (FastAPI): structured parser + image recognition pipeline
- `models/` cache: OCR and recognition artifacts

## Responsibility Split

- Backend computes Java AST metrics and LK presentation fields.
- Python service computes diagram analysis from structured/image inputs.
- Backend proxies design recognition requests and merges responses into stable contracts.
- Estimation endpoint computes project-entity indicators from measured inputs.

## Integration Paths

- Frontend -> Backend
  - `/api/metrics/*`
  - `/api/design/analyze/*`
  - `/api/recognition/*`
  - `/api/estimate/project`
- Backend -> Recognition service
  - `/recognition/analyze/structured`
  - `/recognition/analyze/image`
  - `/recognition/health`
  - `/recognition/models/status`

## Contract Envelope

Top-level response is organized as:

- `codeMetrics`
- `diagramAnalysis`
- `projectEstimation`
- `riskFindings`

