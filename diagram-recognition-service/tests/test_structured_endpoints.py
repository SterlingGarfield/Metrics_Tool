import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
WORKTREE_ROOT = SERVICE_ROOT.parent
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from fastapi.testclient import TestClient
from app.main import app


def _read_sample(relative_path: str) -> str:
    return (WORKTREE_ROOT / relative_path).read_text(encoding='utf-8')


def test_structured_endpoint_parses_plantuml_class_diagram():
    client = TestClient(app)
    payload = {
        'diagramType': 'class',
        'fileName': 'library-domain.puml',
        'sourceSuffix': 'puml',
        'source': _read_sample('samples/diagram-inputs/structured/class/library-domain.puml'),
    }

    response = client.post('/recognition/analyze/structured', json=payload)

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'class'
    assert body['sourceType'] == 'structured'
    assert {item['name'] for item in body['elements']} == {'Book', 'Author', 'Catalog'}
    assert len(body['relations']) == 2
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['classCount'] == 3
    assert metrics['relationshipDensity'] > 0


def test_structured_endpoint_parses_mermaid_flowchart():
    client = TestClient(app)
    payload = {
        'diagramType': 'flow',
        'fileName': 'order-approval.mmd',
        'sourceSuffix': 'mmd',
        'source': _read_sample('samples/diagram-inputs/structured/flow/order-approval.mmd'),
    }

    response = client.post('/recognition/analyze/structured', json=payload)

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'flow'
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['decisionNodeCount'] == 1
    assert metrics['pathCountEstimate'] == 2
    assert any(item['type'] == 'Decision' for item in body['elements'])


def test_structured_endpoint_parses_plantuml_usecase_diagram():
    client = TestClient(app)
    payload = {
        'diagramType': 'usecase',
        'fileName': 'campus-repair.puml',
        'sourceSuffix': 'puml',
        'source': _read_sample('samples/diagram-inputs/structured/usecase/campus-repair.puml'),
    }

    response = client.post('/recognition/analyze/structured', json=payload)

    assert response.status_code == 200
    body = response.json()
    assert body['diagramType'] == 'usecase'
    metrics = {item['name']: item['value'] for item in body['metrics']}
    assert metrics['actorCount'] == 2
    assert metrics['useCaseCount'] == 3
    assert metrics['includeRelationCount'] == 1
    assert metrics['extendRelationCount'] == 1
