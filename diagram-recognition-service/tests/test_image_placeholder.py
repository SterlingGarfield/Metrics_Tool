import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from fastapi.testclient import TestClient
from app.main import app


def test_image_endpoint_returns_not_implemented_until_pipeline_is_ready():
    client = TestClient(app)

    response = client.post(
        '/recognition/analyze/image',
        data={'diagramType': 'class'},
        files={'file': ('library-domain.png', b'fake-image-data', 'image/png')},
    )

    assert response.status_code == 501
    assert response.json()['detail'] == 'Image recognition pipeline is not implemented yet'
