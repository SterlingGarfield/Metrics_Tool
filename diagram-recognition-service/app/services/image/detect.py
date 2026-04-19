from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable

import cv2
import numpy as np

from app.services.image.preprocess import PreprocessResult


@dataclass
class DetectedNode:
    element_type: str
    stereotype: str
    bbox: tuple[int, int, int, int]
    confidence: float
    contour_area: float
    role: str = "node"
    label: str = ""
    attribute_count: int = 0
    method_count: int = 0

    @property
    def center(self) -> tuple[float, float]:
        x, y, w, h = self.bbox
        return (x + (w / 2.0), y + (h / 2.0))


@dataclass(frozen=True)
class _ContourFeature:
    bbox: tuple[int, int, int, int]
    area: float
    fill_ratio: float
    vertices: int


def detect_nodes(diagram_type: str, preprocessed: PreprocessResult) -> list[DetectedNode]:
    features = _collect_features(preprocessed.binary)
    if diagram_type == "class":
        return _detect_class_nodes(features)
    if diagram_type == "flow":
        return _detect_flow_nodes(features)
    if diagram_type == "usecase":
        return _detect_usecase_nodes(features)
    raise ValueError(f"Unsupported diagram type: {diagram_type}")


def _collect_features(binary: np.ndarray) -> list[_ContourFeature]:
    contours, _ = cv2.findContours(binary, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    features: list[_ContourFeature] = []
    for contour in contours:
        area = float(cv2.contourArea(contour))
        if area < 1000.0:
            continue
        x, y, w, h = cv2.boundingRect(contour)
        perimeter = cv2.arcLength(contour, True)
        vertices = len(cv2.approxPolyDP(contour, 0.02 * perimeter, True))
        fill_ratio = area / max(1.0, float(w * h))
        features.append(
            _ContourFeature(
                bbox=(x, y, w, h),
                area=area,
                fill_ratio=float(fill_ratio),
                vertices=vertices,
            )
        )
    return features


def _detect_class_nodes(features: Iterable[_ContourFeature]) -> list[DetectedNode]:
    compartments = [
        feature
        for feature in features
        if feature.vertices == 4
        and feature.fill_ratio >= 0.9
        and feature.bbox[2] >= 180
        and 40 <= feature.bbox[3] <= 110
    ]
    groups: list[list[_ContourFeature]] = []
    for feature in sorted(compartments, key=lambda item: (item.bbox[0], item.bbox[1])):
        x, y, w, _ = feature.bbox
        for group in groups:
            gx, gy, gw, gh = _merge_bbox([item.bbox for item in group])
            vertical_gap = max(gy - (y + feature.bbox[3]), y - (gy + gh), 0)
            if abs(x - gx) <= 30 and abs(w - gw) <= 40 and vertical_gap <= 40:
                group.append(feature)
                break
        else:
            groups.append([feature])

    nodes: list[DetectedNode] = []
    for group in groups:
        if len(group) < 2:
            continue
        bbox = _merge_bbox([item.bbox for item in group])
        nodes.append(
            DetectedNode(
                element_type="Class",
                stereotype="class",
                bbox=bbox,
                confidence=min(0.95, 0.55 + (0.12 * len(group))),
                contour_area=sum(item.area for item in group),
                attribute_count=1 if len(group) >= 2 else 0,
                method_count=1 if len(group) >= 3 else 0,
            )
        )
    return sorted(nodes, key=lambda node: (node.bbox[1], node.bbox[0]))


def _detect_flow_nodes(features: Iterable[_ContourFeature]) -> list[DetectedNode]:
    nodes: list[DetectedNode] = []
    for feature in sorted(features, key=lambda item: item.area, reverse=True):
        x, y, w, h = feature.bbox
        if w > 700 and h > 300:
            continue
        if w < 140 or h < 70:
            continue
        if feature.vertices == 4 and feature.fill_ratio >= 0.9:
            nodes.append(_build_node("Process", "process", feature, 0.9))
        elif feature.vertices == 4 and 0.3 <= feature.fill_ratio <= 0.75:
            nodes.append(_build_node("Decision", "decision", feature, 0.88))
        elif feature.vertices >= 6 and feature.fill_ratio >= 0.63:
            nodes.append(_build_node("Terminal", "terminal", feature, 0.86))
    return _dedupe_nodes(nodes)


def _detect_usecase_nodes(features: Iterable[_ContourFeature]) -> list[DetectedNode]:
    nodes: list[DetectedNode] = []
    for feature in sorted(features, key=lambda item: item.area, reverse=True):
        x, y, w, h = feature.bbox
        if feature.vertices == 4 and feature.fill_ratio >= 0.9 and w >= 600 and h >= 400:
            nodes.append(_build_node("SystemBoundary", "system-boundary", feature, 0.97, role="boundary"))
            continue
        if 50 <= w <= 120 and h >= 130 and feature.fill_ratio <= 0.35:
            nodes.append(_build_node("Actor", "actor", feature, 0.82))
            continue
        if 120 <= w <= 350 and 60 <= h <= 120 and 0.5 <= feature.fill_ratio <= 0.85:
            nodes.append(_build_node("UseCase", "usecase", feature, 0.88))
    return _dedupe_nodes(nodes)


def _build_node(
    element_type: str,
    stereotype: str,
    feature: _ContourFeature,
    confidence: float,
    *,
    role: str = "node",
) -> DetectedNode:
    return DetectedNode(
        element_type=element_type,
        stereotype=stereotype,
        bbox=feature.bbox,
        confidence=confidence,
        contour_area=feature.area,
        role=role,
    )


def _dedupe_nodes(nodes: list[DetectedNode]) -> list[DetectedNode]:
    deduped: list[DetectedNode] = []
    for node in sorted(nodes, key=lambda item: item.contour_area, reverse=True):
        if any(_intersection_over_union(node.bbox, existing.bbox) >= 0.6 for existing in deduped):
            continue
        deduped.append(node)
    return sorted(deduped, key=lambda item: (item.bbox[1], item.bbox[0]))


def _merge_bbox(boxes: list[tuple[int, int, int, int]]) -> tuple[int, int, int, int]:
    min_x = min(box[0] for box in boxes)
    min_y = min(box[1] for box in boxes)
    max_x = max(box[0] + box[2] for box in boxes)
    max_y = max(box[1] + box[3] for box in boxes)
    return (min_x, min_y, max_x - min_x, max_y - min_y)


def _intersection_over_union(
    first: tuple[int, int, int, int],
    second: tuple[int, int, int, int],
) -> float:
    ax1, ay1, aw, ah = first
    bx1, by1, bw, bh = second
    ax2, ay2 = ax1 + aw, ay1 + ah
    bx2, by2 = bx1 + bw, by1 + bh

    intersection_w = max(0, min(ax2, bx2) - max(ax1, bx1))
    intersection_h = max(0, min(ay2, by2) - max(ay1, by1))
    intersection = float(intersection_w * intersection_h)
    if intersection == 0.0:
        return 0.0

    union = float((aw * ah) + (bw * bh) - intersection)
    return intersection / max(union, 1.0)
