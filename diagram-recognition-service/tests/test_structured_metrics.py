import sys
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]
WORKTREE_ROOT = SERVICE_ROOT.parent
if str(SERVICE_ROOT) not in sys.path:
    sys.path.insert(0, str(SERVICE_ROOT))

from app.services.structured.metric_extractor import build_structured_analysis


def _read_sample(relative_path: str) -> str:
    return (WORKTREE_ROOT / relative_path).read_text(encoding='utf-8')


def test_metric_extractor_counts_plantuml_class_structure():
    analysis = build_structured_analysis(
        diagram_type='class',
        file_name='library-domain.puml',
        source_suffix='puml',
        source=_read_sample('samples/diagram-inputs/structured/class/library-domain.puml'),
    )

    metrics = {item.name: item.value for item in analysis.metrics}
    assert metrics['classCount'] == 3
    assert metrics['attributeCount'] == 2
    assert metrics['methodCount'] == 2
    assert metrics['associationCount'] == 1
    assert metrics['dependencyCount'] == 1


def test_metric_extractor_counts_mermaid_flow_structure():
    analysis = build_structured_analysis(
        diagram_type='flow',
        file_name='order-approval.mmd',
        source_suffix='mmd',
        source=_read_sample('samples/diagram-inputs/structured/flow/order-approval.mmd'),
    )

    metrics = {item.name: item.value for item in analysis.metrics}
    assert metrics['nodeCount'] == 6
    assert metrics['decisionNodeCount'] == 1
    assert metrics['terminalNodeCount'] == 2
    assert metrics['estimatedCyclomaticComplexity'] == 2
    assert metrics['maxBranchFanOut'] == 2


def test_metric_extractor_counts_plantuml_usecase_structure():
    analysis = build_structured_analysis(
        diagram_type='usecase',
        file_name='campus-repair.puml',
        source_suffix='puml',
        source=_read_sample('samples/diagram-inputs/structured/usecase/campus-repair.puml'),
    )

    metrics = {item.name: item.value for item in analysis.metrics}
    assert metrics['actorCount'] == 2
    assert metrics['useCaseCount'] == 3
    assert metrics['actorUseCaseAssociationCount'] == 3
    assert metrics['includeRelationCount'] == 1
    assert metrics['extendRelationCount'] == 1
