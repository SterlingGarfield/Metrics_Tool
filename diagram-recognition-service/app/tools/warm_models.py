from __future__ import annotations

import json
import sys

from app.services.image.ocr import extract_text_blocks
from app.services.models.download_manager import describe_model_cache, warm_paddleocr_assets
from app.services.models.runtime_validator import collect_runtime_diagnostics


def main() -> int:
    paddleocr_cache = warm_paddleocr_assets()
    _, probe_issues = extract_text_blocks(_blank_probe_image())
    status = describe_model_cache()
    runtime_diagnostics = collect_runtime_diagnostics()
    payload = {
        "modelCachePath": str(status.model_cache_path),
        "paddleocrCachePath": str(paddleocr_cache),
        "ready": status.ready,
        "presentAssets": status.present_assets,
        "missingAssets": status.missing_assets,
        **runtime_diagnostics.to_payload(),
        "ocrProbeIssues": probe_issues,
    }
    print(json.dumps(payload, ensure_ascii=True, indent=2))
    return 0 if not runtime_diagnostics.issues and not probe_issues else 1


def _blank_probe_image():
    import numpy as np

    return np.full((96, 240, 3), 255, dtype=np.uint8)


if __name__ == "__main__":
    raise SystemExit(main())
