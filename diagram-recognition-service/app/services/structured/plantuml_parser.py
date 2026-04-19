import re
from typing import Dict, List, Tuple
from app.models.contracts import DiagramElement, DiagramRelation, ParsedDiagram

CLASS_DECLARATION = re.compile(r'^(abstract\s+class|class|interface)\s+([A-Za-z_][\w]*)')
ACTOR_DECLARATION = re.compile(r'^actor\s+(?:"([^"]+)"|([A-Za-z_][\w]*))(?:\s+as\s+([A-Za-z_][\w]*))?$')
USECASE_DECLARATION = re.compile(r'^usecase\s+(?:"([^"]+)"|([A-Za-z_][\w]*))(?:\s+as\s+([A-Za-z_][\w]*))?$')


def parse_plantuml_class(source: str) -> ParsedDiagram:
    elements: Dict[str, DiagramElement] = {}
    relations: List[DiagramRelation] = []
    attribute_count = 0.0
    method_count = 0.0
    current_type = None

    for raw_line in source.splitlines():
        line = raw_line.strip()
        if not line or line.startswith('@'):
            continue

        declaration = CLASS_DECLARATION.match(line)
        if declaration:
            keyword, name = declaration.groups()
            if keyword == 'interface':
                element_type = 'Interface'
                stereotype = 'interface'
            elif keyword == 'abstract class':
                element_type = 'AbstractClass'
                stereotype = 'abstract'
            else:
                element_type = 'Class'
                stereotype = 'class'
            elements[name] = DiagramElement(type=element_type, name=name, stereotype=stereotype)
            current_type = name if line.endswith('{') else None
            continue

        if current_type:
            if line.startswith('}'):
                current_type = None
                continue
            if '(' in line and ')' in line:
                method_count += 1
            elif ':' in line:
                attribute_count += 1
            continue

        relation = _parse_class_relation(line)
        if relation is not None:
            relations.append(relation)

    return ParsedDiagram(
        elements=list(elements.values()),
        relations=relations,
        metadata={
            'attributeCount': attribute_count,
            'methodCount': method_count,
        },
    )


def parse_plantuml_usecase(source: str) -> ParsedDiagram:
    elements: Dict[str, DiagramElement] = {}
    aliases: Dict[str, str] = {}
    relations: List[DiagramRelation] = []

    for raw_line in source.splitlines():
        line = raw_line.strip()
        if not line or line.startswith('@') or line.startswith('left to right'):
            continue

        actor_match = ACTOR_DECLARATION.match(line)
        if actor_match:
            display_name = actor_match.group(1) or actor_match.group(2)
            alias = actor_match.group(3) or display_name
            aliases[alias] = display_name
            elements[display_name] = DiagramElement(type='Actor', name=display_name, stereotype='actor')
            continue

        usecase_match = USECASE_DECLARATION.match(line)
        if usecase_match:
            display_name = usecase_match.group(1) or usecase_match.group(2)
            alias = usecase_match.group(3) or display_name
            aliases[alias] = display_name
            elements[display_name] = DiagramElement(type='UseCase', name=display_name, stereotype='usecase')
            continue

        relation = _parse_usecase_relation(line, aliases)
        if relation is not None:
            relations.append(relation)

    return ParsedDiagram(elements=list(elements.values()), relations=relations, metadata={})


def _parse_class_relation(line: str):
    relation_text = line.split(':', 1)[0].strip()
    patterns = [
        (r'^(?P<left>[A-Za-z_][\w]*)\s+<\|--\s+(?P<right>[A-Za-z_][\w]*)$', 'inheritance', True),
        (r'^(?P<left>[A-Za-z_][\w]*)\s+--\|>\s+(?P<right>[A-Za-z_][\w]*)$', 'inheritance', False),
        (r'^(?P<left>[A-Za-z_][\w]*)\s+\.\.>\s+(?P<right>[A-Za-z_][\w]*)$', 'dependency', False),
        (r'^(?P<left>[A-Za-z_][\w]*)\s+o--\s+(?P<right>[A-Za-z_][\w]*)$', 'aggregation', False),
        (r'^(?P<left>[A-Za-z_][\w]*)\s+\*--\s+(?P<right>[A-Za-z_][\w]*)$', 'composition', False),
        (r'^(?P<left>[A-Za-z_][\w]*)\s+-->\s+(?P<right>[A-Za-z_][\w]*)$', 'association', False),
    ]
    for pattern, relation_type, reverse in patterns:
        match = re.match(pattern, relation_text)
        if match:
            left = match.group('left')
            right = match.group('right')
            source, target = (right, left) if reverse else (left, right)
            return DiagramRelation(source=source, target=target, type=relation_type)
    return None


def _parse_usecase_relation(line: str, aliases: Dict[str, str]):
    relation_text, _, tail = line.partition(':')
    relation_text = relation_text.strip()
    relation_label = tail.strip().lower()

    for arrow, relation_type in (('..>', 'include' if '<<include>>' in relation_label else 'extend' if '<<extend>>' in relation_label else 'dependency'), ('-->', 'association')):
        if arrow in relation_text:
            left, right = [part.strip() for part in relation_text.split(arrow, 1)]
            return DiagramRelation(
                source=aliases.get(left, left),
                target=aliases.get(right, right),
                type=relation_type,
            )
    if '<|--' in relation_text:
        left, right = [part.strip() for part in relation_text.split('<|--', 1)]
        return DiagramRelation(source=aliases.get(right, right), target=aliases.get(left, left), type='generalization')
    return None
