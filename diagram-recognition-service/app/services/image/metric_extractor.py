from __future__ import annotations

from app.models.contracts import DiagramMetricValue, ParsedDiagram
from app.services.structured.metric_extractor import build_metrics_for_diagram


def build_image_metrics(diagram_type: str, parsed: ParsedDiagram) -> list[DiagramMetricValue]:
    return build_metrics_for_diagram(diagram_type, parsed)
