# LK Contract Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a primary `lkMetrics` contract with `lkPresentation` compatibility, then align frontend/export consumers and course-facing documentation with the new contract.

**Architecture:** Keep LK metric formulas unchanged and centralize computation in `CodeMetricsResult`. Expose both `lkMetrics` and `lkPresentation` from the backend, make the frontend/export path prefer `lkMetrics` with fallback to `lkPresentation`, and update the course-alignment docs so they describe the implementation that actually ships.

**Tech Stack:** Java 17, Spring Boot, JUnit 5, Vue 3, Vitest, Markdown documentation

---

### Task 1: Add A Failing Backend Contract Test For `lkMetrics`

**Files:**
- Modify: `metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java`
- Modify: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`
- Test: `metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java`
- Test: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

- [ ] **Step 1: Extend `CodeMetricsResultTest` to assert `lkMetrics` and compatibility parity**

Change the test to include assertions like:

```java
assertThat(result.lkMetrics()).isNotNull();
assertThat(result.lkMetrics().relationshipCount()).isEqualTo(6);
assertThat(result.lkMetrics().relationDensity()).isEqualTo(1.0);
assertThat(result.lkPresentation()).isNotNull();
assertThat(result.lkPresentation().relationshipCount()).isEqualTo(result.lkMetrics().relationshipCount());
assertThat(result.lkPresentation().relationDensity()).isEqualTo(result.lkMetrics().relationDensity());
```

- [ ] **Step 2: Extend `MetricsAnalysisServiceTest` to assert the new primary field**

Add assertions like:

```java
assertThat(response.codeMetrics().lkMetrics()).isNotNull();
assertThat(response.codeMetrics().lkMetrics().classCount()).isEqualTo(1);
assertThat(response.codeMetrics().lkMetrics().methodCount()).isEqualTo(1);
assertThat(response.codeMetrics().lkMetrics().relationshipCount()).isEqualTo(0);
assertThat(response.codeMetrics().lkMetrics().relationDensity()).isEqualTo(0.0);
assertThat(response.codeMetrics().lkPresentation()).isNotNull();
assertThat(response.codeMetrics().lkPresentation().classCount()).isEqualTo(response.codeMetrics().lkMetrics().classCount());
```

- [ ] **Step 3: Run the targeted backend tests to verify RED**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q "-Dtest=CodeMetricsResultTest,MetricsAnalysisServiceTest" test
```

Expected: FAIL because `CodeMetricsResult` does not yet expose `lkMetrics`.

### Task 2: Implement The Backend `lkMetrics` Contract With Compatibility Alias

**Files:**
- Modify: `metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java`
- Test: `metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java`
- Test: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

- [ ] **Step 1: Add `lkMetrics` to the `CodeMetricsResult` record header**

Update the record signature to:

```java
public record CodeMetricsResult(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics,
    LkMetrics lkMetrics,
    LkPresentation lkPresentation,
    List<ParseIssue> parseIssues,
    boolean partial
) {
```

- [ ] **Step 2: Replace the old one-field construction with one shared LK computation path**

Inside `fromMetrics`, compute LK once and populate both fields:

```java
LkMetrics lkMetrics = buildLkMetrics(projectSummary, safeClassMetrics, safeMethodMetrics);
return new CodeMetricsResult(
    projectSummary,
    safeClassMetrics,
    safeMethodMetrics,
    lkMetrics,
    new LkPresentation(
        lkMetrics.classCount(),
        lkMetrics.methodCount(),
        lkMetrics.attributeCount(),
        lkMetrics.relationshipCount(),
        lkMetrics.averageMethodsPerClass(),
        lkMetrics.averageAttributesPerClass(),
        lkMetrics.relationDensity(),
        lkMetrics.inheritanceDepthDistribution()
    ),
    safeParseIssues,
    partial
);
```

- [ ] **Step 3: Introduce `buildLkMetrics(...)` and the new nested `LkMetrics` record**

Replace `buildLkPresentation(...)` with:

```java
private static LkMetrics buildLkMetrics(
    ProjectSummary projectSummary,
    List<ClassMetrics> classMetrics,
    List<MethodMetrics> methodMetrics
) {
    Integer classCount = projectSummary != null ? projectSummary.totalClasses() : classMetrics.size();
    Integer methodCount = projectSummary != null ? projectSummary.totalMethods() : methodMetrics.size();
    Integer attributeCount = classMetrics.stream().mapToInt(ClassMetrics::noa).sum();
    Integer relationshipCount = classMetrics.stream().mapToInt(ClassMetrics::cbo).sum();
    Double averageMethodsPerClass = classCount == null || classCount == 0 ? null : (double) methodCount / classCount;
    Double averageAttributesPerClass = classCount == null || classCount == 0 ? null : (double) attributeCount / classCount;
    Double relationDensity = null;
    if (classCount != null && classCount > 1) {
        double denominator = (double) classCount * (classCount - 1);
        relationDensity = round3(relationshipCount / denominator);
    } else if (classCount != null && classCount == 1) {
        relationDensity = 0.0;
    }
    List<Integer> inheritanceDepthDistribution = classMetrics.stream()
        .map(ClassMetrics::dit)
        .sorted()
        .toList();

    return new LkMetrics(
        classCount,
        methodCount,
        attributeCount,
        relationshipCount,
        averageMethodsPerClass,
        averageAttributesPerClass,
        relationDensity,
        inheritanceDepthDistribution
    );
}
```

and add:

```java
public record LkMetrics(
    Integer classCount,
    Integer methodCount,
    Integer attributeCount,
    Integer relationshipCount,
    Double averageMethodsPerClass,
    Double averageAttributesPerClass,
    Double relationDensity,
    List<Integer> inheritanceDepthDistribution
) {
    public LkMetrics {
        inheritanceDepthDistribution = inheritanceDepthDistribution == null
            ? List.of()
            : List.copyOf(inheritanceDepthDistribution);
    }
}
```

Keep the existing `LkPresentation` record unchanged except for using the new shared values.

- [ ] **Step 4: Run the targeted backend tests to verify GREEN**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q "-Dtest=CodeMetricsResultTest,MetricsAnalysisServiceTest" test
```

Expected: PASS

- [ ] **Step 5: Commit the backend contract change**

Run:

```powershell
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" add -- metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" commit -m "backend: add explicit lk metrics contract"
```

Expected: commit created with the backend contract and tests.

### Task 3: Add Failing Frontend And Exporter Tests For `lkMetrics`-First Behavior

**Files:**
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Modify: `metrics-frontend/src/utils/__tests__/exporters.test.js`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/utils/__tests__/exporters.test.js`

- [ ] **Step 1: Update the dashboard mock payload to use `lkMetrics` as the primary field**

Change the mocked analysis payload to:

```javascript
codeMetrics: {
  lkMetrics: {
    classCount: 2,
    methodCount: 2,
    attributeCount: 1,
    relationshipCount: 1,
    averageMethodsPerClass: 1,
    averageAttributesPerClass: 0.5,
    relationDensity: 0.5,
    inheritanceDepthDistribution: [0, 1]
  },
  lkPresentation: {
    classCount: 2,
    methodCount: 2,
    attributeCount: 1,
    relationshipCount: 1,
    averageMethodsPerClass: 1,
    averageAttributesPerClass: 0.5,
    relationDensity: 0.5,
    inheritanceDepthDistribution: [0, 1]
  }
}
```

- [ ] **Step 2: Add a fallback exporter test that only supplies `lkPresentation`**

Add a second exporter test like:

```javascript
test('falls back to lk presentation when lk metrics is absent', () => {
  const snapshot = {
    codeResult: {
      projectSummary: { totalFiles: 1, totalClasses: 1, totalMethods: 1, totalLoc: 12 },
      riskFindings: [],
      codeMetrics: {
        lkPresentation: {
          classCount: 1,
          methodCount: 1,
          attributeCount: 0,
          relationshipCount: 0,
          averageMethodsPerClass: 1,
          averageAttributesPerClass: 0,
          relationDensity: 0,
          inheritanceDepthDistribution: [0]
        }
      }
    }
  }

  expect(buildMarkdownReport(snapshot)).toContain('### LK Course-Aligned View')
  expect(buildCsv(snapshot)).toContain('codeMetrics.lk,classCount,1')
})
```

- [ ] **Step 3: Run the targeted frontend tests to verify RED**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js src/utils/__tests__/exporters.test.js
```

Expected: FAIL because the current frontend/exporters only read `lkPresentation`.

### Task 4: Implement `lkMetrics`-First Frontend And Exporter Consumption

**Files:**
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/utils/exporters.js`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/utils/__tests__/exporters.test.js`

- [ ] **Step 1: Update the LK panel binding in `App.vue`**

Replace the current render condition and prop binding with:

```vue
<LkMetricsPanel
  v-if="result.codeMetrics?.lkMetrics || result.codeMetrics?.lkPresentation"
  :lk-presentation="result.codeMetrics?.lkMetrics || result.codeMetrics?.lkPresentation"
/>
```

- [ ] **Step 2: Add one local LK payload fallback helper to `exporters.js`**

Insert a helper near the top of the file:

```javascript
function getLkMetrics(codeResult) {
  return codeResult?.codeMetrics?.lkMetrics || codeResult?.codeMetrics?.lkPresentation || null
}
```

- [ ] **Step 3: Replace direct `lkPresentation` reads with the helper**

Update both exporters to use:

```javascript
const lk = getLkMetrics(codeResult)
```

instead of:

```javascript
const lk = codeResult?.codeMetrics?.lkPresentation
```

Keep the CSV keys and Markdown heading unchanged.

- [ ] **Step 4: Run the targeted frontend tests to verify GREEN**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js src/utils/__tests__/exporters.test.js
```

Expected: PASS

- [ ] **Step 5: Commit the frontend/exporter change**

Run:

```powershell
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" add -- metrics-frontend/src/App.vue metrics-frontend/src/utils/exporters.js metrics-frontend/src/components/__tests__/DashboardFlow.test.js metrics-frontend/src/utils/__tests__/exporters.test.js
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" commit -m "frontend: prefer lk metrics contract in views and exports"
```

Expected: commit created with frontend/exporter updates and tests.

### Task 5: Sync Course-Facing Documentation With The New Contract

**Files:**
- Modify: `docs/lk-course-alignment.zh-CN.md`
- Modify: `docs/course-requirement-matrix.zh-CN.md`

- [ ] **Step 1: Update the LK alignment doc to name `lkMetrics` as the primary contract**

Change the contract wording to include lines like:

```markdown
- 当前主契约：`codeMetrics.lkMetrics`
- 兼容字段：`codeMetrics.lkPresentation`
```

and update the mapping table rows from:

```markdown
`lkPresentation.classCount`
```

to:

```markdown
`lkMetrics.classCount`
```

Also keep one note that `lkPresentation` remains for compatibility during transition.

- [ ] **Step 2: Update the course requirement matrix for `(1)` and `(2)`**

Revise row `(1)` to say:

```markdown
LK 主契约：`lkMetrics`；兼容字段：`lkPresentation`
```

Revise row `(2)` to say:

```markdown
已支持三类“其它方法”：`UCP`、`Function Point`、设计图度量（类图 / 流程图 / 用例图）
```

- [ ] **Step 3: Verify the doc sync by searching for the new wording**

Run:

```powershell
Select-String -Path 'docs/lk-course-alignment.zh-CN.md','docs/course-requirement-matrix.zh-CN.md' -Pattern 'lkMetrics','lkPresentation','Function Point' | ForEach-Object { "{0}:{1}" -f $_.LineNumber, $_.Line.Trim() }
```

Expected: output shows both docs now name `lkMetrics` as primary and `Function Point` as implemented.

- [ ] **Step 4: Commit the documentation sync**

Run:

```powershell
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" add -- docs/lk-course-alignment.zh-CN.md docs/course-requirement-matrix.zh-CN.md
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" commit -m "docs: align lk contract wording and requirement matrix"
```

Expected: commit created with the contract and matrix wording updates.

### Task 6: Run Final Regression Verification

**Files:**
- Verify: `metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java`
- Verify: `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`
- Verify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Verify: `metrics-frontend/src/utils/__tests__/exporters.test.js`
- Verify: `docs/lk-course-alignment.zh-CN.md`
- Verify: `docs/course-requirement-matrix.zh-CN.md`

- [ ] **Step 1: Run the targeted backend regression suite**

Run:

```powershell
mvn -f metrics-backend/pom.xml -q "-Dtest=MetricsAnalysisServiceTest,CodeMetricsResultTest,EstimationControllerTest,EstimationServiceBehaviorTest" test
```

Expected: PASS

- [ ] **Step 2: Run the targeted frontend regression suite**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js src/utils/__tests__/exporters.test.js
```

Expected: PASS

- [ ] **Step 3: Inspect the final diff summary**

Run:

```powershell
git -C "D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement" diff --stat -- metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java metrics-frontend/src/App.vue metrics-frontend/src/utils/exporters.js metrics-frontend/src/components/__tests__/DashboardFlow.test.js metrics-frontend/src/utils/__tests__/exporters.test.js docs/lk-course-alignment.zh-CN.md docs/course-requirement-matrix.zh-CN.md
```

Expected: diff summary shows backend contract, frontend/exporter, tests, and doc sync together.

- [ ] **Step 4: Record the completion note**

Use this note in the final handoff:

```markdown
LK contract alignment completed: `lkMetrics` is now the primary course-facing contract, `lkPresentation` remains as a compatibility alias, frontend/exporters prefer the new contract, and the course matrix now reflects `Function Point` as implemented.
```
