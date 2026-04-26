# LK Contract Alignment Design

## Context

The project already exposes LK-related course-facing metrics through:

- `metrics-backend/src/main/java/com/metrics/model/response/CodeMetricsResult.java`
- `metrics-frontend/src/components/LkMetricsPanel.vue`
- `metrics-frontend/src/utils/exporters.js`
- `docs/lk-course-alignment.zh-CN.md`

At the moment, the backend contract uses the field name:

- `codeMetrics.lkPresentation`

This works for the current frontend and export path, but it leaves one course-facing weakness:

- the contract name still describes a presentation layer rather than an explicit LK metrics object

That makes the project harder to defend when the course requirement is interpreted strictly as “the system should implement LK metrics,” not just present an LK-aligned view.

At the same time, the current branch already has working frontend rendering, exporter coverage, and backend tests that rely on `lkPresentation`. A hard cut would create unnecessary regression risk.

## Goal

Add an explicit LK metrics contract named `lkMetrics` while preserving compatibility with the current `lkPresentation` field, then align the frontend, export path, and course-alignment documentation to treat `lkMetrics` as the primary course-facing contract.

At the end of this phase:

- backend responses should expose `codeMetrics.lkMetrics`
- `codeMetrics.lkPresentation` should remain available as a compatibility alias
- frontend and exporter code should prefer `lkMetrics` and only fall back to `lkPresentation`
- course-facing docs should describe `lkMetrics` as the primary contract
- the requirement matrix should explicitly state that additional methods now include `Function Point`

## Non-Goals

- changing the actual LK metric formulas
- adding new CK, LoC, or complexity metrics
- redesigning the frontend LK panel
- changing estimation formulas or API behavior outside documentation wording
- removing `lkPresentation` in this phase

## Problem Statement

The current implementation has two separate issues.

### 1. LK is explicit in behavior but not explicit in contract naming

`CodeMetricsResult` already computes a stable group of course-aligned LK fields:

- class count
- method count
- attribute count
- relationship count
- average methods per class
- average attributes per class
- relation density
- inheritance depth distribution

But the current name `lkPresentation` weakens the contract story during review and defense.

### 2. The course-requirement matrix is now behind the codebase

The project already supports:

- `ucp`
- `function_point`
- diagram-metric-based structured and image analysis

However, the current matrix still describes requirement `(2)` in older wording that centers on UCP + diagram metrics and treats Function Point as future work.

## Success Criteria

This phase is complete only when all of the following are true:

### Backend contract

- `CodeMetricsResult` includes a primary `lkMetrics` field
- `lkMetrics` and `lkPresentation` carry the same values in the same response
- the LK calculation logic is implemented once and reused for both fields

### Frontend and export behavior

- the LK panel renders from `codeMetrics.lkMetrics` when present
- the LK panel still works with older payloads that only contain `lkPresentation`
- CSV and Markdown export use the same primary/fallback rule

### Documentation alignment

- `docs/lk-course-alignment.zh-CN.md` names `lkMetrics` as the primary contract
- `docs/course-requirement-matrix.zh-CN.md` names `lkPresentation` as a compatibility field, not the main LK contract
- the matrix requirement `(2)` explicitly lists `UCP + Function Point + design-diagram metrics`

### Verification

- backend tests cover `lkMetrics` creation and compatibility alias behavior
- frontend/export tests cover preference for `lkMetrics` with fallback to `lkPresentation`

## Recommended Design

### 1. Introduce a dedicated `LkMetrics` record in `CodeMetricsResult`

`CodeMetricsResult` should define a dedicated nested record:

- `LkMetrics`

with the existing LK field set:

- `classCount`
- `methodCount`
- `attributeCount`
- `relationshipCount`
- `averageMethodsPerClass`
- `averageAttributesPerClass`
- `relationDensity`
- `inheritanceDepthDistribution`

The response object should then expose:

- `lkMetrics`
- `lkPresentation`

where:

- `lkMetrics` is the primary contract
- `lkPresentation` is a compatibility alias with the same shape and values

This avoids changing the formulas while making the contract story course-facing and explicit.

### 2. Keep one computation path

The backend should compute LK values once and then populate both fields from the same source object.

This avoids:

- drift between the fields
- duplicated logic
- accidental differences in rounding or null handling

The preferred implementation is:

- compute `LkMetrics lkMetrics = buildLkMetrics(...)`
- assign that object to `lkMetrics`
- assign `lkPresentation` from the same values, either by shared construction logic or a small adapter

### 3. Make the frontend and exporters `lkMetrics`-first

The frontend should treat:

- `codeMetrics.lkMetrics`

as the preferred contract and only fall back to:

- `codeMetrics.lkPresentation`

This applies to:

- `metrics-frontend/src/App.vue`
- `metrics-frontend/src/components/LkMetricsPanel.vue`
- `metrics-frontend/src/utils/exporters.js`

The panel title can remain:

- `LK Course-Aligned View`

because the UI is still a course-facing presentation of LK metrics. The contract name change is about API clarity, not UI branding.

### 4. Update course-facing documentation to match the new truth

`docs/lk-course-alignment.zh-CN.md` should be updated to say:

- the primary backend contract is `codeMetrics.lkMetrics`
- `codeMetrics.lkPresentation` remains only for compatibility during transition

`docs/course-requirement-matrix.zh-CN.md` should be updated to reflect two facts:

- requirement `(1)` is now supported by an explicit `lkMetrics` contract plus CK/traditional metrics
- requirement `(2)` already includes `Function Point`, not just UCP and diagram metrics

## Data Flow

After this change, the code metrics path should read like this:

1. Java source is parsed into AST through `JavaSourceParser`
2. class, method, and project metrics are aggregated
3. LK values are computed once in `CodeMetricsResult`
4. the response exposes both:
   - `lkMetrics`
   - `lkPresentation`
5. frontend and exporters prefer `lkMetrics`
6. older consumers still work through `lkPresentation`

## Error Handling And Compatibility Rules

### Backend compatibility rule

Do not remove `lkPresentation` in this phase.

That field must remain populated so that:

- current frontend builds remain safe during transition
- exported report logic remains backward compatible
- any stored sample payloads or tests using `lkPresentation` do not break abruptly

### Frontend compatibility rule

The frontend should use a fallback lookup:

- first `result.codeMetrics?.lkMetrics`
- then `result.codeMetrics?.lkPresentation`

If neither exists, the LK panel should remain hidden as it does today.

## Testing Strategy

This phase should follow TDD for both backend and frontend touchpoints.

### Backend tests

Update and extend:

- `metrics-backend/src/test/java/com/metrics/model/response/CodeMetricsResultTest.java`
- `metrics-backend/src/test/java/com/metrics/service/MetricsAnalysisServiceTest.java`

The tests should verify:

- `lkMetrics` is present
- `lkMetrics` contains the expected values
- `lkPresentation` still exists and matches

### Frontend and exporter tests

Update and extend:

- `metrics-frontend/src/components/__tests__/DashboardFlow.test.js`
- `metrics-frontend/src/utils/__tests__/exporters.test.js`

The tests should verify:

- the LK panel still renders when the payload provides `lkMetrics`
- exporter output uses LK values from `lkMetrics`
- fallback still works when only `lkPresentation` is available

## Risks And Mitigations

### Risk: duplicated field shapes drift later

Mitigation:

- centralize LK computation
- keep one field as the primary source during runtime
- document `lkPresentation` as compatibility-only

### Risk: documentation and code drift again

Mitigation:

- update the requirement matrix and LK alignment doc in the same phase
- treat document sync as part of acceptance, not optional follow-up

### Risk: frontend tests accidentally keep using only the legacy field

Mitigation:

- make at least one targeted frontend test assert `lkMetrics`
- keep one fallback-oriented exporter or dashboard assertion for `lkPresentation`

## Acceptance Rule

Do not call this phase complete until:

1. `CodeMetricsResult` exposes `lkMetrics`
2. `lkPresentation` still works as a compatibility alias
3. frontend/export logic prefers `lkMetrics`
4. the course matrix says `(2)` includes `Function Point`
5. targeted backend and frontend tests pass

## Design Summary

This phase upgrades the LK contract from “course-aligned presentation naming” to an explicit `lkMetrics` API contract without destabilizing the current branch. It also synchronizes the course matrix so the documentation finally matches the implemented feature set.
