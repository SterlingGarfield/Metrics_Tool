# Python Recognition Runtime Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `diagram-recognition-service/.venv311` the only supported OCR runtime for the Python recognition service, remove the `.ocr311` fallback, and make runtime setup reproducible on Windows through one bootstrap path.

**Architecture:** Keep the recognition service API and image pipeline behavior intact while separating model-cache readiness from Python runtime readiness. Add a dedicated runtime validator, route all OCR startup through the active `.venv311` interpreter only, and move Windows-specific environment repair into a bootstrap script that can recreate or repair the local virtual environment deterministically.

**Tech Stack:** Python 3.11, FastAPI, PaddleOCR, PaddlePaddle, Protobuf, OpenCV, Pytest, PowerShell, Windows virtual environments

---

## File Structure

### Python recognition service files to modify

- Modify: `diagram-recognition-service/requirements.txt`
- Modify: `diagram-recognition-service/app/services/image/ocr.py`
- Modify: `diagram-recognition-service/app/tools/warm_models.py`

### Python recognition service files to create

- Create: `diagram-recognition-service/app/services/models/runtime_validator.py`
- Create: `diagram-recognition-service/tests/test_runtime_environment.py`

### Script files to create

- Create: `scripts/bootstrap-models.ps1`

## Task 1: Add Runtime Diagnostics And Warm-Up Verification

**Files:**
- Create: `diagram-recognition-service/app/services/models/runtime_validator.py`
- Modify: `diagram-recognition-service/app/tools/warm_models.py`
- Create: `diagram-recognition-service/tests/test_runtime_environment.py`

- [ ] **Step 1: Write the failing runtime diagnostics tests**

Create `diagram-recognition-service/tests/test_runtime_environment.py` with:

```python
import json
from pathlib import Path
from types import SimpleNamespace

import app.services.models.runtime_validator as runtime_validator
from app.tools import warm_models


def test_current_site_packages_uses_active_interpreter(monkeypatch, tmp_path):
    fake_python = tmp_path / '.venv311' / 'python.exe'
    fake_python.parent.mkdir(parents=True, exist_ok=True)
    monkeypatch.setattr(runtime_validator.sys, 'executable', str(fake_python))

    site_packages = runtime_validator.current_site_packages()

    assert site_packages == fake_python.parent / 'Lib' / 'site-packages'


def test_collect_runtime_diagnostics_reports_versions(monkeypatch, tmp_path):
    fake_python = tmp_path / '.venv311' / 'python.exe'
    fake_python.parent.mkdir(parents=True, exist_ok=True)
    monkeypatch.setattr(runtime_validator.sys, 'executable', str(fake_python))
    monkeypatch.setattr(runtime_validator, 'import_module', lambda name: object())
    monkeypatch.setattr(
        runtime_validator,
        'package_version',
        lambda name: {
            'paddlepaddle': '2.6.2',
            'paddleocr': '2.8.1',
            'protobuf': '3.20.2',
        }[name],
    )

    diagnostics = runtime_validator.collect_runtime_diagnostics()

    assert diagnostics.pythonExecutable == str(fake_python)
    assert diagnostics.sitePackagesPath.endswith('.venv311\\Lib\\site-packages')
    assert diagnostics.paddleVersion == '2.6.2'
    assert diagnostics.paddleocrVersion == '2.8.1'
    assert diagnostics.protobufVersion == '3.20.2'
    assert diagnostics.issues == []


def test_warm_models_includes_runtime_metadata(monkeypatch, capsys, tmp_path):
    monkeypatch.setattr(warm_models, 'warm_paddleocr_assets', lambda: tmp_path / 'paddleocr')
    monkeypatch.setattr(
        warm_models,
        'describe_model_cache',
        lambda: SimpleNamespace(
            ready=True,
            model_cache_path=tmp_path,
            present_assets=['paddleocr'],
            missing_assets=[],
        ),
    )
    monkeypatch.setattr(
        warm_models,
        'collect_runtime_diagnostics',
        lambda: runtime_validator.RuntimeDiagnostics(
            pythonExecutable=str(tmp_path / '.venv311' / 'python.exe'),
            sitePackagesPath=str(tmp_path / '.venv311' / 'Lib' / 'site-packages'),
            paddleVersion='2.6.2',
            paddleocrVersion='2.8.1',
            protobufVersion='3.20.2',
            issues=[],
        ),
    )

    exit_code = warm_models.main()
    payload = json.loads(capsys.readouterr().out)

    assert exit_code == 0
    assert payload['pythonExecutable'].endswith('.venv311\\python.exe')
    assert payload['paddleVersion'] == '2.6.2'
    assert payload['paddleocrVersion'] == '2.8.1'
    assert payload['protobufVersion'] == '3.20.2'
    assert payload['issues'] == []
```

- [ ] **Step 2: Run the new runtime diagnostics tests and confirm they fail**

Run:

```powershell
.\.venv311\python.exe -m pytest tests/test_runtime_environment.py -q
```

Expected: FAIL because `runtime_validator.py` does not exist and `warm_models.py` does not emit runtime metadata yet.

- [ ] **Step 3: Implement the runtime validator**

Create `diagram-recognition-service/app/services/models/runtime_validator.py` with:

```python
from __future__ import annotations

from dataclasses import dataclass
from importlib import import_module
from importlib.metadata import version as package_version
from pathlib import Path
import sys


@dataclass(frozen=True)
class RuntimeDiagnostics:
    pythonExecutable: str
    sitePackagesPath: str
    paddleVersion: str | None
    paddleocrVersion: str | None
    protobufVersion: str | None
    issues: list[str]


def current_site_packages(python_executable: str | None = None) -> Path:
    env_root = Path(python_executable or sys.executable).resolve().parent
    return env_root / 'Lib' / 'site-packages'


def collect_runtime_diagnostics() -> RuntimeDiagnostics:
    issues: list[str] = []
    paddle_version: str | None = None
    paddleocr_version: str | None = None
    protobuf_version: str | None = None

    for module_name, package_name, attr_name in (
        ('paddle', 'paddlepaddle', 'paddle_version'),
        ('paddleocr', 'paddleocr', 'paddleocr_version'),
        ('google.protobuf', 'protobuf', 'protobuf_version'),
    ):
        try:
            import_module(module_name)
            resolved_version = package_version(package_name)
            if attr_name == 'paddle_version':
                paddle_version = resolved_version
            elif attr_name == 'paddleocr_version':
                paddleocr_version = resolved_version
            else:
                protobuf_version = resolved_version
        except Exception as exc:
            issues.append(f'{module_name} import failed: {exc}')

    return RuntimeDiagnostics(
        pythonExecutable=str(Path(sys.executable).resolve()),
        sitePackagesPath=str(current_site_packages().resolve()),
        paddleVersion=paddle_version,
        paddleocrVersion=paddleocr_version,
        protobufVersion=protobuf_version,
        issues=issues,
    )
```

- [ ] **Step 4: Emit runtime diagnostics from `warm_models.py`**

Update `diagram-recognition-service/app/tools/warm_models.py` to:

```python
from __future__ import annotations

import json

from app.services.models.download_manager import describe_model_cache, warm_paddleocr_assets
from app.services.models.runtime_validator import collect_runtime_diagnostics


def main() -> int:
    paddleocr_cache = warm_paddleocr_assets()
    cache_status = describe_model_cache()
    runtime = collect_runtime_diagnostics()
    payload = {
        'pythonExecutable': runtime.pythonExecutable,
        'sitePackagesPath': runtime.sitePackagesPath,
        'paddleVersion': runtime.paddleVersion,
        'paddleocrVersion': runtime.paddleocrVersion,
        'protobufVersion': runtime.protobufVersion,
        'modelCachePath': str(cache_status.model_cache_path),
        'paddleocrCachePath': str(paddleocr_cache),
        'ready': cache_status.ready,
        'presentAssets': cache_status.present_assets,
        'missingAssets': cache_status.missing_assets,
        'issues': runtime.issues,
    }
    print(json.dumps(payload, ensure_ascii=True, indent=2))
    return 0 if not runtime.issues else 1
```

- [ ] **Step 5: Re-run the runtime diagnostics tests**

Run:

```powershell
.\.venv311\python.exe -m pytest tests/test_runtime_environment.py -q
```

Expected: PASS.

- [ ] **Step 6: Commit the runtime diagnostics checkpoint**

```powershell
git add diagram-recognition-service/app/services/models/runtime_validator.py diagram-recognition-service/app/tools/warm_models.py diagram-recognition-service/tests/test_runtime_environment.py
git commit -m "python: add runtime diagnostics for recognition service"
```

## Task 2: Remove `.ocr311` Fallback And Lock The OCR Runtime To `.venv311`

**Files:**
- Modify: `diagram-recognition-service/requirements.txt`
- Modify: `diagram-recognition-service/app/services/image/ocr.py`
- Modify: `diagram-recognition-service/tests/test_runtime_environment.py`

- [ ] **Step 1: Extend the runtime tests with a failing isolation test**

Append this test to `diagram-recognition-service/tests/test_runtime_environment.py`:

```python
import app.services.image.ocr as ocr


def test_ocr_runtime_site_packages_uses_current_environment_only(monkeypatch, tmp_path):
    fake_python = tmp_path / '.venv311' / 'python.exe'
    fake_python.parent.mkdir(parents=True, exist_ok=True)
    monkeypatch.setattr(ocr.sys, 'executable', str(fake_python))

    runtime_paths = ocr._runtime_site_packages()

    assert runtime_paths == [fake_python.parent / 'Lib' / 'site-packages']
```

- [ ] **Step 2: Run the updated runtime tests and confirm they fail**

Run:

```powershell
.\.venv311\python.exe -m pytest tests/test_runtime_environment.py -q
```

Expected: FAIL because `ocr.py` still searches `.ocr311`.

- [ ] **Step 3: Lock the dependency set in `requirements.txt`**

Update `diagram-recognition-service/requirements.txt` to:

```text
fastapi==0.115.6
uvicorn[standard]==0.32.1
pydantic==2.10.5
python-multipart==0.0.20
networkx==3.1
pytest==8.3.4
httpx==0.28.1
numpy==1.26.4
protobuf==3.20.2
opencv-python==4.10.0.84 ; python_version >= "3.11"
pillow==10.4.0 ; python_version >= "3.11"
paddlepaddle==2.6.2 ; python_version >= "3.11"
paddleocr==2.8.1 ; python_version >= "3.11"
```

- [ ] **Step 4: Remove the fallback logic from `ocr.py`**

Update `diagram-recognition-service/app/services/image/ocr.py` so `_runtime_site_packages()` only resolves the active `.venv311` site-packages and `extract_text_blocks()` loads PaddleOCR once from that environment:

```python
from app.services.models.runtime_validator import current_site_packages


def extract_text_blocks(image: np.ndarray) -> tuple[list[TextBlock], list[str]]:
    paddleocr_home = warm_paddleocr_assets()
    try:
        engine = _load_ocr_engine(str(paddleocr_home), str(current_site_packages()))
    except Exception as exc:  # pragma: no cover - soft failure path
        return [], [f'PaddleOCR unavailable: {exc}']

    raw_result = engine.ocr(image, cls=True)
    line_entries = raw_result[0] if raw_result and raw_result[0] else []
    blocks: list[TextBlock] = []
    for box_points, payload in line_entries:
        text, confidence = payload
        if not text:
            continue
        xs = [int(point[0]) for point in box_points]
        ys = [int(point[1]) for point in box_points]
        blocks.append(
            TextBlock(
                text=text.strip(),
                confidence=float(confidence),
                box=(min(xs), min(ys), max(xs) - min(xs), max(ys) - min(ys)),
            )
        )
    return blocks, []


def _runtime_site_packages() -> list[Path]:
    return [current_site_packages()]
```

Keep `_prepare_paddle_runtime()` but ensure it only prepends paths derived from the current interpreter and its own `site-packages`.

- [ ] **Step 5: Re-run the runtime tests and the image pipeline tests**

Run:

```powershell
.\.venv311\python.exe -m pytest tests/test_runtime_environment.py tests/test_image_pipeline.py tests/test_models_status.py -q
```

Expected: PASS, and the service still recognizes the existing image fixtures while no longer using `.ocr311`.

- [ ] **Step 6: Commit the runtime-isolation checkpoint**

```powershell
git add diagram-recognition-service/requirements.txt diagram-recognition-service/app/services/image/ocr.py diagram-recognition-service/tests/test_runtime_environment.py
git commit -m "python: isolate OCR runtime to local venv"
```

## Task 3: Add A Windows-Safe Bootstrap Script For `.venv311`

**Files:**
- Create: `scripts/bootstrap-models.ps1`

- [ ] **Step 1: Prove the bootstrap path is missing**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -Rebuild
```

Expected: FAIL because `scripts/bootstrap-models.ps1` does not exist yet.

- [ ] **Step 2: Create the bootstrap script with rebuild and short-path support**

Create `scripts/bootstrap-models.ps1` with:

```powershell
param(
    [switch]$Rebuild,
    [string]$ShortDrive = 'R:'
)

$root = Resolve-Path "$PSScriptRoot\.."
$serviceRoot = Join-Path $root 'diagram-recognition-service'
$venvPath = Join-Path $serviceRoot '.venv311'
$requirementsPath = Join-Path $serviceRoot 'requirements.txt'
$modelCache = 'D:/Projects/SQA/Metrics_Tool/models/paddleocr'
$substWasCreated = $false

function Remove-ShortDrive {
    param([string]$Drive)
    cmd /c "subst $Drive /d" | Out-Null
}

try {
    if ($Rebuild -and (Test-Path $venvPath)) {
        Remove-Item -LiteralPath $venvPath -Recurse -Force
    }

    cmd /c "subst $ShortDrive `"$serviceRoot`"" | Out-Null
    $substWasCreated = $true
    $shortRoot = "$ShortDrive\"
    $shortVenv = Join-Path $shortRoot '.venv311'
    $shortRequirements = Join-Path $shortRoot 'requirements.txt'

    if (-not (Test-Path $venvPath)) {
        py -3.11 -m venv $shortVenv
    }

    & "$shortVenv\python.exe" -m pip install --upgrade pip
    & "$shortVenv\python.exe" -m pip install -r $shortRequirements

    $env:PADDLEOCR_HOME = $modelCache
    & "$shortVenv\python.exe" -m app.tools.warm_models
} finally {
    if ($substWasCreated) {
        Remove-ShortDrive -Drive $ShortDrive
    }
}
```

- [ ] **Step 3: Run the bootstrap script in rebuild mode**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-models.ps1 -Rebuild
```

Expected: `.venv311` is created or repaired, dependencies install from `requirements.txt`, and `warm_models` finishes with no issues.

- [ ] **Step 4: Run warm-up explicitly from `.venv311` after bootstrap**

Run:

```powershell
cd .\diagram-recognition-service
.\.venv311\python.exe -m app.tools.warm_models
```

Expected: output reports the `.venv311` executable, the `.venv311` site-packages path, the pinned package versions, and `issues: []`.

- [ ] **Step 5: Commit the bootstrap checkpoint**

```powershell
git add scripts/bootstrap-models.ps1
git commit -m "scripts: bootstrap local recognition runtime"
```

## Task 4: Run Full Phase A Verification And Lock The Acceptance Evidence

**Files:**
- Modify: `diagram-recognition-service/app/services/image/ocr.py` only if verification exposes a concrete bug
- Modify: `diagram-recognition-service/app/tools/warm_models.py` only if verification exposes a concrete bug
- Modify: `scripts/bootstrap-models.ps1` only if verification exposes a concrete bug

- [ ] **Step 1: Verify the code no longer references `.ocr311`**

Run:

```powershell
Select-String -Path .\diagram-recognition-service\app\services\image\ocr.py -Pattern '\.ocr311'
```

Expected: no matches.

- [ ] **Step 2: Run the full Python recognition-service test suite**

Run:

```powershell
cd .\diagram-recognition-service
.\.venv311\python.exe -m pytest tests -q
```

Expected: PASS.

- [ ] **Step 3: Start the FastAPI service with `.venv311` and verify startup**

Run:

```powershell
cd .\diagram-recognition-service
Start-Process powershell -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', '.\.venv311\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8090'
Start-Sleep -Seconds 8
Invoke-RestMethod -Uri 'http://127.0.0.1:8090/recognition/health'
Invoke-RestMethod -Uri 'http://127.0.0.1:8090/recognition/models/status'
```

Expected:

- health returns `status = UP`
- models status returns `ready = true`
- both endpoints respond while only `.venv311` is in use

- [ ] **Step 4: Fix any concrete verification failures and re-run the same commands**

If any command in Steps 2-3 fails, make only the smallest necessary code change, then re-run the exact failing command before moving on.

- [ ] **Step 5: Commit the Phase A runtime-hardening completion checkpoint**

```powershell
git add diagram-recognition-service/requirements.txt diagram-recognition-service/app/services/image/ocr.py diagram-recognition-service/app/services/models/runtime_validator.py diagram-recognition-service/app/tools/warm_models.py diagram-recognition-service/tests/test_runtime_environment.py scripts/bootstrap-models.ps1
git commit -m "python: harden recognition runtime bootstrap"
```

## Self-Review Checklist

- Spec coverage: runtime isolation, reproducible bootstrap, diagnostics, full pytest verification, and startup validation all have corresponding tasks.
- Placeholder scan: no task depends on an unspecified helper, hidden manual step, or implied version choice.
- Type consistency: runtime metadata fields use one vocabulary across the validator, warm-up output, and tests: `pythonExecutable`, `sitePackagesPath`, `paddleVersion`, `paddleocrVersion`, `protobufVersion`, and `issues`.
