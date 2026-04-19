from collections import Counter, defaultdict
from typing import Dict, List
from fastapi import HTTPException
from app.models.contracts import (
    ConfidenceSummary,
    DiagramAnalysisResponse,
    DiagramMetricValue,
    ParsedDiagram,
)
from app.services.structured.mermaid_parser import parse_mermaid_class, parse_mermaid_flow
from app.services.structured.plantuml_parser import parse_plantuml_class, parse_plantuml_usecase


def build_structured_analysis(diagram_type: str, file_name: str, source_suffix: str, source: str) -> DiagramAnalysisResponse:
    suffix = source_suffix.lower().lstrip('.')
    parsed = _dispatch_parse(diagram_type, suffix, source)
    metrics = build_metrics_for_diagram(diagram_type, parsed)
    return DiagramAnalysisResponse(
        diagramType=diagram_type,
        sourceType='structured',
        elements=parsed.elements,
        relations=parsed.relations,
        metrics=metrics,
        confidence=ConfidenceSummary(overall=1.0, directlyMeasurable=True),
        issues=[],
    )


def _dispatch_parse(diagram_type: str, suffix: str, source: str) -> ParsedDiagram:
    if suffix in {'puml', 'plantuml'}:
        if diagram_type == 'class':
            return parse_plantuml_class(source)
        if diagram_type == 'usecase':
            return parse_plantuml_usecase(source)
    if suffix in {'mmd', 'mermaid'}:
        if diagram_type == 'flow':
            return parse_mermaid_flow(source)
        if diagram_type == 'class':
            return parse_mermaid_class(source)
    raise HTTPException(status_code=400, detail=f'Unsupported structured diagram input: {diagram_type}/{suffix}')


def build_metrics_for_diagram(diagram_type: str, parsed: ParsedDiagram) -> List[DiagramMetricValue]:
    if diagram_type == 'class':
        return _class_metrics(parsed)
    if diagram_type == 'flow':
        return _flow_metrics(parsed)
    if diagram_type == 'usecase':
        return _usecase_metrics(parsed)
    raise HTTPException(status_code=400, detail=f'Unsupported diagram type: {diagram_type}')


def _class_metrics(parsed: ParsedDiagram) -> List[DiagramMetricValue]:
    relation_counts = Counter(relation.type for relation in parsed.relations)
    class_count = float(len(parsed.elements))
    relation_total = float(len(parsed.relations))
    density_denominator = max(1.0, class_count * (class_count - 1.0))
    relationship_density = relation_total / density_denominator
    object_design_complexity = class_count + relation_total + parsed.metadata.get('attributeCount', 0.0) + parsed.metadata.get('methodCount', 0.0)

    return [
        _metric('classCount', class_count, 'count', 'Detected classes and interfaces'),
        _metric('attributeCount', parsed.metadata.get('attributeCount', 0.0), 'count', 'Detected attributes'),
        _metric('methodCount', parsed.metadata.get('methodCount', 0.0), 'count', 'Detected methods'),
        _metric('inheritanceRelationCount', float(relation_counts.get('inheritance', 0)), 'count', 'Detected inheritance relations'),
        _metric('associationCount', float(relation_counts.get('association', 0)), 'count', 'Detected associations'),
        _metric('dependencyCount', float(relation_counts.get('dependency', 0)), 'count', 'Detected dependencies'),
        _metric('aggregationCount', float(relation_counts.get('aggregation', 0)), 'count', 'Detected aggregations'),
        _metric('compositionCount', float(relation_counts.get('composition', 0)), 'count', 'Detected compositions'),
        _metric('relationshipDensity', relationship_density, 'ratio', 'Relation density across classes'),
        _metric('objectDesignComplexityScore', object_design_complexity, 'score', 'Composite object design complexity score'),
    ]


def _flow_metrics(parsed: ParsedDiagram) -> List[DiagramMetricValue]:
    type_counts = Counter(element.type for element in parsed.elements)
    outgoing: Dict[str, int] = defaultdict(int)
    for relation in parsed.relations:
        outgoing[relation.source] += 1
    max_branch_fan_out = max(outgoing.values()) if outgoing else 0
    decision_count = float(type_counts.get('Decision', 0))
    node_count = float(len(parsed.elements))
    terminal_count = float(type_counts.get('Terminal', 0))
    path_count = float(2 ** int(decision_count)) if decision_count else 1.0
    estimated_complexity = decision_count + 1.0
    control_flow_score = node_count + decision_count + max_branch_fan_out

    return [
        _metric('nodeCount', node_count, 'count', 'Detected flowchart nodes'),
        _metric('decisionNodeCount', decision_count, 'count', 'Detected decision nodes'),
        _metric('terminalNodeCount', terminal_count, 'count', 'Detected terminal nodes'),
        _metric('pathCountEstimate', path_count, 'count', 'Estimated execution paths'),
        _metric('controlFlowComplexityScore', control_flow_score, 'score', 'Composite control-flow complexity score'),
        _metric('estimatedCyclomaticComplexity', estimated_complexity, 'score', 'Estimated cyclomatic complexity from the flow structure'),
        _metric('maxBranchFanOut', float(max_branch_fan_out), 'count', 'Maximum branch fan-out'),
    ]


def _usecase_metrics(parsed: ParsedDiagram) -> List[DiagramMetricValue]:
    type_counts = Counter(element.type for element in parsed.elements)
    relation_counts = Counter(relation.type for relation in parsed.relations)
    association_count = float(relation_counts.get('association', 0))
    include_count = float(relation_counts.get('include', 0))
    extend_count = float(relation_counts.get('extend', 0))
    generalization_count = float(relation_counts.get('generalization', 0))
    use_case_size = float(type_counts.get('UseCase', 0) + include_count + extend_count)
    interaction_complexity = association_count + include_count + extend_count + generalization_count

    return [
        _metric('actorCount', float(type_counts.get('Actor', 0)), 'count', 'Detected actors'),
        _metric('useCaseCount', float(type_counts.get('UseCase', 0)), 'count', 'Detected use cases'),
        _metric('actorUseCaseAssociationCount', association_count, 'count', 'Detected actor-use case associations'),
        _metric('includeRelationCount', include_count, 'count', 'Detected include relations'),
        _metric('extendRelationCount', extend_count, 'count', 'Detected extend relations'),
        _metric('generalizationRelationCount', generalization_count, 'count', 'Detected generalizations'),
        _metric('useCaseSizeScore', use_case_size, 'score', 'Composite use case size score'),
        _metric('interactionComplexityScore', interaction_complexity, 'score', 'Composite interaction complexity score'),
    ]


def _metric(name: str, value: float, unit: str, description: str) -> DiagramMetricValue:
    return DiagramMetricValue(name=name, value=float(value), unit=unit, description=description)
