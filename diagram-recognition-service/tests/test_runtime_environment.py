from __future__ import annotations

import json
import sys
from dataclasses import dataclass
from pathlib import Path
from types import SimpleNamespace

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.services.models import runtime_validator
from app.tools import warm_models


def test_current_site_packages_uses_active_interpreter_path(monkeypatch):
    captured = {}

    def fake_run(command, capture_output, text, check):
        captured["command"] = command
        return SimpleNamespace(stdout="D:/fake/site-packages\n")

    monkeypatch.setattr(runtime_validator.subprocess, "run", fake_run)
    monkeypatch.setattr(runtime_validator.sys, "executable", r"D:\active\python.exe")

    site_packages = runtime_validator.current_site_packages()

    assert captured["command"][0] == r"D:\active\python.exe"
    assert site_packages == Path("D:/fake/site-packages")


def test_collect_runtime_diagnostics_reports_runtime_metadata(monkeypatch):
    class FakeModule:
        def __init__(self, version):
            self.__version__ = version

    def fake_import_module(module_name):
        modules = {
            "paddle": FakeModule("3.0.0"),
            "paddleocr": FakeModule("2.7.0"),
            "google.protobuf": FakeModule("5.1.0"),
        }
        return modules[module_name]

    def fake_version(package_name):
        versions = {
            "paddlepaddle": "3.0.0",
            "paddleocr": "2.7.0",
            "protobuf": "5.1.0",
        }
        return versions[package_name]

    monkeypatch.setattr(runtime_validator.importlib, "import_module", fake_import_module)
    monkeypatch.setattr(runtime_validator.importlib.metadata, "version", fake_version)
    monkeypatch.setattr(runtime_validator, "current_site_packages", lambda python_executable=None: Path("D:/site-packages"))
    monkeypatch.setattr(runtime_validator.sys, "executable", r"D:\active\python.exe")

    diagnostics = runtime_validator.collect_runtime_diagnostics()

    assert diagnostics.pythonExecutable == r"D:\active\python.exe"
    assert Path(diagnostics.sitePackagesPath) == Path("D:/site-packages")
    assert diagnostics.paddleVersion == "3.0.0"
    assert diagnostics.paddleocrVersion == "2.7.0"
    assert diagnostics.protobufVersion == "5.1.0"
    assert diagnostics.issues == []


def test_warm_models_main_emits_runtime_metadata(monkeypatch, capsys):
    @dataclass(frozen=True)
    class CacheStatus:
        ready: bool
        model_cache_path: Path
        present_assets: list[str]
        missing_assets: list[str]

    monkeypatch.setattr(warm_models, "warm_paddleocr_assets", lambda: Path("D:/cache/paddleocr"))
    monkeypatch.setattr(warm_models, "extract_text_blocks", lambda _image: (None, []))
    monkeypatch.setattr(
        warm_models,
        "describe_model_cache",
        lambda: CacheStatus(True, Path("D:/cache"), ["paddleocr"], []),
    )
    monkeypatch.setattr(
        warm_models,
        "collect_runtime_diagnostics",
        lambda: runtime_validator.RuntimeDiagnostics(
            pythonExecutable=r"D:\active\python.exe",
            sitePackagesPath=str(Path("D:/site-packages")),
            paddleVersion="3.0.0",
            paddleocrVersion="2.7.0",
            protobufVersion="5.1.0",
            issues=[],
        ),
    )

    exit_code = warm_models.main()
    payload = json.loads(capsys.readouterr().out)

    assert exit_code == 0
    assert payload["runtimeMetadata"] == {
        "pythonExecutable": r"D:\active\python.exe",
        "sitePackagesPath": str(Path("D:/site-packages")),
        "paddleVersion": "3.0.0",
        "paddleocrVersion": "2.7.0",
        "protobufVersion": "5.1.0",
        "issues": [],
    }
