import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
WORKTREE_ROOT = SERVICE_ROOT.parent
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from fastapi.testclient import TestClient
from app.main import app


def _image_path(relative_path: str) -> Path:
    return WORKTREE_ROOT / relative_path


def _post_image(diagram_type: str, relative_path: str):
    client = TestClient(app)
    image_path = _image_path(relative_path)
    with image_path.open('rb') as fh:
        return client.post(
            '/recognition/analyze/image',
            data={'diagramType': diagram_type},
            files={'file': (image_path.name, fh, 'image/png' if image_path.suffix.lower() == '.png' else 'image/jpeg')},
        )


def test_image_endpoint_recognizes_class_diagram_fixture():
    response = _post_image('class', 'samples/diagram-inputs/images/class/library-domain-zh-en.png')

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'class'
    assert body['sourceType'] == 'image'
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['classCount'] >= 3
    assert metrics['relationshipDensity'] > 0
    assert body['confidence']['overall'] > 0.5
    assert len(body['elements']) >= 3
    assert len(body['relations']) >= 2


def test_image_endpoint_recognizes_flow_diagram_fixture():
    response = _post_image('flow', 'samples/diagram-inputs/images/flow/order-approval-zh-en.jpg')

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'flow'
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['decisionNodeCount'] >= 1
    assert metrics['pathCountEstimate'] >= 2
    assert metrics['estimatedCyclomaticComplexity'] >= 2
    assert len(body['elements']) >= 5


def test_image_endpoint_recognizes_usecase_diagram_fixture():
    response = _post_image('usecase', 'samples/diagram-inputs/images/usecase/campus-repair-zh-en.png')

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'usecase'
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['actorCount'] >= 2
    assert metrics['useCaseCount'] >= 3
    assert metrics['includeRelationCount'] >= 1
    assert len(body['relations']) >= 3


def test_low_confidence_fixture_is_marked_not_directly_measurable():
    response = _post_image('class', 'samples/diagram-inputs/images/low-confidence/noisy-class-zh-en.png')

    assert response.status_code == 200
    body = response.json()
    assert body['confidence']['directlyMeasurable'] is False
    assert any(issue['level'] == 'warning' for issue in body['issues'])
