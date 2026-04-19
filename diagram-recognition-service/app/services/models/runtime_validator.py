from __future__ import annotations

import importlib
import importlib.metadata
import sys
from dataclasses import asdict, dataclass
from pathlib import Path


@dataclass(frozen=True)
class RuntimeDiagnostics:
    pythonExecutable: str
    sitePackagesPath: str
    paddleVersion: str | None
    paddleocrVersion: str | None
    protobufVersion: str | None
    issues: list[str]

    def to_payload(self) -> dict[str, object]:
        return asdict(self)


def current_site_packages(python_executable: str | None = None) -> Path:
    interpreter = python_executable or sys.executable
    return Path(interpreter).expanduser().parent / "Lib" / "site-packages"


def collect_runtime_diagnostics() -> RuntimeDiagnostics:
    issues: list[str] = []

    site_packages_path = current_site_packages()
    paddle_version = _collect_package_version("paddle", "paddlepaddle", issues)
    paddleocr_version = _collect_package_version("paddleocr", "paddleocr", issues)
    protobuf_version = _collect_package_version("google.protobuf", "protobuf", issues)

    return RuntimeDiagnostics(
        pythonExecutable=sys.executable,
        sitePackagesPath=str(site_packages_path),
        paddleVersion=paddle_version,
        paddleocrVersion=paddleocr_version,
        protobufVersion=protobuf_version,
        issues=issues,
    )


def _collect_package_version(module_name: str, package_name: str, issues: list[str]) -> str | None:
    try:
        module = importlib.import_module(module_name)
    except Exception as exc:  # pragma: no cover - import failures depend on the environment
        issues.append(f"Unable to import {module_name}: {exc}")
        return None

    version = getattr(module, "__version__", None)
    if version:
        return str(version)

    try:
        return importlib.metadata.version(package_name)
    except importlib.metadata.PackageNotFoundError:
        issues.append(f"Unable to resolve {package_name} version from package metadata")
        return None
    except Exception as exc:  # pragma: no cover - defensive fallback for unexpected metadata errors
        issues.append(f"Unable to resolve {package_name} version from package metadata: {exc}")
        return None
