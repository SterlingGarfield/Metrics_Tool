# Python Recognition Runtime Hardening Design

## Context

The diagram recognition service now has a working structured parser path and a working image-recognition path inside:

- `diagram-recognition-service/app/services/structured`
- `diagram-recognition-service/app/services/image`
- `diagram-recognition-service/app/tools/warm_models.py`

The image pipeline already passes the targeted tests for:

- `GET /recognition/models/status`
- `POST /recognition/analyze/image`
- low-confidence warning behavior

However, the current OCR runtime is not yet fully self-contained inside:

- `D:\Projects\SQA\Metrics_Tool\.worktrees\diagram-recognition-enhancement\diagram-recognition-service\.venv311`

The service currently carries a fallback path that may load Paddle-related runtime assets from:

- `D:\Projects\SQA\Metrics_Tool\.ocr311`

This means the feature works, but the environment is not yet cleanly reproducible from the worktree alone. That is a delivery risk for subsequent phases such as Java integration, demo scripts, smoke tests, and course defense preparation.

## Goal

Harden the Python recognition runtime so that `.venv311` becomes the only supported execution environment for the recognition service, including OCR startup, model warm-up, full pytest verification, and local FastAPI startup.

At the end of this phase:

- `ocr.py` must load PaddleOCR only from `.venv311`
- `.ocr311` must no longer be used as a fallback runtime source
- the project must have one reproducible bootstrap path for Python recognition setup
- runtime diagnostics must clearly show which interpreter, site-packages path, and dependency versions are in use

## Non-Goals

- redesigning the image-recognition algorithm
- changing diagram metric vocabulary
- Java backend integration
- frontend work
- report/documentation work beyond this focused runtime design
- improving recognition accuracy except where startup correctness depends on it

## Current Problem Statement

The current runtime state has three weaknesses:

### 1. Runtime ambiguity

`ocr.py` may search more than one runtime location for Paddle-related packages. This makes it possible for the service to pass tests while still depending on an external environment.

### 2. Fragile Windows setup

The worktree-local `.venv311` has already hit Windows-specific issues such as:

- long install paths during Paddle installation
- missing runtime DLLs
- incompatible `protobuf` versions
- partially copied site-packages content

### 3. Weak observability

`warm_models.py` reports model-cache readiness, but it is not yet the authoritative place to confirm the exact Python runtime and package versions used by OCR startup.

## Success Criteria

This phase is complete only when all of the following are true:

### Runtime isolation

- `diagram-recognition-service/.venv311` is the only supported runtime for the recognition service
- `ocr.py` does not reference `.ocr311`
- OCR startup succeeds without loading Paddle runtime assets from outside `.venv311`

### Reproducibility

- a documented bootstrap flow can create or repair `.venv311`
- the bootstrap flow can install the pinned dependency set in a repeatable way on the target Windows machine
- PaddleOCR model assets remain cached under `D:\Projects\SQA\Metrics_Tool\models`

### Verification

- `.\.venv311\python.exe -m app.tools.warm_models` completes with no runtime issues
- `.\.venv311\python.exe -m pytest tests -q` passes
- `.\.venv311\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8090` starts successfully

### Diagnostics

- the runtime warm-up output shows enough information to diagnose version drift and runtime path mistakes quickly

## Design Boundaries

This sub-project is intentionally narrow.

It may change:

- `diagram-recognition-service/requirements.txt`
- `diagram-recognition-service/app/services/image/ocr.py`
- `diagram-recognition-service/app/services/models/*`
- `diagram-recognition-service/app/tools/warm_models.py`
- Python bootstrap or helper scripts under `scripts/`
- targeted runtime-oriented tests

It should not change:

- structured parsing behavior
- image metric formulas
- Java contracts
- frontend contracts

## Recommended Design

### 1. Make `.venv311` the only runtime authority

The recognition service should treat the current interpreter as the sole runtime authority for importing:

- `paddle`
- `paddleocr`
- `google.protobuf`

`ocr.py` should derive its runtime paths only from the active `.venv311` interpreter and its `site-packages` directory.

If that runtime is broken, the service should fail with a clear diagnostic instead of silently falling back to `.ocr311`.

### 2. Separate model cache from runtime validation

Keep a clear separation between:

- model-cache readiness
- Python runtime readiness

The model cache answers:

- are OCR model files present under `D:\Projects\SQA\Metrics_Tool\models`?

The runtime validator answers:

- can the current `.venv311` actually import and initialize PaddleOCR?

These are related but not the same. A ready model cache does not prove the runtime is healthy.

### 3. Add explicit runtime diagnostics

`warm_models.py` should report:

- Python executable path
- site-packages path
- Paddle version
- PaddleOCR version
- Protobuf version
- model cache path
- present assets
- missing assets
- runtime issues

This makes the warm-up command the primary diagnostic surface for environment troubleshooting.

### 4. Use one pinned dependency set

The project should keep one pinned `requirements.txt` for the recognition service. This file becomes the contract for:

- local setup
- bootstrap script behavior
- subsequent demo and smoke scripts

The chosen versions must be verified together on Windows with Python 3.11, especially for:

- `paddlepaddle`
- `paddleocr`
- `protobuf`
- `opencv-python`

### 5. Solve Windows installation constraints in the bootstrap path

The runtime bootstrap should own the Windows-specific setup problems instead of expecting users to fix them manually.

Recommended behavior:

- create `.venv311` if missing
- detect a damaged Paddle runtime and rebuild `.venv311` if necessary
- use a short working path strategy during installation if required by Windows path-length constraints
- install from the pinned requirements file
- warm the OCR models into the project-local cache
- fail with actionable error output if any step is incomplete

### 6. Prefer rebuild over patching when runtime is damaged

If `.venv311` contains a partially broken Paddle installation, the supported recovery path should be:

- remove the broken environment
- recreate `.venv311`
- reinstall dependencies from the pinned requirements file

The design should avoid long-term dependence on copying package directories from a separate environment.

## Testing Strategy

This phase needs runtime-focused verification, not just endpoint coverage.

### Verification layers

#### Layer 1: Import-level validation

Verify that `.venv311` can import:

- `paddle`
- `paddleocr`
- `google.protobuf`

and report their versions.

#### Layer 2: Warm-up validation

Verify that:

- `python -m app.tools.warm_models`

reports:

- ready model cache
- no runtime issues
- stable runtime metadata

#### Layer 3: Service test suite

Run:

- `python -m pytest tests -q`

to verify:

- health endpoint
- structured endpoints
- structured metrics
- image pipeline
- model status

#### Layer 4: Startup validation

Run FastAPI locally and confirm startup succeeds under `.venv311` alone.

## Risks And Mitigations

### Risk: hidden fallback dependence

The service may appear healthy while still pulling packages from `.ocr311`.

Mitigation:

- remove fallback logic entirely
- expose runtime paths in warm-up diagnostics

### Risk: Windows path-length failures during Paddle install

Mitigation:

- move path-shortening logic into bootstrap automation
- treat it as a first-class setup step, not a manual workaround

### Risk: dependency drift

Future installs may silently pick up an incompatible package combination.

Mitigation:

- keep `requirements.txt` fully pinned
- validate exact package versions in warm-up output

### Risk: partial environment corruption

Mitigation:

- prefer deterministic rebuild over ad hoc copying or partial repair

## Acceptance Rule

Do not start Java image-analysis integration until this phase is verified complete with:

1. `.venv311` as the only active OCR runtime
2. `warm_models` reporting no issues
3. `pytest tests -q` passing
4. FastAPI startup succeeding without `.ocr311`

## Design Summary

This phase hardens the Python recognition service from a working-but-fragile runtime into a reproducible, worktree-local execution environment. Its purpose is to remove environment ambiguity before the project moves into Java orchestration, demo automation, and end-to-end verification.
