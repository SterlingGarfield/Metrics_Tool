# Diagram Recognition Enhancement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend the existing local Java metrics tool so it can analyze structured and image-based design diagrams, compute diagram metrics, strengthen LK-related object-oriented presentation metrics, estimate workload/cost/schedule/staffing, and produce report-ready requirement and architecture artifacts while keeping all generated assets under `D:\Projects\SQA\Metrics_Tool`.

**Architecture:** Keep Spring Boot as the primary backend for code metrics, unified API orchestration, LK-related summary generation, and estimation. Add a FastAPI-based Python recognition service for structured diagram parsing, mixed Chinese-English OCR, image element detection, relation recovery, and diagram metric extraction. Extend the Vue frontend with dedicated code-metrics, design-diagram, and estimation workspaces, then package the frontend into Spring Boot for demo mode while running the Python service alongside it locally.

**Tech Stack:** Java 17, Spring Boot 3.3, Maven, Eclipse JDT ASTParser, Python 3.11, FastAPI, Pydantic, OpenCV, PaddleOCR, NetworkX, Pytest, Vue 3, Vite, Axios, ECharts, Vitest, PowerShell

---

## File Structure

### Backend files to modify

- Modify: `metrics-backend/pom.xml`
- Modify: `metrics-backend/src/main/resources/application.yml`
- Modify: `metrics-backend/src/main/java/com/metrics/controller/MetricsController.java`
- Modify: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java`
- Modify: `metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java`
- Modify: `metrics-backend/src/test/java/com/metrics/controller/MetricsControllerMultipartTest.java`
- Modify: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

### Backend files to create

- Create: `metrics-backend/src/main/java/com/metrics/config/RecognitionServiceProperties.java`
- Create: `metrics-backend/src/main/java/com/metrics/config/RestClientConfig.java`
- Create: `metrics-backend/src/main/java/com/metrics/client/RecognitionServiceClient.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/DesignAnalysisController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/RecognitionStatusController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/EstimationController.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/StructuredDiagramAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/EstimateProjectRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramAnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramElement.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramRelation.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramMetricValue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ConfidenceSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/RecognitionIssue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ProjectEstimation.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/EstimationBasis.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/DesignAnalysisService.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/EstimationService.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/DesignAnalysisControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/RecognitionStatusControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/EstimationControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/DesignAnalysisServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/EstimationServiceTest.java`

### Python recognition service

- Create: `diagram-recognition-service/requirements.txt`
- Create: `diagram-recognition-service/pytest.ini`
- Create: `diagram-recognition-service/app/main.py`
- Create: `diagram-recognition-service/app/config.py`
- Create: `diagram-recognition-service/app/api/routes.py`
- Create: `diagram-recognition-service/app/models/contracts.py`
- Create: `diagram-recognition-service/app/services/models/download_manager.py`
- Create: `diagram-recognition-service/app/services/structured/plantuml_parser.py`
- Create: `diagram-recognition-service/app/services/structured/mermaid_parser.py`
- Create: `diagram-recognition-service/app/services/structured/metric_extractor.py`
- Create: `diagram-recognition-service/app/services/image/preprocess.py`
- Create: `diagram-recognition-service/app/services/image/ocr.py`
- Create: `diagram-recognition-service/app/services/image/detect.py`
- Create: `diagram-recognition-service/app/services/image/recover.py`
- Create: `diagram-recognition-service/app/services/image/metric_extractor.py`
- Create: `diagram-recognition-service/app/services/image/confidence.py`
- Create: `diagram-recognition-service/app/tools/warm_models.py`
- Create: `diagram-recognition-service/tests/test_health.py`
- Create: `diagram-recognition-service/tests/test_structured_endpoints.py`
- Create: `diagram-recognition-service/tests/test_structured_metrics.py`
- Create: `diagram-recognition-service/tests/test_image_pipeline.py`
- Create: `diagram-recognition-service/tests/test_models_status.py`

### Frontend files to modify

- Modify: `metrics-frontend/package.json`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/api/metrics.js`
- Modify: `metrics-frontend/src/composables/useAnalysis.js`
- Modify: `metrics-frontend/src/utils/exporters.js`
- Modify: `metrics-frontend/src/styles/theme.css`

### Frontend files to create

- Create: `metrics-frontend/src/components/AnalysisTabs.vue`
- Create: `metrics-frontend/src/components/CodeMetricsWorkspace.vue`
- Create: `metrics-frontend/src/components/DiagramAnalysisWorkspace.vue`
- Create: `metrics-frontend/src/components/RecognitionPreview.vue`
- Create: `metrics-frontend/src/components/DiagramMetricsPanel.vue`
- Create: `metrics-frontend/src/components/LkMetricsPanel.vue`
- Create: `metrics-frontend/src/components/EstimationWorkspace.vue`
- Create: `metrics-frontend/src/components/EstimationSummary.vue`
- Create: `metrics-frontend/src/components/__tests__/DiagramWorkspace.test.js`
- Create: `metrics-frontend/src/components/__tests__/EstimationFlow.test.js`

### Samples, scripts, and docs

- Create: `models/.gitkeep`
- Create: `samples/diagram-inputs/structured/class/library-domain.puml`
- Create: `samples/diagram-inputs/structured/flow/order-approval.mmd`
- Create: `samples/diagram-inputs/structured/usecase/campus-repair.puml`
- Create: `samples/diagram-inputs/images/class/library-domain-zh-en.png`
- Create: `samples/diagram-inputs/images/flow/order-approval-zh-en.jpg`
- Create: `samples/diagram-inputs/images/usecase/campus-repair-zh-en.png`
- Create: `docs/requirements/functional-use-cases.md`
- Create: `docs/requirements/data-model-class-diagram.md`
- Create: `docs/requirements/non-functional-requirements.md`
- Create: `docs/architecture/system-architecture.md`
- Create: `docs/architecture/measurement-workflow.md`
- Create: `docs/estimation-method.md`
- Modify: `docs/metric-definitions.md`
- Modify: `docs/report-outline.md`
- Modify: `README.md`
- Modify: `scripts/dev.ps1`
- Modify: `scripts/build-demo.ps1`
- Modify: `scripts/run-demo.ps1`
- Modify: `scripts/smoke-demo.ps1`
- Create: `scripts/bootstrap-models.ps1`

## Contract Snapshot

Use the following backend-to-frontend shape as the working contract once the enhancement is complete:

```json
{
  "codeMetrics": {
    "projectSummary": {},
    "classMetrics": [],
    "methodMetrics": [],
    "lkPresentation": {
      "classCount": 0,
      "methodCount": 0,
      "attributeCount": 0,
      "relationshipCount": 0,
      "averageMethodsPerClass": 0,
      "averageAttributesPerClass": 0,
      "relationDensity": 0,
      "inheritanceDepthDistribution": []
    },
    "parseIssues": [],
    "partial": false
  },
  "diagramAnalysis": {
    "diagramType": "class",
    "sourceType": "image",
    "elements": [],
    "relations": [],
    "metrics": [],
    "confidence": {
      "overall": 0.91,
      "directlyMeasurable": true
    },
    "issues": []
  },
  "projectEstimation": {
    "workloadPersonMonths": 3.6,
    "cost": 54000,
    "scheduleMonths": 2.0,
    "suggestedStaffing": 2,
    "basis": {}
  },
  "riskFindings": []
}
```

Use the following fixed enum vocabulary across Java, Python, and Vue:

- `diagramType`: `class`, `flow`, `usecase`
- `sourceType`: `structured`, `image`
- `issueLevel`: `info`, `warning`, `error`

## Task 1: Lock The Backend Contracts And Orchestration Tests

**Files:**
- Modify: `metrics-backend/pom.xml`
- Modify: `metrics-backend/src/main/resources/application.yml`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/config/RecognitionServiceProperties.java`
- Create: `metrics-backend/src/main/java/com/metrics/config/RestClientConfig.java`
- Create: `metrics-backend/src/main/java/com/metrics/client/RecognitionServiceClient.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/DesignAnalysisController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/RecognitionStatusController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/EstimationController.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/StructuredDiagramAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/EstimateProjectRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramAnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramElement.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramRelation.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DiagramMetricValue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ConfidenceSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/RecognitionIssue.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/ProjectEstimation.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/EstimationBasis.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/DesignAnalysisControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/RecognitionStatusControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/EstimationControllerTest.java`

- [ ] **Step 1: Write the failing Spring MVC tests for the new public API surface.**

Cover these endpoints exactly:

- `POST /api/design/analyze/structured`
- `POST /api/design/analyze/image`
- `GET /api/recognition/health`
- `POST /api/estimate/project`

Assert these behaviors:

- request validation rejects missing `diagramType` and missing files
- health endpoint proxies Python service readiness into a simple JSON response
- estimation endpoint returns workload, cost, schedule, staffing, and basis text
- design-analysis responses include `diagramType`, `sourceType`, `metrics`, `confidence`, and `issues`
- the top-level contract already reserves `codeMetrics`, `diagramAnalysis`, `projectEstimation`, and `riskFindings`

- [ ] **Step 2: Run the new backend tests before implementation.**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q -Dtest=DesignAnalysisControllerTest,RecognitionStatusControllerTest,EstimationControllerTest test
```

Expected: FAIL because the controllers, request models, and response contracts do not exist yet.

- [ ] **Step 3: Implement the backend contracts and wiring with fixed configuration.**

Add these exact application properties:

```yaml
recognition:
  base-url: http://localhost:8090
  model-cache-path: D:/Projects/SQA/Metrics_Tool/models
  connect-timeout-ms: 5000
  read-timeout-ms: 60000
```

Implementation notes:

- keep the existing code-metrics endpoints intact
- use a Spring-managed `RestTemplate` or `RestClient` bean for Java-to-Python calls
- model `CodeMetricsResult` as a wrapper around the current project/class/method metrics payload plus the new `lkPresentation` block
- keep all new response models serializable with Jackson records or plain DTOs
- make `RecognitionStatusController` a thin proxy over `RecognitionServiceClient.health()` and `RecognitionServiceClient.modelsStatus()`

- [ ] **Step 4: Re-run the new backend tests and then the full backend suite.**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q -Dtest=DesignAnalysisControllerTest,RecognitionStatusControllerTest,EstimationControllerTest test
mvn -f metrics-backend/pom.xml -q test
```

Expected: PASS.

- [ ] **Step 5: Commit the contract layer checkpoint.**

```powershell
git add metrics-backend/pom.xml metrics-backend/src/main/resources/application.yml metrics-backend/src/main/java/com/metrics/config metrics-backend/src/main/java/com/metrics/client metrics-backend/src/main/java/com/metrics/controller/DesignAnalysisController.java metrics-backend/src/main/java/com/metrics/controller/RecognitionStatusController.java metrics-backend/src/main/java/com/metrics/controller/EstimationController.java metrics-backend/src/main/java/com/metrics/model/request metrics-backend/src/main/java/com/metrics/model/response metrics-backend/src/test/java/com/metrics/controller/DesignAnalysisControllerTest.java metrics-backend/src/test/java/com/metrics/controller/RecognitionStatusControllerTest.java metrics-backend/src/test/java/com/metrics/controller/EstimationControllerTest.java
```

## Task 2: Build The Python Recognition Service And Structured Diagram Parsers

**Files:**
- Create: `diagram-recognition-service/requirements.txt`
- Create: `diagram-recognition-service/pytest.ini`
- Create: `diagram-recognition-service/app/main.py`
- Create: `diagram-recognition-service/app/config.py`
- Create: `diagram-recognition-service/app/api/routes.py`
- Create: `diagram-recognition-service/app/models/contracts.py`
- Create: `diagram-recognition-service/app/services/structured/plantuml_parser.py`
- Create: `diagram-recognition-service/app/services/structured/mermaid_parser.py`
- Create: `diagram-recognition-service/app/services/structured/metric_extractor.py`
- Create: `diagram-recognition-service/tests/test_health.py`
- Create: `diagram-recognition-service/tests/test_structured_endpoints.py`
- Create: `diagram-recognition-service/tests/test_structured_metrics.py`
- Create: `samples/diagram-inputs/structured/class/library-domain.puml`
- Create: `samples/diagram-inputs/structured/flow/order-approval.mmd`
- Create: `samples/diagram-inputs/structured/usecase/campus-repair.puml`

- [ ] **Step 1: Create the Python virtual environment and pin the recognition-service dependencies.**

Use this bootstrap command:

```powershell
py -3.11 -m venv diagram-recognition-service/.venv
.\diagram-recognition-service\.venv\Scripts\python.exe -m pip install --upgrade pip
```

Pin these runtime dependencies in `diagram-recognition-service/requirements.txt`:

- `fastapi`
- `uvicorn[standard]`
- `pydantic`
- `python-multipart`
- `networkx`
- `opencv-python`
- `pillow`
- `paddleocr`
- `pytest`
- `httpx`

- [ ] **Step 2: Write the failing pytest coverage for health and structured parsing.**

Cover these cases:

- `GET /recognition/health` returns service name, status, and model-cache path
- `POST /recognition/analyze/structured` parses PlantUML class diagrams into classes and relations
- `POST /recognition/analyze/structured` parses Mermaid flowcharts into nodes, decisions, and estimated paths
- `POST /recognition/analyze/structured` parses PlantUML use case diagrams into actors, use cases, and include or extend relations

- [ ] **Step 3: Run pytest before implementation.**

Run:

```powershell
.\diagram-recognition-service\.venv\Scripts\python.exe -m pip install -r diagram-recognition-service/requirements.txt
.\diagram-recognition-service\.venv\Scripts\python.exe -m pytest diagram-recognition-service/tests/test_health.py diagram-recognition-service/tests/test_structured_endpoints.py diagram-recognition-service/tests/test_structured_metrics.py -q
```

Expected: FAIL because the FastAPI app, routes, and parser modules do not exist yet.

- [ ] **Step 4: Implement the structured parsing service with one normalized graph contract.**

Implementation notes:

- route structured requests through one handler that dispatches by `diagramType` and file suffix
- support these structured inputs in the first pass:
- PlantUML class diagrams with `class`, `interface`, `abstract`, and relation arrows
- Mermaid class diagrams with class blocks and relation arrows
- Mermaid flowcharts with `flowchart` or `graph` syntax and decision nodes containing `{}`
- PlantUML use case diagrams with `actor`, `usecase`, `-->`, `..>`, `<<include>>`, and `<<extend>>`
- calculate metrics in Python, not in Java, so the same logic can be reused by image recognition after graph recovery

- [ ] **Step 5: Re-run the targeted pytest commands, then launch the service locally.**

Run:

```powershell
.\diagram-recognition-service\.venv\Scripts\python.exe -m pytest diagram-recognition-service/tests/test_health.py diagram-recognition-service/tests/test_structured_endpoints.py diagram-recognition-service/tests/test_structured_metrics.py -q
.\diagram-recognition-service\.venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8090
```

Expected: tests PASS and the service starts on `http://127.0.0.1:8090`.

- [ ] **Step 6: Commit the structured-recognition checkpoint.**

```powershell
git add diagram-recognition-service samples/diagram-inputs/structured
```

## Task 3: Implement Model Management And The Image Recognition Pipeline

**Files:**
- Create: `models/.gitkeep`
- Create: `diagram-recognition-service/app/services/models/download_manager.py`
- Create: `diagram-recognition-service/app/services/image/preprocess.py`
- Create: `diagram-recognition-service/app/services/image/ocr.py`
- Create: `diagram-recognition-service/app/services/image/detect.py`
- Create: `diagram-recognition-service/app/services/image/recover.py`
- Create: `diagram-recognition-service/app/services/image/metric_extractor.py`
- Create: `diagram-recognition-service/app/services/image/confidence.py`
- Create: `diagram-recognition-service/app/tools/warm_models.py`
- Create: `diagram-recognition-service/tests/test_image_pipeline.py`
- Create: `diagram-recognition-service/tests/test_models_status.py`
- Create: `samples/diagram-inputs/images/class/library-domain-zh-en.png`
- Create: `samples/diagram-inputs/images/flow/order-approval-zh-en.jpg`
- Create: `samples/diagram-inputs/images/usecase/campus-repair-zh-en.png`

- [ ] **Step 1: Write failing image-pipeline and model-status tests before adding the implementation.**

Cover these cases:

- `GET /recognition/models/status` reports whether OCR assets are present under `D:/Projects/SQA/Metrics_Tool/models`
- `POST /recognition/analyze/image` returns recognized elements, relations, confidence, and issues for one class, one flow, and one use case fixture
- low-confidence fixtures produce `directlyMeasurable=false` and at least one warning issue

- [ ] **Step 2: Run pytest and confirm the new image tests fail first.**

Run:

```powershell
.\diagram-recognition-service\.venv\Scripts\python.exe -m pytest diagram-recognition-service/tests/test_image_pipeline.py diagram-recognition-service/tests/test_models_status.py -q
```

Expected: FAIL because the image pipeline and model manager do not exist yet.

- [ ] **Step 3: Implement the image pipeline in six explicit stages.**

Stage rules:

- preprocessing: grayscale, denoise, threshold, line enhancement, skew correction
- OCR: preserve bounding boxes and per-block confidence for mixed Chinese-English labels
- detection: find rectangles, ellipses, diamonds, arrows, connectors, and actor stick figures
- recovery: convert geometry into a normalized graph and attach nearby text blocks
- metrics: derive diagram metrics from the graph using the same metric vocabulary as structured parsing
- confidence: mark the result directly measurable only when OCR, graph completeness, and relation coverage pass thresholds

Model-management rules:

- keep every downloaded model under `D:/Projects/SQA/Metrics_Tool/models`
- expose the cache state through `GET /recognition/models/status`
- provide `app/tools/warm_models.py` so the demo machine can download once and then run offline

- [ ] **Step 4: Warm the models and re-run the image tests.**

Run:

```powershell
$env:PADDLEOCR_HOME = 'D:/Projects/SQA/Metrics_Tool/models/paddleocr'
.\diagram-recognition-service\.venv\Scripts\python.exe -m app.tools.warm_models
.\diagram-recognition-service\.venv\Scripts\python.exe -m pytest diagram-recognition-service/tests/test_image_pipeline.py diagram-recognition-service/tests/test_models_status.py -q
```

Expected: PASS.

- [ ] **Step 5: Commit the image-recognition checkpoint.**

```powershell
git add models diagram-recognition-service/app/services/models diagram-recognition-service/app/services/image diagram-recognition-service/app/tools diagram-recognition-service/tests/test_image_pipeline.py diagram-recognition-service/tests/test_models_status.py samples/diagram-inputs/images
```

## Task 4: Integrate Diagram Analysis, LK Presentation, And Estimation Into The Java Backend

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/service/DesignAnalysisService.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/EstimationService.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/DesignAnalysisServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/EstimationServiceTest.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Modify: `metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java`
- Modify: `metrics-backend/src/main/java/com/metrics/controller/MetricsController.java`
- Modify: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`

- [ ] **Step 1: Write failing backend service tests for orchestration, LK presentation, and estimation.**

Cover these behaviors:

- `DesignAnalysisService` delegates structured and image inputs to the Python service client and surfaces recognition issues without dropping them
- `EstimationService` combines LoC, class count, relation count, use case count, and decision-node count into workload, cost, schedule, and staffing outputs
- existing code-analysis responses still work when no diagram-analysis payload is present
- code-metrics aggregation surfaces LK-related presentation fields such as relationship count, average methods per class, average attributes per class, relation density, and inheritance depth distribution

- [ ] **Step 2: Run the targeted backend service tests before implementation.**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q -Dtest=ClassMetricsAnalyzerTest,DesignAnalysisServiceTest,EstimationServiceTest,MetricsAnalysisServiceTest test
```

Expected: FAIL because the services and combined response logic do not exist yet.

- [ ] **Step 3: Implement the Java-side aggregation and heuristic estimation model.**

Estimation rules to encode in `EstimationService`:

- workload is a weighted function of `totalLoc`, `totalClasses`, `useCaseCount`, `decisionNodeCount`, and `relationshipCount`
- cost uses `workloadPersonMonths * costRatePerPersonMonth`
- schedule uses workload divided by effective staff capacity with a floor of one iteration window
- suggested staffing is the rounded-up workload divided by target schedule, with a minimum of one
- `EstimationBasis` must list every contributing metric and every user parameter so the result is explainable in the report
- extend the existing code-metrics analyzers so `ProjectSummary`, class-level data, and `AnalysisResponse` explicitly expose the LK-related presentation values promised in the approved spec

- [ ] **Step 4: Re-run the targeted tests and then the full backend suite.**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q -Dtest=ClassMetricsAnalyzerTest,DesignAnalysisServiceTest,EstimationServiceTest,MetricsAnalysisServiceTest test
mvn -f metrics-backend/pom.xml -q test
```

Expected: PASS.

- [ ] **Step 5: Commit the backend integration checkpoint.**

```powershell
git add metrics-backend/src/main/java/com/metrics/analyzer metrics-backend/src/main/java/com/metrics/service metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java metrics-backend/src/main/java/com/metrics/model/response/ClassMetrics.java metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java metrics-backend/src/main/java/com/metrics/model/response/RiskFinding.java metrics-backend/src/main/java/com/metrics/controller/MetricsController.java metrics-backend/src/test/java/com/metrics/analyzer/ClassMetricsAnalyzerTest.java metrics-backend/src/test/java/com/metrics/service
```

## Task 5: Expand The Vue Frontend For Diagram Analysis, LK Views, And Project Estimation

**Files:**
- Modify: `metrics-frontend/package.json`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/api/metrics.js`
- Modify: `metrics-frontend/src/composables/useAnalysis.js`
- Modify: `metrics-frontend/src/utils/exporters.js`
- Modify: `metrics-frontend/src/styles/theme.css`
- Create: `metrics-frontend/src/components/AnalysisTabs.vue`
- Create: `metrics-frontend/src/components/CodeMetricsWorkspace.vue`
- Create: `metrics-frontend/src/components/DiagramAnalysisWorkspace.vue`
- Create: `metrics-frontend/src/components/RecognitionPreview.vue`
- Create: `metrics-frontend/src/components/DiagramMetricsPanel.vue`
- Create: `metrics-frontend/src/components/LkMetricsPanel.vue`
- Create: `metrics-frontend/src/components/EstimationWorkspace.vue`
- Create: `metrics-frontend/src/components/EstimationSummary.vue`
- Create: `metrics-frontend/src/components/__tests__/DiagramWorkspace.test.js`
- Create: `metrics-frontend/src/components/__tests__/EstimationFlow.test.js`

- [ ] **Step 1: Write the failing frontend tests for the new workspaces.**

Cover these scenarios:

- users can switch between `Code Metrics`, `Design Diagrams`, and `Project Estimation`
- the code-metrics workspace renders a dedicated LK-related panel with class count, method count, attribute count, relation density, and inheritance depth distribution
- `DiagramAnalysisWorkspace` uploads a structured file and shows recognized metrics after a mocked API response
- `DiagramAnalysisWorkspace` uploads an image, displays preview labels, confidence, and issue warnings
- `EstimationWorkspace` submits user parameters and renders workload, cost, schedule, and staffing values
- exports include code metrics, diagram metrics, estimation basis, and risk findings in the Markdown output

- [ ] **Step 2: Run the new frontend tests before implementation.**

Run:

```powershell
npm --prefix metrics-frontend run test -- --run src/components/__tests__/DiagramWorkspace.test.js src/components/__tests__/EstimationFlow.test.js
```

Expected: FAIL because the new components and API calls do not exist yet.

- [ ] **Step 3: Implement the new frontend flow without regressing the current code-metrics page.**

Implementation notes:

- keep the current code-input workflow available under `CodeMetricsWorkspace`
- add an `LkMetricsPanel` to the code workspace instead of hiding LK-related values inside the general overview cards
- add a diagram workspace with explicit selectors for `diagramType` and `sourceType`
- render recognition output in three blocks: preview, metric cards, issue list
- add an estimation workspace that defaults to the most recent code and diagram metrics but allows user-supplied productivity and cost parameters
- extend `buildMarkdownReport` and `buildCsv` so the exported result includes diagram metrics, LK-related summary values, and estimation basis sections

- [ ] **Step 4: Re-run the new frontend tests, the existing frontend tests, and the production build.**

Run:

```powershell
npm --prefix metrics-frontend run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js src/components/__tests__/DiagramWorkspace.test.js src/components/__tests__/EstimationFlow.test.js
npm --prefix metrics-frontend run build
```

Expected: PASS.

- [ ] **Step 5: Commit the frontend enhancement checkpoint.**

```powershell
git add metrics-frontend/package.json metrics-frontend/src/App.vue metrics-frontend/src/api/metrics.js metrics-frontend/src/composables/useAnalysis.js metrics-frontend/src/utils/exporters.js metrics-frontend/src/styles/theme.css metrics-frontend/src/components
```

## Task 6: Add Samples, Report Artifacts, Scripts, And End-To-End Verification

**Files:**
- Create: `docs/requirements/functional-use-cases.md`
- Create: `docs/requirements/data-model-class-diagram.md`
- Create: `docs/requirements/non-functional-requirements.md`
- Create: `docs/architecture/system-architecture.md`
- Create: `docs/architecture/measurement-workflow.md`
- Create: `docs/estimation-method.md`
- Modify: `docs/metric-definitions.md`
- Modify: `docs/report-outline.md`
- Modify: `README.md`
- Modify: `scripts/dev.ps1`
- Modify: `scripts/build-demo.ps1`
- Modify: `scripts/run-demo.ps1`
- Modify: `scripts/smoke-demo.ps1`
- Create: `scripts/bootstrap-models.ps1`

- [ ] **Step 1: Add the report-support docs required by the course guidance.**

Document requirements exactly like this:

- `docs/requirements/functional-use-cases.md`: functional requirements described with a Mermaid use-case diagram plus bullet explanations
- `docs/requirements/data-model-class-diagram.md`: data requirements described with a Mermaid class diagram covering code metrics, diagram analysis, LK-related summary data, and estimation entities
- `docs/requirements/non-functional-requirements.md`: non-functional requirements for accuracy, explainability, offline reuse, performance, and local deployment
- `docs/architecture/system-architecture.md`: backend, Python service, and frontend architecture with one Mermaid component diagram
- `docs/architecture/measurement-workflow.md`: UML-style sequence or activity description of code analysis, structured parsing, image recognition, LK summarization, and estimation flow
- `docs/estimation-method.md`: the heuristic formulas, parameter definitions, and limitations section used in the course report

- [ ] **Step 2: Update the scripts so demo mode launches both services and keeps assets in D.**

Script rules:

- `scripts/bootstrap-models.ps1` creates the Python venv if needed, installs `diagram-recognition-service/requirements.txt`, and runs `app.tools.warm_models`
- `scripts/dev.ps1` starts Spring Boot, Vite, and the Python service in separate shells
- `scripts/build-demo.ps1` builds the Vue frontend, packages the Spring Boot jar, and validates that the Python service dependencies are installed
- `scripts/run-demo.ps1` starts the Python service first and then the Spring Boot jar
- `scripts/smoke-demo.ps1` checks `http://localhost:8080/api/metrics/health`, `http://localhost:8080/api/recognition/health`, and one structured-diagram analysis request through the Java backend

- [ ] **Step 3: Rebuild the sample assets and docs index.**

Update these files so users can actually try the new features:

- `README.md`
- `docs/metric-definitions.md`
- `docs/report-outline.md`
- `samples/ui-demo-inputs/README.md`

Add references to the new diagram fixtures, the Python-service bootstrap step, and the LK-related explanation view.

- [ ] **Step 4: Run the full verification stack in D.**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1
.\diagram-recognition-service\.venv\Scripts\python.exe -m pytest diagram-recognition-service/tests -q
mvn -f metrics-backend/pom.xml -q test
npm --prefix metrics-frontend run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js src/components/__tests__/DiagramWorkspace.test.js src/components/__tests__/EstimationFlow.test.js
npm --prefix metrics-frontend run build
powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1
```

Expected: PASS end to end.

- [ ] **Step 5: Commit the docs, scripts, and verification checkpoint.**

```powershell
git add docs README.md scripts samples/ui-demo-inputs/README.md
```

## Self-Review Checklist

- Spec coverage: structured diagrams, image recognition, LK-related presentation, estimation, frontend integration, and report artifacts each have at least one implementation task.
- TDD coverage: every major subsystem starts with failing tests before implementation.
- Vocabulary consistency: `diagramType`, `sourceType`, and issue-level enums are fixed once and reused across Java, Python, and Vue.
- Deployment safety: all scripts, caches, models, and runtime assets remain under `D:\Projects\SQA\Metrics_Tool`.
- Report support: the plan explicitly adds functional, data, non-functional, and architecture docs required by the course guidance.




