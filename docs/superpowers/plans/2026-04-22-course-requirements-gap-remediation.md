# Metrics Tool Course Requirements Gap Remediation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring the current Metrics Tool from a Java-code-centric OO metrics analyzer to a course-aligned software metrics tool that covers code metrics, design-stage metrics, and project estimation metrics required by the assignment.

**Architecture:** Keep the current `Spring Boot + JDT ASTParser + Vue/Electron` foundation, but split the analysis contract into three domains: `codeMetrics`, `designMetrics`, and `estimationMetrics`. Add diagram/design-stage input as a new ingestion path, keep Java AST analysis as the strongest implemented path, and expose project-effort estimators through explicit user-input forms instead of hiding them inside code metrics.

**Tech Stack:** Spring Boot 3, Eclipse JDT ASTParser, Vue 3, Electron desktop bridge, Vitest, JUnit 5

---

## File Structure

**Backend code metrics (existing, expand in place)**
- Modify: `metrics-backend/src/main/java/com/metrics/service/MetricsAnalysisService.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/ProjectSummary.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`

**Backend design metrics (new)**
- Create: `metrics-backend/src/main/java/com/metrics/model/request/DesignAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/request/UseCaseEstimateRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DesignMetricsSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/EstimationSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/DesignMetricsService.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/EstimationService.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/DesignMetricsController.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/EstimationController.java`

**Frontend inputs and reporting**
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/api/metrics.js`
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Create: `metrics-frontend/src/components/DesignInputPanel.vue`
- Create: `metrics-frontend/src/components/EstimationInputPanel.vue`
- Create: `metrics-frontend/src/components/DesignMetricsPanel.vue`
- Create: `metrics-frontend/src/components/EstimationPanel.vue`
- Modify: `metrics-frontend/src/composables/useAnalysis.js`

**Desktop bridge**
- Modify: `metrics-desktop/src/main/ipc.cjs`
- Modify: `metrics-desktop/src/preload/index.cjs`
- Modify: `metrics-desktop/src/main/fs-sources.cjs`

**Tests**
- Modify: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/DesignMetricsServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/service/EstimationServiceTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/DesignMetricsControllerTest.java`
- Create: `metrics-backend/src/test/java/com/metrics/controller/EstimationControllerTest.java`
- Modify: `metrics-frontend/src/api/__tests__/metrics.test.js`
- Create: `metrics-frontend/src/components/__tests__/DesignInputPanel.test.js`
- Create: `metrics-frontend/src/components/__tests__/EstimationInputPanel.test.js`

---

### Task 1: Stabilize a Course-Aligned Analysis Contract

**Files:**
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/DesignMetricsSummary.java`
- Create: `metrics-backend/src/main/java/com/metrics/model/response/EstimationSummary.java`
- Modify: `metrics-frontend/src/api/metrics.js`
- Test: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

- [ ] **Step 1: Write the failing backend test**

```java
@Test
void analysisResponseExposesCodeDesignAndEstimationSections() {
    AnalysisResponse response = service.analyzeText(
        new TextAnalyzeRequest("Demo.java", "public class Demo { void run() {} }")
    );

    assertThat(response.codeMetrics()).isNotNull();
    assertThat(response.designMetrics()).isNotNull();
    assertThat(response.estimationMetrics()).isNotNull();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=MetricsAnalysisServiceTest"`
Expected: FAIL because `AnalysisResponse` does not yet define the three top-level sections.

- [ ] **Step 3: Write minimal implementation**

```java
public record AnalysisResponse(
    CodeMetricsSummary codeMetrics,
    DesignMetricsSummary designMetrics,
    EstimationSummary estimationMetrics,
    List<RiskFinding> riskFindings,
    List<ParseIssue> parseIssues,
    boolean partial
) {}
```

- [ ] **Step 4: Run the backend test again**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=MetricsAnalysisServiceTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-backend/src/main/java/com/metrics/model/response/AnalysisResponse.java metrics-backend/src/main/java/com/metrics/model/response/DesignMetricsSummary.java metrics-backend/src/main/java/com/metrics/model/response/EstimationSummary.java metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java
git commit -m "feat: split analysis response into code design and estimation domains"
```

### Task 2: Complete the Code-Metrics Requirement Around CK + Traditional Metrics

**Files:**
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ClassMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/MethodMetricsAnalyzer.java`
- Modify: `metrics-backend/src/main/java/com/metrics/analyzer/ProjectMetricsAnalyzer.java`
- Modify: `metrics-frontend/src/components/MetricsTables.vue`
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Test: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

- [ ] **Step 1: Write the failing CK coverage test**

```java
@Test
void returnsCoreCkMetricsAndTraditionalCodeMetrics() {
    AnalysisResponse response = service.analyzeText(
        new TextAnalyzeRequest("Account.java", "class Account { int x; void a(){ if(x>0){} } }")
    );

    assertThat(response.codeMetrics().classMetrics()).first().satisfies(metric -> {
        assertThat(metric.wmc()).isGreaterThanOrEqualTo(1);
        assertThat(metric.cbo()).isGreaterThanOrEqualTo(0);
        assertThat(metric.rfc()).isGreaterThanOrEqualTo(1);
        assertThat(metric.lcom()).isGreaterThanOrEqualTo(0);
        assertThat(metric.dit()).isGreaterThanOrEqualTo(0);
        assertThat(metric.noc()).isGreaterThanOrEqualTo(0);
        assertThat(metric.loc()).isGreaterThanOrEqualTo(1);
    });
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=MetricsAnalysisServiceTest"`
Expected: FAIL until the contract is moved under `codeMetrics` and the frontend/reporting is updated.

- [ ] **Step 3: Write minimal implementation**

```java
public record CodeMetricsSummary(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics
) {}
```

Also keep the current implemented metrics as the baseline course-compliance set:
- CK core: `WMC`, `CBO`, `RFC`, `LCOM`, `DIT`, `NOC`
- traditional metrics: `LOC`, `cyclomaticComplexity`, `maxNestingDepth`, `branchCount`

- [ ] **Step 4: Run backend and frontend checks**

Run:
- `mvn -f metrics-backend/pom.xml -q test`
- `npm run test -- --run src/components/__tests__/DashboardFlow.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-backend/src/main/java/com/metrics/analyzer metrics-frontend/src/components/MetricsTables.vue metrics-frontend/src/components/MetricInfoDrawer.vue
git commit -m "feat: formalize ck and traditional code metrics coverage"
```

### Task 3: Add Project-Estimation Metrics for Workload, Cost, Time, and Personnel

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/model/request/ManualEstimateRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/EstimationService.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/EstimationController.java`
- Create: `metrics-frontend/src/components/EstimationInputPanel.vue`
- Create: `metrics-frontend/src/components/EstimationPanel.vue`
- Modify: `metrics-frontend/src/App.vue`
- Test: `metrics-backend/src/test/java/com/metrics/service/EstimationServiceTest.java`

- [ ] **Step 1: Write the failing estimation test**

```java
@Test
void computesEffortCostScheduleAndStaffingFromManualInputs() {
    EstimationSummary summary = service.summarize(
        new ManualEstimateRequest(1200, 6, 2, 12000.0)
    );

    assertThat(summary.loc()).isEqualTo(1200);
    assertThat(summary.staffCount()).isEqualTo(6);
    assertThat(summary.devMonths()).isEqualTo(2);
    assertThat(summary.cost()).isEqualTo(12000.0);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=EstimationServiceTest"`
Expected: FAIL because there is no estimation service yet.

- [ ] **Step 3: Write minimal implementation**

```java
public record ManualEstimateRequest(
    int loc,
    int staffCount,
    int devMonths,
    double cost
) {}
```

Expose a dedicated estimation input card in the frontend instead of hiding these values inside code analysis.

- [ ] **Step 4: Run backend and UI tests**

Run:
- `mvn -f metrics-backend/pom.xml -q test "-Dtest=EstimationServiceTest,EstimationControllerTest"`
- `npm run test -- --run src/components/__tests__/EstimationInputPanel.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-backend/src/main/java/com/metrics/model/request/ManualEstimateRequest.java metrics-backend/src/main/java/com/metrics/service/EstimationService.java metrics-backend/src/main/java/com/metrics/controller/EstimationController.java metrics-frontend/src/components/EstimationInputPanel.vue metrics-frontend/src/components/EstimationPanel.vue
git commit -m "feat: add estimation metrics for effort cost schedule and staffing"
```

### Task 4: Add Design-Stage Input for Class Diagram, Flowchart, and Use Case Data

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/model/request/DesignAnalyzeRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/service/DesignMetricsService.java`
- Create: `metrics-backend/src/main/java/com/metrics/controller/DesignMetricsController.java`
- Create: `metrics-frontend/src/components/DesignInputPanel.vue`
- Create: `metrics-frontend/src/components/DesignMetricsPanel.vue`
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-desktop/src/main/ipc.cjs`
- Test: `metrics-backend/src/test/java/com/metrics/service/DesignMetricsServiceTest.java`

- [ ] **Step 1: Write the failing design-metrics test**

```java
@Test
void summarizesClassUseCaseAndFlowInputs() {
    DesignMetricsSummary summary = service.analyze(
        new DesignAnalyzeRequest(8, 12, 3, 6, 4)
    );

    assertThat(summary.classCount()).isEqualTo(8);
    assertThat(summary.relationshipCount()).isEqualTo(12);
    assertThat(summary.useCaseCount()).isEqualTo(3);
    assertThat(summary.actorCount()).isEqualTo(6);
    assertThat(summary.flowNodeCount()).isEqualTo(4);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=DesignMetricsServiceTest"`
Expected: FAIL because design-stage analysis is not implemented.

- [ ] **Step 3: Write minimal implementation**

```java
public record DesignAnalyzeRequest(
    int classCount,
    int relationshipCount,
    int useCaseCount,
    int actorCount,
    int flowNodeCount
) {}
```

Start with explicit user-entered design counts plus optional diagram-file upload. Treat OCR/image recognition as an enhancement path, not as the only path to compliance.

- [ ] **Step 4: Run tests**

Run:
- `mvn -f metrics-backend/pom.xml -q test "-Dtest=DesignMetricsServiceTest,DesignMetricsControllerTest"`
- `npm run test -- --run src/components/__tests__/DesignInputPanel.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-backend/src/main/java/com/metrics/model/request/DesignAnalyzeRequest.java metrics-backend/src/main/java/com/metrics/service/DesignMetricsService.java metrics-backend/src/main/java/com/metrics/controller/DesignMetricsController.java metrics-frontend/src/components/DesignInputPanel.vue metrics-frontend/src/components/DesignMetricsPanel.vue metrics-frontend/src/components/InputWorkspace.vue metrics-desktop/src/main/ipc.cjs
git commit -m "feat: add design-stage inputs for class use case and flow metrics"
```

### Task 5: Add One Additional Course Metric Family Beyond OO Metrics

**Files:**
- Create: `metrics-backend/src/main/java/com/metrics/model/request/UseCaseEstimateRequest.java`
- Modify: `metrics-backend/src/main/java/com/metrics/service/EstimationService.java`
- Modify: `metrics-frontend/src/components/EstimationInputPanel.vue`
- Modify: `metrics-frontend/src/components/EstimationPanel.vue`
- Test: `metrics-backend/src/test/java/com/metrics/service/EstimationServiceTest.java`

- [ ] **Step 1: Write the failing use-case-point test**

```java
@Test
void computesUseCasePointStyleSizeFromActorsAndUseCases() {
    var summary = service.summarizeUseCasePoints(
        new UseCaseEstimateRequest(2, 3, 4, 5)
    );

    assertThat(summary.useCasePoints()).isGreaterThan(0);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f metrics-backend/pom.xml -q test "-Dtest=EstimationServiceTest"`
Expected: FAIL because UCP support does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
public record UseCaseEstimateRequest(
    int simpleActors,
    int averageActors,
    int simpleUseCases,
    int averageUseCases
) {}
```

Choose Use Case Points as the first non-OO extension because it aligns with the assignment's explicit `用例图` input requirement and is lower risk than full Function Point automation.

- [ ] **Step 4: Run tests**

Run:
- `mvn -f metrics-backend/pom.xml -q test "-Dtest=EstimationServiceTest"`
- `npm run test -- --run src/api/__tests__/metrics.test.js`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add metrics-backend/src/main/java/com/metrics/model/request/UseCaseEstimateRequest.java metrics-backend/src/main/java/com/metrics/service/EstimationService.java metrics-frontend/src/components/EstimationInputPanel.vue metrics-frontend/src/components/EstimationPanel.vue
git commit -m "feat: add use case point estimation workflow"
```

### Task 6: Demo Hardening, Report Coverage, and Delivery Cleanup

**Files:**
- Modify: `README.md`
- Modify: `scripts/build-demo.ps1`
- Modify: `scripts/run-demo.ps1`
- Modify: `scripts/smoke-demo.ps1`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/components/OverviewCards.vue`

- [ ] **Step 1: Write the failing smoke assertion**

```powershell
if (-not ($response.Content -match 'designMetrics' -and $response.Content -match 'estimationMetrics')) {
  throw 'Demo payload is missing design/estimation coverage.'
}
```

- [ ] **Step 2: Run smoke script to verify it fails**

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1`
Expected: FAIL until the expanded payload is present.

- [ ] **Step 3: Update the demo docs and report UI**

```markdown
- design-stage metrics input
- project estimation metrics
- use-case-point estimation
```

- [ ] **Step 4: Run full verification**

Run:
- `mvn -f metrics-backend/pom.xml -q test`
- `npm run test -- --run`
- `npm run build`
- `powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1`
- `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-demo.ps1`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add README.md scripts/build-demo.ps1 scripts/run-demo.ps1 scripts/smoke-demo.ps1 metrics-frontend/src/App.vue metrics-frontend/src/components/OverviewCards.vue
git commit -m "docs: align demo and reports with course requirement coverage"
```

---

## Self-Review

**Spec coverage:** This plan covers the current gaps against the assignment: missing LK/extended OO presentation, missing non-code entities (`工作量/成本/时间/人员`), missing design-stage inputs (`类图/流程图/用例图`), and missing secondary metric-family coverage.

**Placeholder scan:** No `TBD` or `TODO` placeholders remain. The only intentionally staged decision is to start design-stage support with explicit structured input plus optional diagram upload, which is a deliberate scope choice rather than an unresolved placeholder.

**Type consistency:** The plan consistently uses `AnalysisResponse -> codeMetrics/designMetrics/estimationMetrics`, and the added request models are named `DesignAnalyzeRequest`, `ManualEstimateRequest`, and `UseCaseEstimateRequest`.

