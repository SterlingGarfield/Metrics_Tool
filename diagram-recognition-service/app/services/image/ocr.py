from __future__ import annotations

from dataclasses import dataclass
from functools import lru_cache
import importlib
import os
from pathlib import Path
import sys
import warnings

import numpy as np

from app.services.models.download_manager import warm_paddleocr_assets
from app.services.models.runtime_validator import current_site_packages


@dataclass(frozen=True)
class TextBlock:
    text: str
    confidence: float
    box: tuple[int, int, int, int]

    @property
    def center(self) -> tuple[float, float]:
        x, y, w, h = self.box
        return (x + (w / 2.0), y + (h / 2.0))


def extract_text_blocks(image: np.ndarray) -> tuple[list[TextBlock], list[str]]:
    paddleocr_home = warm_paddleocr_assets()
    try:
        engine = _load_ocr_engine(str(paddleocr_home), str(current_site_packages()))
    except Exception as exc:  # pragma: no cover - exercised as a soft fallback
        return [], [f"PaddleOCR unavailable: {exc}"]

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


@lru_cache(maxsize=4)
def _load_ocr_engine(paddleocr_home: str, site_packages: str):
    site_packages_path = Path(site_packages)
    env_root = site_packages_path.parents[1]
    _prepare_paddle_runtime(env_root, site_packages_path)
    _purge_paddle_modules()
    importlib.invalidate_caches()
    warnings.filterwarnings("ignore", category=DeprecationWarning, module=r"paddle\.base\.proto\..*")
    os.environ["PADDLEOCR_HOME"] = paddleocr_home
    from paddleocr import PaddleOCR

    return PaddleOCR(use_angle_cls=True, lang="ch", show_log=False)


def _prepare_paddle_runtime(env_root: Path, site_packages: Path) -> None:
    candidate_paths = [
        site_packages,
        env_root,
        env_root / "Library" / "bin",
        env_root / "Scripts",
        env_root / "libs",
        site_packages / "paddle" / "libs",
    ]
    if str(site_packages) not in sys.path:
        sys.path.insert(0, str(site_packages))
    existing_parts = os.environ.get("PATH", "").split(os.pathsep)
    prepend_parts: list[str] = []
    for candidate in candidate_paths:
        if not candidate.exists():
            continue
        text = str(candidate)
        if text not in existing_parts:
            prepend_parts.append(text)
        add_dll_directory = getattr(os, "add_dll_directory", None)
        if add_dll_directory is not None:
            try:
                add_dll_directory(text)
            except (FileNotFoundError, OSError):
                pass
    if prepend_parts:
        os.environ["PATH"] = os.pathsep.join(prepend_parts + existing_parts)


def _runtime_site_packages() -> list[Path]:
    return [current_site_packages()]


def _purge_paddle_modules() -> None:
    for module_name in list(sys.modules):
        if module_name.startswith("paddle") or module_name.startswith("google.protobuf"):
            sys.modules.pop(module_name, None)
