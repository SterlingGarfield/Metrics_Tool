# Non-Functional Requirements

## Runtime And Deployment

- Local-first execution under `D:\Projects\SQA\Metrics_Tool`
- Java backend and Python recognition service run independently and integrate via HTTP
- Frontend can run in dev mode and can be packaged into backend static resources

## Reliability

- Recognition APIs must return structured `issues[]` instead of silent failure.
- Health/status endpoints must expose service/model readiness.
- Warm-up tooling must provide diagnostics for Python runtime and model cache status.

## Reproducibility

- Python runtime setup uses a dedicated `.venv311` workflow.
- Runtime and model-cache paths stay project-local for demo repeatability.
- Tests are runnable with explicit Windows `--basetemp` override when temp ACL issues occur.

## Performance

- Code metrics should respond within interactive latency for small/medium course projects.
- Diagram analysis should provide progress-transparent confidence output even under partial recognition.

## Usability

- A unified frontend should allow code metrics, diagram analysis, and estimation from one workspace.
- Export output should be report-ready (CSV/Markdown + JSON payloads).

