from __future__ import annotations

from dataclasses import dataclass
import math

import cv2
import numpy as np


@dataclass(frozen=True)
class PreprocessResult:
    image: np.ndarray
    gray: np.ndarray
    binary: np.ndarray
    line_mask: np.ndarray
    blur_score: float
    skew_angle: float


def preprocess_image(payload: bytes) -> PreprocessResult:
    buffer = np.frombuffer(payload, dtype=np.uint8)
    image = cv2.imdecode(buffer, cv2.IMREAD_COLOR)
    if image is None:
        raise ValueError("Unreadable or unsupported image payload")

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    normalized = cv2.GaussianBlur(gray, (3, 3), 0)
    _, binary = cv2.threshold(normalized, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    skew_angle = _estimate_skew_angle(binary)
    if abs(skew_angle) > 0.5:
        image = _rotate_image(image, skew_angle)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        normalized = cv2.GaussianBlur(gray, (3, 3), 0)
        _, binary = cv2.threshold(normalized, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    line_mask = cv2.morphologyEx(binary, cv2.MORPH_CLOSE, np.ones((3, 3), np.uint8))
    blur_score = float(cv2.Laplacian(gray, cv2.CV_64F).var())
    return PreprocessResult(
        image=image,
        gray=gray,
        binary=binary,
        line_mask=line_mask,
        blur_score=blur_score,
        skew_angle=float(skew_angle),
    )


def _estimate_skew_angle(binary: np.ndarray) -> float:
    edges = cv2.Canny(binary, 50, 150)
    lines = cv2.HoughLinesP(
        edges,
        rho=1,
        theta=np.pi / 180.0,
        threshold=120,
        minLineLength=max(80, binary.shape[1] // 6),
        maxLineGap=20,
    )
    if lines is None:
        return 0.0

    angles: list[float] = []
    for line in lines[:, 0]:
        x1, y1, x2, y2 = line.tolist()
        angle = math.degrees(math.atan2(y2 - y1, x2 - x1))
        if -20.0 < angle < 20.0:
            angles.append(angle)
    if not angles:
        return 0.0
    return float(np.median(np.asarray(angles)))


def _rotate_image(image: np.ndarray, angle: float) -> np.ndarray:
    height, width = image.shape[:2]
    center = (width / 2.0, height / 2.0)
    matrix = cv2.getRotationMatrix2D(center, angle, 1.0)
    return cv2.warpAffine(
        image,
        matrix,
        (width, height),
        flags=cv2.INTER_LINEAR,
        borderMode=cv2.BORDER_CONSTANT,
        borderValue=(255, 255, 255),
    )
