package com.metrics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrics.service.DesignSuggestionService.DiagramOcrClient;
import com.metrics.service.DesignSuggestionService.OcrScanResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class LocalDiagramOcrClient implements DiagramOcrClient {

    private static final Duration OCR_TIMEOUT = Duration.ofSeconds(90);

    private static final String OCR_SCRIPT = """
        import json
        import os
        import sys

        try:
            import cv2
            from paddleocr import PaddleOCR

            image_path = sys.argv[1]
            model_home = sys.argv[2]
            if model_home:
                os.environ['PADDLEOCR_HOME'] = model_home

            image_width = None
            image_height = None
            image = cv2.imread(image_path)
            if image is not None:
                image_height, image_width = image.shape[:2]

            ocr = PaddleOCR(use_angle_cls=True, lang='ch', show_log=False)
            result = ocr.ocr(image_path, cls=True)
            lines = []
            scores = []
            tokens = []
            for page in result or []:
                for item in page or []:
                    if not item or len(item) < 2:
                        continue
                    text = str(item[1][0]).strip()
                    score = float(item[1][1]) if len(item[1]) > 1 else 0.0
                    if text:
                        polygon = []
                        for point in item[0] or []:
                            if not point or len(point) < 2:
                                continue
                            polygon.append([float(point[0]), float(point[1])])
                        if polygon:
                            xs = [point[0] for point in polygon]
                            ys = [point[1] for point in polygon]
                            bbox = [min(xs), min(ys), max(xs), max(ys)]
                        else:
                            bbox = []
                        lines.append(text)
                        scores.append(score)
                        tokens.append({
                            'text': text,
                            'confidence': score,
                            'bbox': bbox,
                            'polygon': polygon
                        })

            average_confidence = sum(scores) / len(scores) if scores else 0.0
            print(json.dumps({
                'recognizedText': lines,
                'averageConfidence': average_confidence,
                'tokens': tokens,
                'imageWidth': image_width,
                'imageHeight': image_height
            }, ensure_ascii=False))
        except Exception as exc:
            print(json.dumps({
                'recognizedText': [],
                'averageConfidence': 0.0,
                'tokens': [],
                'imageWidth': None,
                'imageHeight': None,
                'error': str(exc)
            }, ensure_ascii=False))
        """;

    private final ObjectMapper objectMapper;

    public LocalDiagramOcrClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public OcrScanResult scan(String fileName, byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return unavailable("未提供可识别的设计图图片。");
        }

        Optional<OcrRuntimePaths> runtimePaths = resolveRuntimePaths();
        if (runtimePaths.isEmpty()) {
            return unavailable("本地 OCR 运行时或模型不可用。");
        }

        Path tempImage = null;
        try {
            tempImage = Files.createTempFile("metrics-design-", resolveExtension(fileName));
            Files.write(tempImage, imageBytes);

            ProcessBuilder processBuilder = new ProcessBuilder(
                runtimePaths.get().pythonBin().toString(),
                "-c",
                OCR_SCRIPT,
                tempImage.toString(),
                runtimePaths.get().modelHome().toString()
            );
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("PADDLEOCR_HOME", runtimePaths.get().modelHome().toString());

            Process process = processBuilder.start();
            boolean completed = process.waitFor(OCR_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return unavailable("OCR 识别超时，请改用手工录入。");
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();

            if (stdout.isBlank()) {
                return unavailable(stderr.isBlank() ? "OCR 未返回可解析结果。" : stderr);
            }

            JsonNode payload = objectMapper.readTree(stdout);
            List<String> recognizedText = new ArrayList<>();
            for (JsonNode line : payload.path("recognizedText")) {
                String value = line.asText("").trim();
                if (!value.isEmpty()) {
                    recognizedText.add(value);
                }
            }

            List<String> warnings = new ArrayList<>();
            String error = payload.path("error").asText("");
            if (!error.isBlank()) {
                warnings.add(error);
            }
            if (!stderr.isBlank()) {
                warnings.add(stderr);
            }

            List<DesignSuggestionService.OcrToken> tokens = parseTokens(payload.path("tokens"));

            return new OcrScanResult(
                !recognizedText.isEmpty(),
                List.copyOf(new LinkedHashSet<>(recognizedText)),
                payload.path("averageConfidence").asDouble(0.0),
                List.copyOf(new LinkedHashSet<>(warnings)),
                tokens,
                payload.path("imageWidth").isNumber() ? payload.path("imageWidth").asInt() : null,
                payload.path("imageHeight").isNumber() ? payload.path("imageHeight").asInt() : null
            );
        } catch (Exception exception) {
            return unavailable(exception.getMessage() == null ? "OCR 执行失败。" : exception.getMessage());
        } finally {
            if (tempImage != null) {
                try {
                    Files.deleteIfExists(tempImage);
                } catch (IOException ignored) {
                    // Ignore temporary file cleanup failures.
                }
            }
        }
    }

    private Optional<OcrRuntimePaths> resolveRuntimePaths() {
        Optional<OcrRuntimePaths> configuredPaths = resolveConfiguredRuntimePaths();
        if (configuredPaths.isPresent()) {
            return configuredPaths;
        }

        for (Path candidateRoot : candidateRoots()) {
            Path pythonBin = candidateRoot.resolve(".ocr311").resolve("python.exe");
            Path modelHome = candidateRoot.resolve("models").resolve("paddleocr");
            if (Files.exists(pythonBin) && Files.exists(modelHome)) {
                return Optional.of(new OcrRuntimePaths(pythonBin, modelHome));
            }
        }

        return Optional.empty();
    }

    private Optional<OcrRuntimePaths> resolveConfiguredRuntimePaths() {
        String pythonOverride = firstConfiguredValue("METRICS_OCR_PYTHON_BIN", "metrics.ocr.python-bin");
        String modelOverride = firstConfiguredValue("METRICS_OCR_MODEL_HOME", "metrics.ocr.model-home");

        if (pythonOverride == null || modelOverride == null) {
            return Optional.empty();
        }

        Path pythonBin = Paths.get(pythonOverride).toAbsolutePath();
        Path modelHome = Paths.get(modelOverride).toAbsolutePath();
        if (Files.exists(pythonBin) && Files.exists(modelHome)) {
            return Optional.of(new OcrRuntimePaths(pythonBin, modelHome));
        }

        return Optional.empty();
    }

    private List<Path> candidateRoots() {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        collectAncestors(Paths.get("").toAbsolutePath(), roots);

        try {
            CodeSource codeSource = LocalDiagramOcrClient.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                Path codeSourcePath = Paths.get(codeSource.getLocation().toURI()).toAbsolutePath();
                collectAncestors(Files.isDirectory(codeSourcePath) ? codeSourcePath : codeSourcePath.getParent(), roots);
            }
        } catch (Exception ignored) {
            // Ignore code-source resolution errors and rely on the working directory search.
        }

        return List.copyOf(roots);
    }

    private void collectAncestors(Path start, LinkedHashSet<Path> roots) {
        Path current = start;
        int depth = 0;
        while (current != null && depth < 6) {
            roots.add(current);
            current = current.getParent();
            depth++;
        }
    }

    private String resolveExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".png";
        }

        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private OcrScanResult unavailable(String warning) {
        return new OcrScanResult(false, List.of(), 0.0, List.of(warning));
    }

    private List<DesignSuggestionService.OcrToken> parseTokens(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }

        List<DesignSuggestionService.OcrToken> tokens = new ArrayList<>();
        for (JsonNode tokenNode : node) {
            String text = tokenNode.path("text").asText("").trim();
            if (text.isEmpty()) {
                continue;
            }

            List<Double> bbox = new ArrayList<>();
            JsonNode bboxNode = tokenNode.path("bbox");
            if (bboxNode.isArray()) {
                for (JsonNode valueNode : bboxNode) {
                    if (valueNode.isNumber()) {
                        bbox.add(valueNode.asDouble());
                    }
                }
            }

            List<List<Double>> polygon = new ArrayList<>();
            JsonNode polygonNode = tokenNode.path("polygon");
            if (polygonNode.isArray()) {
                for (JsonNode pointNode : polygonNode) {
                    if (!pointNode.isArray() || pointNode.size() < 2) {
                        continue;
                    }
                    if (pointNode.get(0).isNumber() && pointNode.get(1).isNumber()) {
                        polygon.add(List.of(pointNode.get(0).asDouble(), pointNode.get(1).asDouble()));
                    }
                }
            }

            tokens.add(new DesignSuggestionService.OcrToken(
                text,
                tokenNode.path("confidence").asDouble(0.0),
                bbox,
                polygon
            ));
        }

        return List.copyOf(tokens);
    }

    private String firstConfiguredValue(String environmentKey, String propertyKey) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        return null;
    }

    private record OcrRuntimePaths(Path pythonBin, Path modelHome) {}
}
