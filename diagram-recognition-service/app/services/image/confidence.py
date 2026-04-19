from __future__ import annotations

from statistics import mean

from app.models.contracts import ConfidenceSummary, RecognitionIssue
from app.services.image.detect import DetectedNode
from app.services.image.ocr import TextBlock

_EXPECTED_ELEMENT_COUNTS = {
    "class": 3,
    "flow": 5,
    "usecase": 5,
}
_EXPECTED_RELATION_COUNTS = {
    "class": 2,
    "flow": 5,
    "usecase": 5,
}


def evaluate_confidence(
    diagram_type: str,
    *,
    blur_score: float,
    nodes: list[DetectedNode],
    relation_count: int,
    text_blocks: list[TextBlock],
    ocr_messages: list[str],
) -> tuple[ConfidenceSummary, list[RecognitionIssue]]:
    expected_nodes = _EXPECTED_ELEMENT_COUNTS.get(diagram_type, 1)
    expected_relations = _EXPECTED_RELATION_COUNTS.get(diagram_type, 1)

    shape_score = min(1.0, len([node for node in nodes if node.role == "node"]) / float(expected_nodes))
    relation_score = min(1.0, relation_count / float(expected_relations))
    ocr_score = _ocr_score(text_blocks, ocr_messages)
    clarity_score = min(1.0, blur_score / 1200.0)

    overall = round((0.35 * shape_score) + (0.25 * relation_score) + (0.20 * ocr_score) + (0.20 * clarity_score), 3)
    directly_measurable = (
        overall >= 0.6
        and shape_score >= 0.75
        and relation_score >= 0.5
        and clarity_score >= 0.2
    )

    issues: list[RecognitionIssue] = []
    if blur_score < 80.0:
        issues.append(
            RecognitionIssue(
                level="warning",
                code="low_image_clarity",
                message="The image appears blurred, so the recovered graph may be incomplete.",
            )
        )
    if ocr_messages:
        issues.append(
            RecognitionIssue(
                level="warning",
                code="ocr_unavailable",
                message=ocr_messages[0],
            )
        )
    if shape_score < 1.0:
        issues.append(
            RecognitionIssue(
                level="warning",
                code="partial_element_recovery",
                message="Some diagram elements may not have been fully recovered from the image.",
            )
        )
    if relation_score < 1.0:
        issues.append(
            RecognitionIssue(
                level="warning",
                code="partial_relation_recovery",
                message="Some relations may be missing or weakly connected in the recovered graph.",
            )
        )

    return ConfidenceSummary(overall=overall, directlyMeasurable=directly_measurable), issues


def _ocr_score(text_blocks: list[TextBlock], ocr_messages: list[str]) -> float:
    if text_blocks:
        return float(mean(block.confidence for block in text_blocks))
    if ocr_messages:
        return 0.35
    return 0.55
