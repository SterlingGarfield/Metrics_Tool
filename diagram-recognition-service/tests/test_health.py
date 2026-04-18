import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from fastapi.testclient import TestClient
from app.main import app


def test_health_returns_service_status_and_model_cache_path():
    client = TestClient(app)

    response = client.get('/recognition/health')

    assert response.status_code == 200
    payload = response.json()
    assert payload['status'] == 'UP'
    assert payload['service'] == 'diagram-recognition'
    assert payload['modelCachePath'].endswith('models')
