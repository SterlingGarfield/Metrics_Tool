# Desktop Bridge Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the first strict desktop-native bridge slice so the renderer can use Electron IPC and a Java CLI analyzer without reintroducing localhost or HTTP into the desktop execution path.

**Architecture:** Route desktop mode through a new preload/main bridge and a non-web Spring Boot CLI entrypoint. Any remaining web HTTP flow exists only as legacy browser/demo compatibility and is explicitly outside the Electron desktop contract. Reuse the existing Vue UI and `MetricsAnalysisService`, while moving renderer contracts onto a transport selector.

**Tech Stack:** Vue 3, Vitest, Electron, Spring Boot 3, Jackson, PowerShell, Node.js

---

### Task 1: Frontend Transport Selector

**Files:**
- Modify: `metrics-frontend/src/api/metrics.js`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/composables/useAnalysis.js`
- Test: `metrics-frontend/src/api/__tests__/metrics.test.js`

- [ ] **Step 1: Write failing transport-selection tests**
- [ ] **Step 2: Run `npm test -- --run src/api/__tests__/metrics.test.js` and confirm browser/desktop selection fails before implementation**
- [ ] **Step 3: Implement desktop-aware metrics client selection and export helpers**
- [ ] **Step 4: Re-run `npm test -- --run src/api/__tests__/metrics.test.js` until green**

### Task 2: Desktop-Aware Input Workspace

**Files:**
- Modify: `metrics-frontend/src/components/InputWorkspace.vue`
- Modify: `metrics-frontend/src/components/__tests__/InputWorkspace.test.js`
- Modify: `metrics-frontend/src/App.vue`
- Modify: `metrics-frontend/src/composables/useAnalysis.js`

- [ ] **Step 1: Write failing component coverage for desktop-native file and folder actions**
- [ ] **Step 2: Run `npm test -- --run src/components/__tests__/InputWorkspace.test.js` and confirm the new desktop assertions fail**
- [ ] **Step 3: Implement desktop-only native-action buttons while preserving browser file inputs**
- [ ] **Step 4: Re-run `npm test -- --run src/components/__tests__/InputWorkspace.test.js` and the dashboard flow test until green**

### Task 3: Java Desktop CLI Mode

**Files:**
- Modify: `metrics-backend/src/main/java/com/metrics/MetricsApplication.java`
- Create: `metrics-backend/src/main/java/com/metrics/desktop/DesktopCliExecutor.java`
- Create: `metrics-backend/src/main/java/com/metrics/desktop/DesktopCliRequest.java`
- Create: `metrics-backend/src/main/java/com/metrics/desktop/DesktopCliResponse.java`
- Test: `metrics-backend/src/test/java/com/metrics/desktop/DesktopCliExecutorTest.java`

- [ ] **Step 1: Write failing Spring Boot tests for desktop CLI text analysis and invalid command handling**
- [ ] **Step 2: Run `mvn -q test -Dtest=DesktopCliExecutorTest` and confirm the new test class fails before implementation**
- [ ] **Step 3: Implement non-web CLI execution that reuses `MetricsAnalysisService`**
- [ ] **Step 4: Re-run `mvn -q test -Dtest=DesktopCliExecutorTest` until green**

### Task 4: Electron Desktop Skeleton

**Files:**
- Create: `metrics-desktop/package.json`
- Create: `metrics-desktop/src/main/index.cjs`
- Create: `metrics-desktop/src/main/analyzer.cjs`
- Create: `metrics-desktop/src/main/fs-sources.cjs`
- Create: `metrics-desktop/src/main/ipc.cjs`
- Create: `metrics-desktop/src/preload/index.cjs`
- Create: `metrics-desktop/src/splash/index.html`

- [ ] **Step 1: Add the Electron shell, preload bridge, IPC handlers, and Java CLI adapter**
- [ ] **Step 2: Run `node --check` against the new desktop entry files**
- [ ] **Step 3: Confirm the bridge contract names match the frontend transport selector exactly**
- [ ] **Step 4: Document any remaining packaging gaps in the final handoff**
