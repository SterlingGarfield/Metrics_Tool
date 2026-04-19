import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from fastapi.testclient import TestClient
from app.config import get_settings
from app.main import app


def test_models_status_reports_empty_cache_as_not_ready(tmp_path, monkeypatch):
    monkeypatch.setenv('METRICS_TOOL_MODEL_CACHE', str(tmp_path))
    get_settings.cache_clear()
    client = TestClient(app)

    response = client.get('/recognition/models/status')

    assert response.status_code == 200
    payload = response.json()
    assert payload['ready'] is False
    assert payload['modelsLoaded'] is False
    assert Path(payload['modelCachePath']).resolve() == tmp_path.resolve()
    assert payload['missingAssets']
    get_settings.cache_clear()


def test_models_status_reports_ready_when_required_assets_exist(tmp_path, monkeypatch):
    (tmp_path / 'paddleocr').mkdir(parents=True, exist_ok=True)
    monkeypatch.setenv('METRICS_TOOL_MODEL_CACHE', str(tmp_path))
    get_settings.cache_clear()
    client = TestClient(app)

    response = client.get('/recognition/models/status')

    assert response.status_code == 200
    payload = response.json()
    assert payload['ready'] is True
    assert payload['modelsLoaded'] is True
    assert payload['presentAssets'] == ['paddleocr']
    assert payload['missingAssets'] == []
    get_settings.cache_clear()
