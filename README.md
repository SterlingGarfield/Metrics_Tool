# Metrics Tool Workbench

[中文说明](README.zh-CN.md)

## Core Mainlines

The course project is explicitly converged to three mainlines:

- code metrics
- design diagram metrics
- project estimation

## Environment

- JDK 17
- Maven 3.9+
- Node.js 24
- Python 3.11 (for `diagram-recognition-service`)

## Quick Start

### 1. Bootstrap recognition runtime

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -SkipTests
```

### 2. Run frontend + backend development mode

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev.ps1
```

- backend: `http://localhost:8080`
- frontend: `http://localhost:5173`

## API Workflows

- code metrics:
  - `POST /api/metrics/analyze/text`
  - `POST /api/metrics/analyze/files`
  - `POST /api/metrics/analyze/folder`
- diagram analysis:
  - `POST /api/design/analyze/structured`
  - `POST /api/design/analyze/image`
- recognition status:
  - `GET /api/recognition/health`
  - `GET /api/recognition/models/status`
- project estimation:
  - `POST /api/estimate/project`

Project estimation now supports both `ucp` and `function_point` paths. See `docs/estimation-method.md` for formulas and traceability fields.

## Test Commands

### Python recognition service

```powershell
cd .\diagram-recognition-service
.\.venv311\Scripts\python.exe -m pytest tests\test_runtime_environment.py -q
.\.venv311\Scripts\python.exe -m pytest tests -q --basetemp "C:\Users\<user>\AppData\Local\Temp\codex-pytest"
```

### Java backend

```powershell
mvn -f .\metrics-backend\pom.xml -q test
```

### Frontend

```powershell
cd .\metrics-frontend
npm run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js
```

## Demo Packaging

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
```

## Course Defense References

- Course requirement matrix: `docs/course-requirement-matrix.zh-CN.md`
- LK course alignment mapping: `docs/lk-course-alignment.zh-CN.md`
- Metric definitions: `docs/metric-definitions.md`
