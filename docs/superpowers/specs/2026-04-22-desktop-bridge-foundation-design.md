# Metrics Tool Desktop Bridge Foundation Design

## Context

The current repository still runs as a browser-first system:

- `metrics-frontend` calls `/api/metrics` directly through Axios
- `metrics-backend` exposes analysis only through Spring MVC controllers
- file and folder analysis depend on browser upload semantics
- export is handled entirely inside the renderer with browser blob downloads

That flow works for web demos, but it conflicts with the now-locked desktop direction:

- renderer should no longer depend on `localhost` as its main contract
- desktop orchestration should move into Electron main and preload
- Java analysis logic should remain reusable while moving away from HTTP as the primary desktop transport
- native file and folder dialogs should replace browser-only upload patterns in desktop mode

## Goal

Create the first strict de-HTTP desktop foundation slice that is small enough to land safely now and strong enough to support the remaining desktop work.

This slice must:

1. add a new `metrics-desktop/` Electron shell
2. expose a stable renderer contract through preload IPC
3. stop the Vue renderer from depending directly on Axios in desktop mode
4. add a non-HTTP Java analyzer entrypoint for desktop calls
5. keep any remaining Spring controllers outside the desktop contract, only as legacy web/demo compatibility

## Non-Goals

This slice will not complete:

- the final Windows installer
- bundled JRE delivery
- dual-directory installer UX
- full splash-state polish
- long-running analyzer supervision
- recognition-service desktop packaging

Those remain later milestones after the bridge contract is in place.

## Recommended Approach

Adopt one strict desktop path plus one legacy browser compatibility path:

- desktop mode uses `renderer -> preload -> Electron main -> Java CLI`
- browser mode may temporarily keep the existing Axios-to-Spring flow, but that flow is not part of the desktop architecture and must not leak back into Electron main

The important architectural change is that the renderer no longer knows which transport is used. It calls one shared metrics client. Desktop mode is the primary contract; browser mode is a separate compatibility surface only.

## Desktop Units

### 1. Frontend Metrics Client

Keep `metrics-frontend/src/api/metrics.js` as the renderer-facing module, but turn it into a transport selector.

Responsibilities:

- detect whether `window.metricsDesktop` is available
- route health, analysis, selection, and export calls to the active transport
- keep browser mode behavior intact for existing tests and demo flows

### 2. Electron Preload Bridge

Expose a narrow API into the renderer:

- `getAppStatus`
- `analyzeText`
- `analyzeFiles`
- `analyzeFolder`
- `selectFiles`
- `selectFolder`
- `exportCsv`
- `exportMarkdown`

The preload layer owns security boundaries; the renderer receives only the methods it needs.

### 3. Electron Main Analyzer Adapter

Electron main should:

- resolve the backend jar path
- read selected file/folder contents from disk
- spawn the Java analyzer jar on demand
- pass JSON through stdin/stdout
- return structured JSON results back through IPC

This avoids reintroducing renderer-to-localhost coupling.

### 4. Java Desktop CLI

`metrics-backend` should gain a desktop CLI mode inside the existing jar.

Responsibilities:

- boot Spring in non-web mode when `--metrics.desktop.mode=cli` is present
- read one JSON request from stdin
- execute `getAppStatus`, `analyzeText`, or `analyzeSources`
- write one JSON response to stdout
- exit cleanly with a success or failure code

The existing `MetricsAnalysisService` remains the core analysis engine for both web and desktop paths.

## Frontend Interaction Changes

Desktop mode should replace browser-only inputs where the browser contract leaks through the UI:

- single-file analysis becomes a native file dialog action
- multi-file analysis becomes a native multi-select file dialog action
- folder analysis becomes a native directory picker
- export becomes a native save dialog action

Text analysis can stay in the renderer because it is already desktop-neutral.

## Error Handling

This slice should fail clearly rather than pretending the analyzer exists.

Rules:

- missing jar path should surface a readable error to the renderer
- unsupported CLI commands should return structured JSON errors
- empty file selections should no-op rather than showing fake success
- browser mode should remain unaffected if the desktop bridge is absent

## Verification Targets

The slice is acceptable when all of the following are true:

1. frontend unit tests prove the metrics client chooses desktop or browser transport correctly
2. frontend component tests prove desktop mode swaps browser file inputs for native-action buttons
3. backend tests prove the desktop CLI can analyze text and reject invalid commands
4. Electron main and preload files parse successfully under Node syntax checks
5. existing frontend text-analysis flow still passes in browser-mode tests

## Follow-On Work

After this slice lands, the next milestones become much cleaner:

- long-running analyzer lifecycle and splash readiness
- packaging the jar and JRE under `metrics-desktop`
- replacing remaining browserish labels and flows with desktop-first copy
- installer work, including dual-directory selection
