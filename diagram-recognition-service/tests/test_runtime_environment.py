from __future__ import annotations

import json
import sys
from dataclasses import dataclass
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.services.image import ocr
from app.services.models import runtime_validator
from app.tools import warm_models


def test_current_site_packages_uses_active_interpreter_path(monkeypatch):
    fake_interpreter = r"D:\active\python.exe"
    monkeypatch.setattr(runtime_validator.sys, "executable", fake_interpreter)

    site_packages = runtime_validator.current_site_packages()

    assert site_packages == Path(fake_interpreter).parent / "Lib" / "site-packages"


def test_runtime_site_packages_only_uses_active_interpreter_site_packages(monkeypatch):
    fake_interpreter = r"D:\active\python.exe"
    active_site_packages = Path(fake_interpreter).parent / "Lib" / "site-packages"
    alternate_site_packages = Path(ocr.__file__).resolve().parents[6] / ".ocr311" / "Lib" / "site-packages"

    monkeypatch.setattr(runtime_validator.sys, "executable", fake_interpreter)

    def fake_exists(self: Path) -> bool:
        candidate = Path(self)
        if candidate in {
            active_site_packages,
            alternate_site_packages,
            active_site_packages / "paddle" / "base" / "libpaddle.pyd",
            alternate_site_packages / "paddle" / "base" / "libpaddle.pyd",
        }:
            return True
        return False

    monkeypatch.setattr(Path, "exists", fake_exists, raising=True)

    assert ocr._runtime_site_packages() == [active_site_packages]


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

    monkeypatch.setattr(runtime_validator.importlib, "import_module", fake_import_module)
    monkeypatch.setattr(runtime_validator.sys, "executable", r"D:\active\python.exe")
    monkeypatch.setattr(runtime_validator, "current_site_packages", lambda python_executable=None: Path(r"D:\active\Lib\site-packages"))

    diagnostics = runtime_validator.collect_runtime_diagnostics()

    assert diagnostics.pythonExecutable == r"D:\active\python.exe"
    assert Path(diagnostics.sitePackagesPath) == Path(r"D:\active\Lib\site-packages")
    assert diagnostics.paddleVersion == "3.0.0"
    assert diagnostics.paddleocrVersion == "2.7.0"
    assert diagnostics.protobufVersion == "5.1.0"
    assert diagnostics.issues == []


def test_collect_runtime_diagnostics_records_issue_when_metadata_lookup_fails(monkeypatch):
    class FakeModule:
        def __init__(self, version=None):
            if version is not None:
                self.__version__ = version

    def fake_import_module(module_name):
        modules = {
            "paddle": FakeModule("3.0.0"),
            "paddleocr": FakeModule("2.7.0"),
            "google.protobuf": FakeModule(),
        }
        return modules[module_name]

    def fake_version(package_name):
        if package_name == "protobuf":
            raise runtime_validator.importlib.metadata.PackageNotFoundError
        raise AssertionError(f"unexpected package lookup: {package_name}")

    monkeypatch.setattr(runtime_validator.importlib, "import_module", fake_import_module)
    monkeypatch.setattr(runtime_validator.importlib.metadata, "version", fake_version)
    monkeypatch.setattr(runtime_validator, "current_site_packages", lambda python_executable=None: Path(r"D:\active\Lib\site-packages"))
    monkeypatch.setattr(runtime_validator.sys, "executable", r"D:\active\python.exe")

    diagnostics = runtime_validator.collect_runtime_diagnostics()

    assert diagnostics.protobufVersion is None
    assert any("protobuf" in issue for issue in diagnostics.issues)


def test_warm_models_main_emits_flat_runtime_metadata(monkeypatch, capsys):
    @dataclass(frozen=True)
    class CacheStatus:
        ready: bool
        model_cache_path: Path
        present_assets: list[str]
        missing_assets: list[str]

    monkeypatch.setattr(warm_models, "warm_paddleocr_assets", lambda: Path(r"D:\cache\paddleocr"))
    monkeypatch.setattr(warm_models, "extract_text_blocks", lambda _image: (None, []))
    monkeypatch.setattr(
        warm_models,
        "describe_model_cache",
        lambda: CacheStatus(True, Path(r"D:\cache"), ["paddleocr"], []),
    )
    monkeypatch.setattr(
        warm_models,
        "collect_runtime_diagnostics",
        lambda: runtime_validator.RuntimeDiagnostics(
            pythonExecutable=r"D:\active\python.exe",
            sitePackagesPath=str(Path(r"D:\active\Lib\site-packages")),
            paddleVersion="3.0.0",
            paddleocrVersion="2.7.0",
            protobufVersion="5.1.0",
            issues=[],
        ),
    )

    exit_code = warm_models.main()
    payload = json.loads(capsys.readouterr().out)

    assert exit_code == 0
    assert payload == {
        "modelCachePath": str(Path(r"D:\cache")),
        "paddleocrCachePath": str(Path(r"D:\cache\paddleocr")),
        "ready": True,
        "presentAssets": ["paddleocr"],
        "missingAssets": [],
        "pythonExecutable": r"D:\active\python.exe",
        "sitePackagesPath": str(Path(r"D:\active\Lib\site-packages")),
        "paddleVersion": "3.0.0",
        "paddleocrVersion": "2.7.0",
        "protobufVersion": "5.1.0",
        "issues": [],
        "ocrProbeIssues": [],
    }


def test_warm_models_main_fails_when_runtime_diagnostics_have_issues(monkeypatch, capsys):
    @dataclass(frozen=True)
    class CacheStatus:
        ready: bool
        model_cache_path: Path
        present_assets: list[str]
        missing_assets: list[str]

    monkeypatch.setattr(warm_models, "warm_paddleocr_assets", lambda: Path(r"D:\cache\paddleocr"))
    monkeypatch.setattr(warm_models, "extract_text_blocks", lambda _image: (None, []))
    monkeypatch.setattr(
        warm_models,
        "describe_model_cache",
        lambda: CacheStatus(True, Path(r"D:\cache"), ["paddleocr"], []),
    )
    monkeypatch.setattr(
        warm_models,
        "collect_runtime_diagnostics",
        lambda: runtime_validator.RuntimeDiagnostics(
            pythonExecutable=r"D:\active\python.exe",
            sitePackagesPath=str(Path(r"D:\active\Lib\site-packages")),
            paddleVersion=None,
            paddleocrVersion="2.7.0",
            protobufVersion="5.1.0",
            issues=["Unable to import paddle: missing package"],
        ),
    )

    exit_code = warm_models.main()
    payload = json.loads(capsys.readouterr().out)

    assert exit_code != 0
    assert payload["issues"] == ["Unable to import paddle: missing package"]
    assert payload["ocrProbeIssues"] == []
