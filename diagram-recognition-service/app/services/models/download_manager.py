from __future__ import annotations

import os
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from app.config import get_settings

REQUIRED_MODEL_ASSETS = ("paddleocr",)
_LOCAL_PADDLEOCR_CANDIDATES = (
    lambda: os.getenv("METRICS_TOOL_PADDLEOCR_SOURCE"),
    lambda: os.getenv("PADDLEOCR_HOME"),
    lambda: str(Path.home() / ".paddleocr"),
)
_PADDLEOCR_SENTINELS = (
    Path("whl/cls"),
    Path("whl/det"),
    Path("whl/rec"),
)


@dataclass(frozen=True)
class ModelCacheStatus:
    ready: bool
    model_cache_path: Path
    present_assets: list[str]
    missing_assets: list[str]


def get_model_cache_path() -> Path:
    settings = get_settings()
    return Path(settings.model_cache_path).resolve()


def get_paddleocr_cache_path() -> Path:
    return get_model_cache_path() / "paddleocr"


def describe_model_cache() -> ModelCacheStatus:
    model_cache_path = get_model_cache_path()
    present_assets = [asset for asset in REQUIRED_MODEL_ASSETS if (model_cache_path / asset).exists()]
    missing_assets = [asset for asset in REQUIRED_MODEL_ASSETS if asset not in present_assets]
    return ModelCacheStatus(
        ready=not missing_assets,
        model_cache_path=model_cache_path,
        present_assets=present_assets,
        missing_assets=missing_assets,
    )


def ensure_model_cache_dirs() -> Path:
    model_cache_path = get_model_cache_path()
    model_cache_path.mkdir(parents=True, exist_ok=True)
    return model_cache_path


def has_warmed_paddleocr_assets(cache_path: Path | None = None) -> bool:
    paddleocr_cache = cache_path or get_paddleocr_cache_path()
    return all((paddleocr_cache / sentinel).exists() for sentinel in _PADDLEOCR_SENTINELS)


def warm_paddleocr_assets() -> Path:
    paddleocr_cache = get_paddleocr_cache_path()
    paddleocr_cache.mkdir(parents=True, exist_ok=True)
    if has_warmed_paddleocr_assets(paddleocr_cache):
        return paddleocr_cache

    for candidate in _iter_local_paddleocr_candidates():
        if _copy_tree(candidate, paddleocr_cache):
            break

    return paddleocr_cache


def _iter_local_paddleocr_candidates() -> Iterable[Path]:
    seen: set[Path] = set()
    for factory in _LOCAL_PADDLEOCR_CANDIDATES:
        raw_path = factory()
        if not raw_path:
            continue
        candidate = Path(raw_path).expanduser().resolve()
        if candidate in seen or not candidate.exists():
            continue
        seen.add(candidate)
        yield candidate


def _copy_tree(source: Path, destination: Path) -> bool:
    if source.resolve() == destination.resolve():
        return has_warmed_paddleocr_assets(destination)

    if not _looks_like_paddleocr_cache(source):
        return False

    shutil.copytree(source, destination, dirs_exist_ok=True)
    return True


def _looks_like_paddleocr_cache(path: Path) -> bool:
    if not path.exists():
        return False
    if all((path / sentinel).exists() for sentinel in _PADDLEOCR_SENTINELS):
        return True
    whl_dir = path / "whl"
    return whl_dir.exists() and any(whl_dir.rglob("inference.pdmodel"))
