import re
from typing import Dict, List, Tuple
from app.models.contracts import DiagramElement, DiagramRelation, ParsedDiagram

CLASS_RELATION_PATTERNS = [
    (re.compile(r'^(?P<left>[A-Za-z_][\w]*)\s+<\|--\s+(?P<right>[A-Za-z_][\w]*)$'), 'inheritance', True),
    (re.compile(r'^(?P<left>[A-Za-z_][\w]*)\s+\.\.>\s+(?P<right>[A-Za-z_][\w]*)$'), 'dependency', False),
    (re.compile(r'^(?P<left>[A-Za-z_][\w]*)\s+-->\s+(?P<right>[A-Za-z_][\w]*)$'), 'association', False),
]


def parse_mermaid_class(source: str) -> ParsedDiagram:
    elements: Dict[str, DiagramElement] = {}
    relations: List[DiagramRelation] = []
    in_block = None
    attribute_count = 0.0
    method_count = 0.0

    for raw_line in source.splitlines():
        line = raw_line.strip()
        if not line or line.startswith('classDiagram'):
            continue
        if line.startswith('class '):
            name = line.split()[1]
            elements[name] = DiagramElement(type='Class', name=name, stereotype='class')
            in_block = name if line.endswith('{') else None
            continue
        if in_block:
            if line.startswith('}'):
                in_block = None
                continue
            if '(' in line and ')' in line:
                method_count += 1
            elif ':' in line:
                attribute_count += 1
            continue
        for pattern, relation_type, reverse in CLASS_RELATION_PATTERNS:
            match = pattern.match(line)
            if match:
                left = match.group('left')
                right = match.group('right')
                source, target = (right, left) if reverse else (left, right)
                relations.append(DiagramRelation(source=source, target=target, type=relation_type))
                break

    return ParsedDiagram(
        elements=list(elements.values()),
        relations=relations,
        metadata={'attributeCount': attribute_count, 'methodCount': method_count},
    )


def parse_mermaid_flow(source: str) -> ParsedDiagram:
    nodes: Dict[str, DiagramElement] = {}
    relations: List[DiagramRelation] = []

    for raw_line in source.splitlines():
        line = raw_line.strip()
        if not line or line.startswith('flowchart') or line.startswith('graph '):
            continue
        if '-->' not in line:
            continue
        left_token, right_token = _split_edge(line)
        left_id, left_element = _parse_node(left_token)
        right_id, right_element = _parse_node(right_token)
        nodes[left_id] = nodes.get(left_id, left_element)
        nodes[right_id] = nodes.get(right_id, right_element)
        relations.append(DiagramRelation(source=nodes[left_id].name, target=nodes[right_id].name, type='sequence'))

    return ParsedDiagram(elements=list(nodes.values()), relations=relations, metadata={})


def _split_edge(line: str) -> Tuple[str, str]:
    left, right = line.split('-->', 1)
    right = re.sub(r'\|[^|]+\|', '', right)
    return left.strip(), right.strip()


def _parse_node(token: str) -> Tuple[str, DiagramElement]:
    match = re.match(r'^(?P<identifier>[A-Za-z_][\w]*)(?P<body>.*)$', token)
    identifier = match.group('identifier')
    body = match.group('body').strip()

    if body.startswith('([') and body.endswith('])'):
        label = body[2:-2]
        element_type = 'Terminal'
        stereotype = 'terminal'
    elif body.startswith('{') and body.endswith('}'):
        label = body[1:-1]
        element_type = 'Decision'
        stereotype = 'decision'
    elif body.startswith('[') and body.endswith(']'):
        label = body[1:-1]
        element_type = 'Process'
        stereotype = 'process'
    else:
        label = identifier
        element_type = 'Process'
        stereotype = 'process'

    return identifier, DiagramElement(type=element_type, name=label, stereotype=stereotype)
