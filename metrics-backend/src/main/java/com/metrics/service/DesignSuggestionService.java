package com.metrics.service;

import com.metrics.model.response.DesignSuggestionResponse;
import com.metrics.model.response.SuggestedDesignMetrics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class DesignSuggestionService {

    private static final double MIN_AVERAGE_CONFIDENCE = 0.45;
    private static final int MIN_LAYOUT_TOKEN_COUNT = 3;
    private static final int MIN_RECOGNIZED_LINE_COUNT = 3;
    private static final double RELATION_HINT_MAX_DISTANCE_PX = 1800.0;
    private static final double CLASS_MEMBER_MAX_VERTICAL_GAP_PX = 720.0;

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("([A-Z][A-Za-z0-9_]{1,40})");
    private static final Pattern ENGLISH_IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{1,40}$");

    private static final Set<String> ACTOR_KEYWORDS = Set.of(
        "user", "admin", "teacher", "student", "customer", "manager",
        "operator", "client", "guest", "member", "用户", "管理员", "教师",
        "老师", "学生", "顾客", "客户", "访客", "会员"
    );

    private static final Set<String> RELATIONSHIP_KEYWORDS = Set.of(
        "关联", "依赖", "继承", "实现", "聚合", "组合", "拥有", "属于",
        "负责", "支付", "预约", "就诊", "就诊于", "生成", "连接", "流向",
        "include", "includes", "extend", "extends", "depend", "depends",
        "inherit", "inherits", "associate", "associated", "belongs",
        "contain", "contains"
    );

    private final DiagramOcrClient diagramOcrClient;

    public DesignSuggestionService(DiagramOcrClient diagramOcrClient) {
        this.diagramOcrClient = diagramOcrClient;
    }

    public DesignSuggestionResponse suggest(String diagramType, String fileName, byte[] imageBytes) {
        return suggestFromScan(diagramType, diagramOcrClient.scan(fileName, imageBytes));
    }

    public DesignSuggestionResponse suggestFromScan(String diagramType, OcrScanResult scanResult) {
        String normalizedType = normalizeDiagramType(diagramType);
        OcrScanResult normalizedScanResult = normalizeScanResult(scanResult);
        List<String> recognizedText = normalizeRecognizedText(normalizedScanResult.recognizedText());
        List<LayoutToken> layoutTokens = normalizeLayoutTokens(normalizedScanResult.tokens());
        if (recognizedText.isEmpty() && !layoutTokens.isEmpty()) {
            recognizedText = layoutTokens.stream().map(LayoutToken::text).toList();
        }

        SuggestedDesignMetrics suggestedMetrics = inferMetrics(normalizedType, recognizedText, layoutTokens);
        List<String> warnings = new ArrayList<>(normalizedScanResult.warnings() == null ? List.of() : normalizedScanResult.warnings());
        int populatedMetricCount = populatedMetricCount(suggestedMetrics);

        if (!normalizedScanResult.available()) {
            if (warnings.isEmpty()) {
                warnings.add("本地 OCR 运行时或模型不可用。");
            }

            return new DesignSuggestionResponse(
                false,
                normalizedType,
                recognizedText,
                suggestedMetrics,
                0.0,
                dedupeWarnings(warnings)
            );
        }

        QualityGateResult qualityGateResult = evaluateQualityGate(normalizedScanResult, recognizedText, layoutTokens);
        if (!qualityGateResult.accepted()) {
            warnings.addAll(qualityGateResult.warnings());
            warnings.add("OCR 识别质量不足，已暂停自动建议，请补全后提交。");
            return new DesignSuggestionResponse(
                false,
                normalizedType,
                recognizedText,
                suggestedMetrics,
                0.0,
                dedupeWarnings(warnings)
            );
        }

        if (recognizedText.isEmpty()) {
            warnings.add("未能识别出清晰文本，请改用手工录入。");
        }

        if (populatedMetricCount == 0) {
            warnings.add("未能从识别文本中推断稳定的度量建议，请改用手工录入。");
        } else {
            warnings.add("以下建议基于 OCR 文本与布局线索联合推断，请在提交前确认。");
        }

        return new DesignSuggestionResponse(
            true,
            normalizedType,
            recognizedText,
            suggestedMetrics,
            calculateConfidence(
                normalizedScanResult.averageConfidence(),
                populatedMetricCount,
                recognizedText.size(),
                layoutTokens.size()
            ),
            dedupeWarnings(warnings)
        );
    }

    private OcrScanResult normalizeScanResult(OcrScanResult scanResult) {
        if (scanResult == null) {
            return new OcrScanResult(false, List.of(), 0.0, List.of("本地 OCR 运行时或模型不可用。"));
        }

        return new OcrScanResult(
            scanResult.available(),
            scanResult.recognizedText() == null ? List.of() : List.copyOf(scanResult.recognizedText()),
            scanResult.averageConfidence(),
            scanResult.warnings() == null ? List.of() : List.copyOf(scanResult.warnings()),
            scanResult.tokens() == null ? List.of() : List.copyOf(scanResult.tokens()),
            scanResult.imageWidth(),
            scanResult.imageHeight()
        );
    }

    private List<String> normalizeRecognizedText(List<String> rawLines) {
        if (rawLines == null || rawLines.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> lines = new LinkedHashSet<>();
        for (String rawLine : rawLines) {
            if (rawLine == null) {
                continue;
            }

            String normalized = rawLine.trim().replaceAll("\\s+", " ");
            if (!normalized.isEmpty()) {
                lines.add(normalized);
            }
        }

        return List.copyOf(lines);
    }

    private List<LayoutToken> normalizeLayoutTokens(List<OcrToken> rawTokens) {
        if (rawTokens == null || rawTokens.isEmpty()) {
            return List.of();
        }

        List<LayoutToken> normalized = new ArrayList<>();
        for (OcrToken rawToken : rawTokens) {
            if (rawToken == null || rawToken.text() == null) {
                continue;
            }

            String text = rawToken.text().trim().replaceAll("\\s+", " ");
            if (text.isEmpty()) {
                continue;
            }

            BoundingBox bbox = normalizeBoundingBox(rawToken);
            if (bbox == null) {
                continue;
            }

            double confidence = clamp(rawToken.confidence(), 0.0, 1.0);
            normalized.add(new LayoutToken(
                text,
                normalizeToken(text),
                confidence,
                bbox,
                (bbox.minX() + bbox.maxX()) / 2.0,
                (bbox.minY() + bbox.maxY()) / 2.0,
                Math.max(0.0, bbox.maxX() - bbox.minX()),
                Math.max(0.0, bbox.maxY() - bbox.minY())
            ));
        }

        normalized.sort(Comparator.comparingDouble(LayoutToken::centerY).thenComparingDouble(LayoutToken::centerX));
        return List.copyOf(normalized);
    }

    private BoundingBox normalizeBoundingBox(OcrToken token) {
        List<Double> bbox = token.bbox();
        if (bbox != null && bbox.size() >= 4) {
            Double x0 = bbox.get(0);
            Double y0 = bbox.get(1);
            Double x1 = bbox.get(2);
            Double y1 = bbox.get(3);
            if (isFinite(x0) && isFinite(y0) && isFinite(x1) && isFinite(y1)) {
                return new BoundingBox(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
            }
        }

        List<List<Double>> polygon = token.polygon();
        if (polygon == null || polygon.isEmpty()) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        boolean hasPoint = false;
        for (List<Double> point : polygon) {
            if (point == null || point.size() < 2) {
                continue;
            }
            Double x = point.get(0);
            Double y = point.get(1);
            if (!isFinite(x) || !isFinite(y)) {
                continue;
            }
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            hasPoint = true;
        }

        if (!hasPoint) {
            return null;
        }
        return new BoundingBox(minX, minY, maxX, maxY);
    }

    private boolean isFinite(Double value) {
        return value != null && Double.isFinite(value);
    }

    private SuggestedDesignMetrics inferMetrics(String diagramType, List<String> recognizedText, List<LayoutToken> tokens) {
        return switch (diagramType) {
            case "use-case", "usecase" -> inferUseCaseMetrics(recognizedText, tokens);
            case "flow", "flowchart" -> inferFlowMetrics(recognizedText, tokens);
            default -> inferClassMetrics(recognizedText, tokens);
        };
    }

    private SuggestedDesignMetrics inferUseCaseMetrics(List<String> recognizedText, List<LayoutToken> tokens) {
        LinkedHashSet<String> actorNames = new LinkedHashSet<>();
        LinkedHashSet<String> useCaseNames = new LinkedHashSet<>();

        for (String line : collectTextsForInference(recognizedText, tokens)) {
            String normalizedLine = line.trim();
            if (normalizedLine.isEmpty()) {
                continue;
            }

            if (looksLikeActor(normalizedLine)) {
                actorNames.add(normalizeToken(normalizedLine));
                continue;
            }

            if (looksLikeUseCase(normalizedLine)
                && !looksLikeRelationshipLabel(normalizedLine)
                && !looksLikeMultiplicityToken(normalizedLine)) {
                useCaseNames.add(normalizedLine.toLowerCase(Locale.ROOT));
            }
        }

        int relationshipCount = inferRelationshipCount(recognizedText, tokens, List.of());

        return new SuggestedDesignMetrics(
            null,
            nullIfZero(relationshipCount),
            nullIfZero(useCaseNames.size()),
            nullIfZero(actorNames.size()),
            null
        );
    }

    private SuggestedDesignMetrics inferFlowMetrics(List<String> recognizedText, List<LayoutToken> tokens) {
        LinkedHashSet<String> flowNodes = new LinkedHashSet<>();
        for (String line : collectTextsForInference(recognizedText, tokens)) {
            String normalizedLine = line.trim();
            if (normalizedLine.isEmpty()) {
                continue;
            }

            if (looksLikeFlowNode(normalizedLine)) {
                flowNodes.add(normalizedLine.toLowerCase(Locale.ROOT));
            }
        }

        int relationshipCount = inferRelationshipCount(recognizedText, tokens, List.of());
        return new SuggestedDesignMetrics(
            null,
            nullIfZero(relationshipCount),
            null,
            null,
            nullIfZero(flowNodes.size())
        );
    }

    private SuggestedDesignMetrics inferClassMetrics(List<String> recognizedText, List<LayoutToken> tokens) {
        List<ClassEntity> classEntities = inferClassEntities(tokens, recognizedText);
        int classCount = classEntities.size();
        int relationshipCount = inferRelationshipCount(recognizedText, tokens, classEntities);

        return new SuggestedDesignMetrics(
            nullIfZero(classCount),
            nullIfZero(relationshipCount),
            null,
            null,
            null
        );
    }

    private List<String> collectTextsForInference(List<String> recognizedText, List<LayoutToken> tokens) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (LayoutToken token : tokens) {
            if (!token.text().isBlank()) {
                merged.add(token.text());
            }
        }
        merged.addAll(recognizedText);
        return List.copyOf(merged);
    }

    private boolean looksLikeActor(String line) {
        String normalized = normalizeToken(line);
        if (ACTOR_KEYWORDS.contains(normalized)) {
            return true;
        }

        return ACTOR_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private boolean looksLikeUseCase(String line) {
        String normalized = line.trim();
        if (normalized.length() < 3 || looksLikeActor(normalized)) {
            return false;
        }

        if (normalized.startsWith("+") || normalized.startsWith("-") || normalized.contains(":")) {
            return false;
        }

        return normalized.contains(" ")
            || normalized.matches(".*(登录|注册|查询|创建|删除|审批|提交|查看|修改|重置).*")
            || normalized.matches(".*[a-zA-Z]+\\s+[a-zA-Z]+.*");
    }

    private boolean looksLikeFlowNode(String line) {
        if (line.length() < 2) {
            return false;
        }

        if (looksLikeRelationshipLabel(line) || looksLikeMultiplicityToken(line) || looksLikeActor(line)) {
            return false;
        }

        return !line.startsWith("+")
            && !line.startsWith("-")
            && !line.contains(":");
    }

    private List<ClassEntity> inferClassEntities(List<LayoutToken> tokens, List<String> recognizedText) {
        if (tokens.isEmpty()) {
            return inferClassEntitiesFromText(recognizedText);
        }

        Map<String, ScoredClassEntity> classEntityMap = new java.util.LinkedHashMap<>();
        for (LayoutToken token : tokens) {
            String className = normalizeClassNameCandidate(token.text());
            if (className == null || !looksLikeClassTitleToken(token, className)) {
                continue;
            }

            List<LayoutToken> nearbyMembers = findNearbyClassMembers(token, tokens);
            if (nearbyMembers.size() < 2) {
                continue;
            }

            BoundingBox mergedBounds = mergeBounds(token, nearbyMembers);
            ClassEntity entity = new ClassEntity(className, normalizeToken(className), mergedBounds);
            ScoredClassEntity existing = classEntityMap.get(entity.normalizedName());
            if (existing == null || nearbyMembers.size() > existing.supportScore()) {
                classEntityMap.put(entity.normalizedName(), new ScoredClassEntity(entity, nearbyMembers.size()));
            }
        }

        if (classEntityMap.isEmpty()) {
            return inferClassEntitiesFromText(recognizedText);
        }

        return classEntityMap.values().stream()
            .map(ScoredClassEntity::entity)
            .sorted(Comparator.comparingDouble(entity -> entity.bounds().minY()))
            .toList();
    }

    private List<ClassEntity> inferClassEntitiesFromText(List<String> recognizedText) {
        LinkedHashSet<String> classNames = new LinkedHashSet<>();
        for (String line : recognizedText) {
            String className = normalizeClassNameCandidate(line);
            if (className == null) {
                continue;
            }
            if (ENGLISH_IDENTIFIER_PATTERN.matcher(className).matches()) {
                classNames.add(className);
            }
        }

        return classNames.stream()
            .map(name -> new ClassEntity(name, normalizeToken(name), new BoundingBox(0, 0, 0, 0)))
            .toList();
    }

    private boolean looksLikeClassTitleToken(LayoutToken token, String className) {
        String text = token.text();
        if (text.startsWith("+") || text.startsWith("-") || text.contains(":")) {
            return false;
        }
        if (looksLikeRelationshipLabel(text) || looksLikeMultiplicityToken(text)) {
            return false;
        }
        if (className.length() < 2) {
            return false;
        }

        return ENGLISH_IDENTIFIER_PATTERN.matcher(className).matches()
            || className.matches("^[\\p{IsHan}][\\p{IsHan}\\p{Alnum}_]{1,20}$");
    }

    private List<LayoutToken> findNearbyClassMembers(LayoutToken title, List<LayoutToken> tokens) {
        List<LayoutToken> members = new ArrayList<>();
        double maxHorizontalDistance = Math.max(220.0, title.width() * 1.4);

        for (LayoutToken token : tokens) {
            if (token == title) {
                continue;
            }
            if (token.centerY() <= title.centerY() + 6) {
                continue;
            }
            if (token.centerY() - title.centerY() > CLASS_MEMBER_MAX_VERTICAL_GAP_PX) {
                continue;
            }
            if (Math.abs(token.centerX() - title.centerX()) > maxHorizontalDistance) {
                continue;
            }
            if (!looksLikeClassMemberToken(token.text())) {
                continue;
            }
            members.add(token);
        }
        return members;
    }

    private boolean looksLikeClassMemberToken(String text) {
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            return false;
        }

        return normalized.startsWith("-")
            || normalized.startsWith("+")
            || normalized.contains(":")
            || normalized.endsWith("()");
    }

    private String normalizeClassNameCandidate(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim().replaceAll("[\\p{Punct}&&[^_]]+$", "").replaceAll("^[\\p{Punct}&&[^_]]+", "");
        if (normalized.isBlank()) {
            return null;
        }

        Matcher matcher = CLASS_NAME_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1);
        }

        if (normalized.matches("^[\\p{IsHan}][\\p{IsHan}\\p{Alnum}_]{1,20}$")) {
            return normalized;
        }
        return null;
    }

    private BoundingBox mergeBounds(LayoutToken title, List<LayoutToken> members) {
        double minX = title.bounds().minX();
        double minY = title.bounds().minY();
        double maxX = title.bounds().maxX();
        double maxY = title.bounds().maxY();
        for (LayoutToken token : members) {
            minX = Math.min(minX, token.bounds().minX());
            minY = Math.min(minY, token.bounds().minY());
            maxX = Math.max(maxX, token.bounds().maxX());
            maxY = Math.max(maxY, token.bounds().maxY());
        }
        return new BoundingBox(minX, minY, maxX, maxY);
    }

    private int inferRelationshipCount(List<String> recognizedText, List<LayoutToken> tokens, List<ClassEntity> classEntities) {
        List<RelationHint> labelHints = new ArrayList<>();
        List<RelationHint> multiplicityHints = new ArrayList<>();
        for (LayoutToken token : tokens) {
            if (looksLikeRelationshipLabel(token.text())) {
                labelHints.add(new RelationHint(token, HintType.RELATION_LABEL));
            } else if (looksLikeMultiplicityToken(token.text())) {
                multiplicityHints.add(new RelationHint(token, HintType.MULTIPLICITY));
            }
        }

        int dedupedLabelCount = dedupeHintCount(labelHints);
        Set<String> pairSignals = new LinkedHashSet<>();
        if (!classEntities.isEmpty()) {
            pairSignals.addAll(pairHintsToClassEntities(classEntities, labelHints));
            pairSignals.addAll(pairHintsToClassEntities(classEntities, multiplicityHints));
        }

        int fallbackTextCount = detectRelationshipCountFromText(recognizedText);
        return Math.max(Math.max(dedupedLabelCount, pairSignals.size()), fallbackTextCount);
    }

    private int dedupeHintCount(List<RelationHint> hints) {
        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        for (RelationHint hint : hints) {
            int gridX = (int) Math.round(hint.token().centerX() / 56.0);
            int gridY = (int) Math.round(hint.token().centerY() / 56.0);
            deduped.add(hint.token().normalizedText() + "@" + gridX + ":" + gridY);
        }
        return deduped.size();
    }

    private Set<String> pairHintsToClassEntities(List<ClassEntity> classEntities, List<RelationHint> hints) {
        LinkedHashSet<String> pairs = new LinkedHashSet<>();
        for (RelationHint hint : hints) {
            List<ClassDistance> distances = classEntities.stream()
                .map(entity -> new ClassDistance(entity, distanceToBounds(hint.token().centerX(), hint.token().centerY(), entity.bounds())))
                .sorted(Comparator.comparingDouble(ClassDistance::distance))
                .toList();
            if (distances.size() < 2) {
                continue;
            }

            ClassDistance first = distances.get(0);
            ClassDistance second = distances.get(1);
            if (second.distance() > RELATION_HINT_MAX_DISTANCE_PX) {
                continue;
            }

            pairs.add(pairKey(first.entity().normalizedName(), second.entity().normalizedName()));
        }
        return pairs;
    }

    private String pairKey(String left, String right) {
        if (left.compareTo(right) <= 0) {
            return left + "|" + right;
        }
        return right + "|" + left;
    }

    private double distanceToBounds(double x, double y, BoundingBox bounds) {
        double dx = 0.0;
        if (x < bounds.minX()) {
            dx = bounds.minX() - x;
        } else if (x > bounds.maxX()) {
            dx = x - bounds.maxX();
        }

        double dy = 0.0;
        if (y < bounds.minY()) {
            dy = bounds.minY() - y;
        } else if (y > bounds.maxY()) {
            dy = y - bounds.maxY();
        }

        return Math.hypot(dx, dy);
    }

    private boolean looksLikeRelationshipLabel(String text) {
        String normalized = normalizeToken(text);
        if (normalized.isEmpty()) {
            return false;
        }

        for (String keyword : RELATIONSHIP_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }

        return normalized.contains("->")
            || normalized.contains("--")
            || normalized.contains("<|")
            || normalized.contains("<>")
            || normalized.contains("=>");
    }

    private boolean looksLikeMultiplicityToken(String text) {
        String normalized = text == null ? "" : text.trim().replace('O', '0').replace('o', '0').replaceAll("\\s+", "");
        if (normalized.isEmpty()) {
            return false;
        }

        return normalized.matches("^(\\*|1|0\\.\\.1|1\\.\\.\\*|1\\.\\.[0-9]+|[0-9]+\\.\\.\\*|[0-9]+)$")
            || normalized.matches("^[1*]{1,3}$")
            || normalized.matches("^0\\.\\.$");
    }

    private int detectRelationshipCountFromText(List<String> recognizedText) {
        int count = 0;
        for (String line : recognizedText) {
            count += countOccurrences(line, "->");
            count += countOccurrences(line, "--");
            count += countOccurrences(line, "<|");
            count += countOccurrences(line, "<>");
            for (String keyword : RELATIONSHIP_KEYWORDS) {
                count += countOccurrences(line.toLowerCase(Locale.ROOT), keyword);
            }
        }
        return count;
    }

    private int countOccurrences(String source, String token) {
        if (source == null || source.isEmpty() || token == null || token.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private QualityGateResult evaluateQualityGate(
        OcrScanResult scanResult,
        List<String> recognizedText,
        List<LayoutToken> tokens
    ) {
        List<String> warnings = new ArrayList<>();
        if (scanResult.averageConfidence() < MIN_AVERAGE_CONFIDENCE) {
            warnings.add(String.format(
                Locale.ROOT,
                "OCR 平均置信度 %.2f 低于阈值 %.2f。",
                scanResult.averageConfidence(),
                MIN_AVERAGE_CONFIDENCE
            ));
        }

        if (tokens.size() < MIN_LAYOUT_TOKEN_COUNT && recognizedText.size() < MIN_RECOGNIZED_LINE_COUNT) {
            warnings.add(String.format(
                Locale.ROOT,
                "OCR 文本密度不足：token=%d, textLines=%d。",
                tokens.size(),
                recognizedText.size()
            ));
        }

        return new QualityGateResult(warnings.isEmpty(), warnings);
    }

    private String normalizeDiagramType(String diagramType) {
        if (diagramType == null || diagramType.isBlank()) {
            return "class";
        }

        return diagramType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeToken(String line) {
        return line.trim().toLowerCase(Locale.ROOT);
    }

    private int populatedMetricCount(SuggestedDesignMetrics metrics) {
        int count = 0;
        if (metrics.classCount() != null) {
            count++;
        }
        if (metrics.relationshipCount() != null) {
            count++;
        }
        if (metrics.useCaseCount() != null) {
            count++;
        }
        if (metrics.actorCount() != null) {
            count++;
        }
        if (metrics.flowNodeCount() != null) {
            count++;
        }
        return count;
    }

    private double calculateConfidence(double averageConfidence, int populatedMetricCount, int textLineCount, int tokenCount) {
        double boundedConfidence = Math.max(0.0, Math.min(averageConfidence, 1.0));
        double confidence = boundedConfidence * 0.65;
        confidence += Math.min(populatedMetricCount, 3) * 0.08;
        confidence += Math.min(tokenCount, 60) / 60.0 * 0.14;
        if (textLineCount > 0) {
            confidence += 0.03;
        }
        return Math.max(0.0, Math.min(confidence, 0.96));
    }

    private List<String> dedupeWarnings(List<String> warnings) {
        return List.copyOf(new LinkedHashSet<>(warnings));
    }

    private Integer nullIfZero(int value) {
        return value > 0 ? value : null;
    }

    private double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    @FunctionalInterface
    public interface DiagramOcrClient {
        OcrScanResult scan(String fileName, byte[] imageBytes);
    }

    public record OcrScanResult(
        boolean available,
        List<String> recognizedText,
        double averageConfidence,
        List<String> warnings,
        List<OcrToken> tokens,
        Integer imageWidth,
        Integer imageHeight
    ) {
        public OcrScanResult {
            recognizedText = recognizedText == null ? List.of() : List.copyOf(recognizedText);
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            tokens = tokens == null ? List.of() : List.copyOf(tokens);
        }

        public OcrScanResult(
            boolean available,
            List<String> recognizedText,
            double averageConfidence,
            List<String> warnings
        ) {
            this(available, recognizedText, averageConfidence, warnings, List.of(), null, null);
        }
    }

    public record OcrToken(
        String text,
        double confidence,
        List<Double> bbox,
        List<List<Double>> polygon
    ) {
        public OcrToken {
            bbox = bbox == null ? List.of() : List.copyOf(bbox);
            polygon = polygon == null ? List.of() : List.copyOf(polygon);
        }
    }

    private record BoundingBox(
        double minX,
        double minY,
        double maxX,
        double maxY
    ) {}

    private record LayoutToken(
        String text,
        String normalizedText,
        double confidence,
        BoundingBox bounds,
        double centerX,
        double centerY,
        double width,
        double height
    ) {}

    private enum HintType {
        RELATION_LABEL,
        MULTIPLICITY
    }

    private record RelationHint(
        LayoutToken token,
        HintType type
    ) {}

    private record ClassEntity(
        String name,
        String normalizedName,
        BoundingBox bounds
    ) {}

    private record ScoredClassEntity(
        ClassEntity entity,
        int supportScore
    ) {}

    private record ClassDistance(
        ClassEntity entity,
        double distance
    ) {}

    private record QualityGateResult(
        boolean accepted,
        List<String> warnings
    ) {}
}
