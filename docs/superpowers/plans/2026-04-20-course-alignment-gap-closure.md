# Course Alignment Gap Closure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the remaining course-defense gaps by making LK alignment visible in the frontend, preserving export evidence, and planning follow-up work for an additional independent estimation method.

**Architecture:** Keep the backend contracts stable for this phase and strengthen the course-facing layer in the frontend and documentation. Add a dedicated LK course-aligned presentation panel that reads from `codeMetrics.lkPresentation`, enrich the in-app metric guide, and keep the report/export path consistent with the same vocabulary used in the course alignment docs.

**Tech Stack:** Vue 3, Vitest, Testing Library, Markdown documentation

---

### Task 1: Formalize The Gap-Closure Workstream

**Files:**
- Create: `docs/superpowers/plans/2026-04-20-course-alignment-gap-closure.md`

- [ ] **Step 1: Capture the confirmed remaining gaps**

Write these gaps into the plan:

```markdown
- LK is implemented as a course-aligned presentation layer, but the frontend does not yet foreground it as a first-class result area.
- The course-defense path is stronger with one additional explicitly named metric method beyond UCP.
- The demo flow still depends too much on raw JSON and developer interpretation instead of a fixed visual narrative.
```

- [ ] **Step 2: Record the execution order**

Write this execution order into the plan:

```markdown
1. Deliver the LK course-aligned panel in the frontend.
2. Align the in-app metric guide with the documented LK vocabulary.
3. Keep export/report evidence consistent with the same LK wording.
4. Plan Function Point as the next independent estimation method.
```

- [ ] **Step 3: Mark Task 1 complete in the tracking checklist**

No command needed.

### Task 2: Add A Failing Frontend Test For LK Presentation

**Files:**
- Modify: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Extend the mocked analysis payload with LK presentation data**

Add this `codeMetrics` block to the mocked response:

```javascript
codeMetrics: {
  lkPresentation: {
    classCount: 1,
    methodCount: 1,
    attributeCount: 0,
    relationshipCount: 1,
    averageMethodsPerClass: 1,
    averageAttributesPerClass: 0,
    relationDensity: 0,
    inheritanceDepthDistribution: [0]
  }
},
```

- [ ] **Step 2: Assert the LK course-aligned UI appears after analysis**

Add assertions like:

```javascript
expect(await screen.findByText('LK Course-Aligned View')).toBeInTheDocument()
expect(await screen.findByText('Relationship Density')).toBeInTheDocument()
expect(await screen.findByText('1.00')).toBeInTheDocument()
```

- [ ] **Step 3: Run the focused dashboard test and verify it fails for the expected reason**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js
```

Expected: FAIL because the LK panel text is not rendered yet.

### Task 3: Implement The LK Course-Aligned Panel

**Files:**
- Create: `metrics-frontend/src/components/LkMetricsPanel.vue`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/styles/theme.css`

- [ ] **Step 1: Create the dedicated LK panel component**

Implement a focused component that accepts `lkPresentation` and renders:

```vue
<template>
  <section class="panel">
    <h2>LK Course-Aligned View</h2>
    <p class="lede">
      These values present the current code result using the LK-aligned vocabulary referenced by the course-defense notes.
    </p>
  </section>
</template>
```

The full component should show cards for class count, method count, attribute count, relationship count, average methods per class, average attributes per class, relationship density, and inheritance depth distribution.

- [ ] **Step 2: Wire the panel into the main code-metrics result flow**

Import and render the component in `App.vue` under the overview cards:

```vue
<LkMetricsPanel :lk-presentation="result.codeMetrics?.lkPresentation" />
```

Only render it when `result.codeMetrics?.lkPresentation` exists.

- [ ] **Step 3: Add lightweight styling for the new panel**

Add CSS classes for a compact metrics grid and a subdued evidence note. Reuse the current paper/ink/accent palette instead of introducing a new theme.

- [ ] **Step 4: Run the focused dashboard test and verify it passes**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js
```

Expected: PASS

### Task 4: Align The Metric Guide With The New LK Vocabulary

**Files:**
- Modify: `metrics-frontend/src/components/MetricInfoDrawer.vue`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`

- [ ] **Step 1: Expand the guide text to explain LK alignment**

Add concise bullets covering:

```markdown
- LK course-aligned view: class scale, method scale, attribute scale, relationship count, relation density, inheritance depth distribution.
- CK remains available as the class-level drill-down view.
```

- [ ] **Step 2: Add one visible guidance assertion to the dashboard test**

Add:

```javascript
expect(await screen.findByText(/LK course-aligned view/i)).toBeInTheDocument()
```

- [ ] **Step 3: Run the dashboard test again**

Run:

```powershell
npm run test -- --run src/components/__tests__/DashboardFlow.test.js
```

Expected: PASS

### Task 5: Verify Targeted Frontend Coverage

**Files:**
- Test: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Test: `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- Test: `metrics-frontend/src/utils/__tests__/exporters.test.js`

- [ ] **Step 1: Run the targeted frontend regression suite**

Run:

```powershell
npm run test -- --run src/components/__tests__/InputWorkspace.test.js src/components/__tests__/DashboardFlow.test.js src/utils/__tests__/exporters.test.js
```

Expected: PASS with 0 failing tests.

- [ ] **Step 2: Record the next planned follow-up**

Document this follow-up in the final handoff:

```markdown
Next recommended phase: add Function Point estimation as a second explicitly named independent method under the estimation mainline.
```
