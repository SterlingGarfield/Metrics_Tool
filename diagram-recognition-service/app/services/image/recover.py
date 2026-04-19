from __future__ import annotations

from dataclasses import dataclass
import math
from typing import Iterable

import cv2
import numpy as np

from app.models.contracts import (
    DiagramAnalysisResponse,
    DiagramElement,
    DiagramRelation,
    ParsedDiagram,
)
from app.services.image.confidence import evaluate_confidence
from app.services.image.detect import DetectedNode, detect_nodes
from app.services.image.metric_extractor import build_image_metrics
from app.services.image.ocr import TextBlock, extract_text_blocks
from app.services.image.preprocess import preprocess_image


def analyze_image_diagram(diagram_type: str, filename: str, payload: bytes) -> DiagramAnalysisResponse:
    preprocessed = preprocess_image(payload)
    nodes = detect_nodes(diagram_type, preprocessed)
    text_blocks, ocr_messages = extract_text_blocks(preprocessed.image)
    _annotate_nodes(diagram_type, nodes, text_blocks)
    relations = _recover_relations(diagram_type, nodes, preprocessed.line_mask, text_blocks)
    parsed = _build_parsed_diagram(diagram_type, nodes, relations)
    metrics = build_image_metrics(diagram_type, parsed)
    confidence, issues = evaluate_confidence(
        diagram_type,
        blur_score=preprocessed.blur_score,
        nodes=nodes,
        relation_count=len(relations),
        text_blocks=text_blocks,
        ocr_messages=ocr_messages,
    )
    return DiagramAnalysisResponse(
        diagramType=diagram_type,
        sourceType="image",
        elements=parsed.elements,
        relations=parsed.relations,
        metrics=metrics,
        confidence=confidence,
        issues=issues,
    )


def _annotate_nodes(diagram_type: str, nodes: list[DetectedNode], text_blocks: list[TextBlock]) -> None:
    for index, node in enumerate(nodes, start=1):
        blocks = _blocks_for_node(diagram_type, node, text_blocks)
        if node.element_type == "Class":
            node.label = _class_label(blocks) or f"Class {index}"
            attribute_count = sum(1 for block in blocks if ":" in block.text and "(" not in block.text)
            method_count = sum(1 for block in blocks if "(" in block.text and ")" in block.text)
            if attribute_count:
                node.attribute_count = attribute_count
            if method_count:
                node.method_count = method_count
        elif node.element_type == "Actor":
            node.label = _join_labels(blocks) or f"Actor {index}"
        elif node.element_type == "UseCase":
            node.label = _join_labels(blocks) or f"Use Case {index}"
        elif node.element_type == "SystemBoundary":
            node.label = _join_labels(blocks) or "System"
        else:
            node.label = _join_labels(blocks) or f"{node.element_type} {index}"


def _blocks_for_node(diagram_type: str, node: DetectedNode, text_blocks: list[TextBlock]) -> list[TextBlock]:
    x, y, w, h = node.bbox
    if node.element_type == "Actor":
        region = (x - 80, y - 10, w + 160, h + 120)
    elif node.element_type == "SystemBoundary":
        region = (x + 20, y + 10, w - 40, min(80, h))
    else:
        region = (x - 8, y - 8, w + 16, h + 16)
    rx, ry, rw, rh = region
    blocks = [
        block
        for block in text_blocks
        if rx <= block.center[0] <= rx + rw and ry <= block.center[1] <= ry + rh
    ]
    return sorted(blocks, key=lambda block: (block.box[1], block.box[0]))


def _recover_relations(
    diagram_type: str,
    nodes: list[DetectedNode],
    line_mask: np.ndarray,
    text_blocks: list[TextBlock],
) -> list[DiagramRelation]:
    connector_mask = _build_connector_mask(line_mask, nodes)
    if diagram_type == "class":
        return _recover_class_relations(nodes, connector_mask, text_blocks)
    if diagram_type == "flow":
        return _recover_flow_relations(nodes, connector_mask)
    if diagram_type == "usecase":
        return _recover_usecase_relations(nodes, connector_mask, text_blocks)
    return []


def _recover_class_relations(
    nodes: list[DetectedNode],
    connector_mask: np.ndarray,
    text_blocks: list[TextBlock],
) -> list[DiagramRelation]:
    class_nodes = [node for node in nodes if node.role == "node"]
    relations: list[DiagramRelation] = []
    for index, source in enumerate(class_nodes):
        for target in class_nodes[index + 1 :]:
            density = _connector_density(connector_mask, source, target)
            if density < 0.03:
                continue
            relation_type = _class_relation_type(source, target, text_blocks)
            relations.append(
                DiagramRelation(
                    source=source.label,
                    target=target.label,
                    type=relation_type,
                    confidence=min(0.96, 0.55 + (density * 2.5)),
                )
            )
    return relations


def _recover_flow_relations(nodes: list[DetectedNode], connector_mask: np.ndarray) -> list[DiagramRelation]:
    flow_nodes = [node for node in nodes if node.role == "node"]
    relations: list[DiagramRelation] = []
    for source in flow_nodes:
        candidates: list[tuple[float, DetectedNode]] = []
        for target in flow_nodes:
            if source is target:
                continue
            if target.center[0] < source.center[0] - 120 and target.center[1] < source.center[1] - 120:
                continue
            density = _connector_density(connector_mask, source, target)
            if density >= 0.035:
                candidates.append((density, target))
        for density, target in sorted(candidates, key=lambda item: item[0], reverse=True)[:2]:
            relation = DiagramRelation(
                source=source.label,
                target=target.label,
                type="sequence",
                confidence=min(0.95, 0.5 + (density * 2.2)),
            )
            if not _relation_exists(relations, relation):
                relations.append(relation)
    return relations


def _recover_usecase_relations(
    nodes: list[DetectedNode],
    connector_mask: np.ndarray,
    text_blocks: list[TextBlock],
) -> list[DiagramRelation]:
    actors = [node for node in nodes if node.element_type == "Actor"]
    usecases = [node for node in nodes if node.element_type == "UseCase"]
    relations: list[DiagramRelation] = []

    for actor in actors:
        for usecase in usecases:
            density = _connector_density(connector_mask, actor, usecase)
            if density < 0.03:
                continue
            relation = DiagramRelation(
                source=actor.label,
                target=usecase.label,
                type="association",
                confidence=min(0.94, 0.5 + (density * 2.1)),
            )
            if not _relation_exists(relations, relation):
                relations.append(relation)

    for source in usecases:
        for target in usecases:
            if source is target:
                continue
            density = _connector_density(connector_mask, source, target)
            if density < 0.03:
                continue
            relation_type = _usecase_relation_type(source, target, text_blocks)
            relation = DiagramRelation(
                source=source.label,
                target=target.label,
                type=relation_type,
                confidence=min(0.94, 0.52 + (density * 2.0)),
            )
            if not _relation_exists(relations, relation):
                relations.append(relation)
    return relations


def _build_parsed_diagram(
    diagram_type: str,
    nodes: list[DetectedNode],
    relations: list[DiagramRelation],
) -> ParsedDiagram:
    elements = [
        DiagramElement(
            type=node.element_type,
            name=node.label,
            stereotype=node.stereotype,
            confidence=node.confidence,
        )
        for node in nodes
        if node.role == "node"
    ]
    metadata: dict[str, float] = {}
    if diagram_type == "class":
        metadata["attributeCount"] = float(sum(node.attribute_count for node in nodes if node.role == "node"))
        metadata["methodCount"] = float(sum(node.method_count for node in nodes if node.role == "node"))
    return ParsedDiagram(elements=elements, relations=relations, metadata=metadata)


def _build_connector_mask(line_mask: np.ndarray, nodes: list[DetectedNode]) -> np.ndarray:
    node_mask = np.zeros_like(line_mask)
    for node in nodes:
        _draw_node_mask(node_mask, node)
    return cv2.bitwise_and(line_mask, cv2.bitwise_not(node_mask))


def _draw_node_mask(mask: np.ndarray, node: DetectedNode) -> None:
    x, y, w, h = node.bbox
    if node.element_type in {"UseCase", "Terminal"}:
        center = (int(x + (w / 2.0)), int(y + (h / 2.0)))
        axes = (max(1, w // 2), max(1, h // 2))
        cv2.ellipse(mask, center, axes, 0, 0, 360, 255, -1)
        return
    if node.element_type == "Decision":
        points = np.asarray(
            [
                (x + (w // 2), y),
                (x + w, y + (h // 2)),
                (x + (w // 2), y + h),
                (x, y + (h // 2)),
            ],
            dtype=np.int32,
        )
        cv2.fillConvexPoly(mask, points, 255)
        return
    cv2.rectangle(mask, (x, y), (x + w, y + h), 255, -1)


def _connector_density(mask: np.ndarray, source: DetectedNode, target: DetectedNode) -> float:
    start = _anchor_point(source, target.center)
    end = _anchor_point(target, source.center)
    canvas = np.zeros_like(mask)
    cv2.line(canvas, start, end, 255, 6)
    band = canvas > 0
    if not np.any(band):
        return 0.0
    return float(np.count_nonzero(mask[band])) / float(np.count_nonzero(band))


def _anchor_point(node: DetectedNode, target_center: tuple[float, float]) -> tuple[int, int]:
    x, y, w, h = node.bbox
    cx, cy = node.center
    dx = target_center[0] - cx
    dy = target_center[1] - cy

    if node.element_type in {"UseCase", "Terminal"}:
        rx = max(1.0, w / 2.0)
        ry = max(1.0, h / 2.0)
        scale = 1.0 / math.sqrt(((dx * dx) / (rx * rx)) + ((dy * dy) / (ry * ry)) + 1e-9)
        return (int(cx + (dx * scale)), int(cy + (dy * scale)))

    if node.element_type == "Decision":
        if abs(dx) > abs(dy):
            return (int(x + w if dx > 0 else x), int(cy))
        return (int(cx), int(y + h if dy > 0 else y))

    if abs(dx) > abs(dy):
        return (int(x + w if dx > 0 else x), int(cy))
    return (int(cx), int(y + h if dy > 0 else y))


def _class_relation_type(source: DetectedNode, target: DetectedNode, text_blocks: list[TextBlock]) -> str:
    label = _relation_label(source, target, text_blocks)
    lowered = label.lower()
    if "dep" in lowered or "依赖" in label:
        return "dependency"
    if "agg" in lowered or "聚合" in label:
        return "aggregation"
    if "comp" in lowered or "组合" in label:
        return "composition"
    if abs(source.center[1] - target.center[1]) > 120:
        return "dependency"
    return "association"


def _usecase_relation_type(source: DetectedNode, target: DetectedNode, text_blocks: list[TextBlock]) -> str:
    label = _relation_label(source, target, text_blocks)
    lowered = label.lower()
    if "include" in lowered:
        return "include"
    if "extend" in lowered:
        return "extend"
    return "include" if source.center[1] <= target.center[1] else "extend"


def _relation_label(source: DetectedNode, target: DetectedNode, text_blocks: list[TextBlock]) -> str:
    midpoint = ((source.center[0] + target.center[0]) / 2.0, (source.center[1] + target.center[1]) / 2.0)
    nearby = [
        block.text
        for block in text_blocks
        if _distance_point_to_segment(block.center, source.center, target.center) <= 55.0
        and _distance(block.center, midpoint) <= 180.0
    ]
    return " ".join(nearby)


def _distance_point_to_segment(
    point: tuple[float, float],
    start: tuple[float, float],
    end: tuple[float, float],
) -> float:
    px, py = point
    sx, sy = start
    ex, ey = end
    line_dx = ex - sx
    line_dy = ey - sy
    if line_dx == 0 and line_dy == 0:
        return _distance(point, start)
    projection = ((px - sx) * line_dx + (py - sy) * line_dy) / float((line_dx * line_dx) + (line_dy * line_dy))
    projection = max(0.0, min(1.0, projection))
    nearest = (sx + (projection * line_dx), sy + (projection * line_dy))
    return _distance(point, nearest)


def _distance(first: tuple[float, float], second: tuple[float, float]) -> float:
    return math.dist(first, second)


def _class_label(blocks: list[TextBlock]) -> str:
    for block in blocks:
        text = block.text.strip()
        if ":" in text or "(" in text:
            continue
        return text
    return _join_labels(blocks)


def _join_labels(blocks: list[TextBlock]) -> str:
    unique = [block.text for block in blocks if block.text]
    return " ".join(unique).strip()


def _relation_exists(relations: list[DiagramRelation], candidate: DiagramRelation) -> bool:
    return any(
        relation.source == candidate.source
        and relation.target == candidate.target
        and relation.type == candidate.type
        for relation in relations
    )
