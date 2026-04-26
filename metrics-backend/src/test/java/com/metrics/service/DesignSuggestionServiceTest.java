package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrics.service.DesignSuggestionService.OcrToken;
import com.metrics.service.DesignSuggestionService.OcrScanResult;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesignSuggestionServiceTest {

    @Test
    void derivesUseCaseSuggestionsFromRecognizedText() {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            true,
            List.of("User", "Register Account", "Reset Password"),
            0.88,
            List.of()
        ));

        var response = service.suggest("use-case", "design.png", "diagram".getBytes());

        assertThat(response.available()).isTrue();
        assertThat(response.recognizedText()).contains("User", "Register Account");
        assertThat(response.suggestedMetrics().actorCount()).isEqualTo(1);
        assertThat(response.suggestedMetrics().useCaseCount()).isEqualTo(2);
        assertThat(response.confidence()).isGreaterThan(0.5);
    }

    @Test
    void returnsWarningsWhenRecognizerIsUnavailable() {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            false,
            List.of(),
            0.0,
            List.of("本地 OCR 运行时或模型不可用。")
        ));

        var response = service.suggest("class", "design.png", "diagram".getBytes());

        assertThat(response.available()).isFalse();
        assertThat(response.warnings()).contains("本地 OCR 运行时或模型不可用。");
        assertThat(response.suggestedMetrics().classCount()).isNull();
    }

    @Test
    void derivesUseCaseSuggestionsFromAnExistingOcrScanResult() {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            false,
            List.of(),
            0.0,
            List.of("unused")
        ));

        var response = service.suggestFromScan("use-case", new OcrScanResult(
            true,
            List.of("Admin", "Approve Request", "View Dashboard"),
            0.9,
            List.of()
        ));

        assertThat(response.available()).isTrue();
        assertThat(response.suggestedMetrics().actorCount()).isEqualTo(1);
        assertThat(response.suggestedMetrics().useCaseCount()).isEqualTo(2);
        assertThat(response.confidence()).isGreaterThan(0.5);
    }

    @Test
    void derivesStrictClassCountsFromGoldenFixtureTokens() throws Exception {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            false,
            List.of(),
            0.0,
            List.of("unused")
        ));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode fixture = readResourceAsJson(objectMapper, "fixtures/design/class-diagram-sample.tokens.json");
        JsonNode expected = readResourceAsJson(objectMapper, "fixtures/design/class-diagram-sample.expected.json");
        OcrScanResult scanResult = buildScanFromFixture(fixture);

        var firstResponse = service.suggestFromScan("class", scanResult);
        var secondResponse = service.suggestFromScan("class", scanResult);

        assertThat(firstResponse.available()).isTrue();
        assertThat(firstResponse.suggestedMetrics().classCount()).isEqualTo(expected.path("classCount").asInt());
        assertThat(firstResponse.suggestedMetrics().relationshipCount()).isEqualTo(expected.path("relationshipCount").asInt());
        assertThat(firstResponse.warnings()).noneMatch(warning -> warning.contains("未能从识别文本中推断稳定的度量建议"));
        assertThat(firstResponse.confidence()).isGreaterThanOrEqualTo(expected.path("minConfidence").asDouble());
        assertThat(secondResponse.suggestedMetrics().classCount()).isEqualTo(firstResponse.suggestedMetrics().classCount());
        assertThat(secondResponse.suggestedMetrics().relationshipCount()).isEqualTo(firstResponse.suggestedMetrics().relationshipCount());
    }

    @Test
    void derivesStrictUseCaseCountsFromTokenFixture() {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            false,
            List.of(),
            0.0,
            List.of("unused")
        ));

        List<OcrToken> tokens = List.of(
            token("User", 0.95, List.of(80.0, 80.0, 180.0, 120.0)),
            token("Register Account", 0.96, List.of(360.0, 140.0, 620.0, 180.0)),
            token("Approve Request", 0.96, List.of(360.0, 240.0, 620.0, 280.0)),
            token("include", 0.92, List.of(260.0, 170.0, 330.0, 205.0)),
            token("extend", 0.92, List.of(260.0, 260.0, 330.0, 295.0))
        );
        List<String> text = tokens.stream().map(OcrToken::text).toList();

        var response = service.suggestFromScan("use-case", new OcrScanResult(
            true,
            text,
            0.9,
            List.of(),
            tokens,
            800,
            600
        ));

        assertThat(response.available()).isTrue();
        assertThat(response.suggestedMetrics().actorCount()).isEqualTo(1);
        assertThat(response.suggestedMetrics().useCaseCount()).isEqualTo(2);
        assertThat(response.suggestedMetrics().relationshipCount()).isEqualTo(2);
    }

    @Test
    void derivesStrictFlowCountsFromTokenFixture() {
        DesignSuggestionService service = new DesignSuggestionService((fileName, imageBytes) -> new OcrScanResult(
            false,
            List.of(),
            0.0,
            List.of("unused")
        ));

        List<OcrToken> tokens = List.of(
            token("Start", 0.95, List.of(120.0, 100.0, 220.0, 140.0)),
            token("Validate Input", 0.95, List.of(320.0, 100.0, 520.0, 140.0)),
            token("Create Order", 0.95, List.of(560.0, 220.0, 760.0, 260.0)),
            token("End", 0.95, List.of(820.0, 220.0, 920.0, 260.0)),
            token("->", 0.9, List.of(250.0, 110.0, 300.0, 140.0)),
            token("->", 0.9, List.of(540.0, 180.0, 580.0, 220.0)),
            token("->", 0.9, List.of(780.0, 230.0, 810.0, 260.0))
        );
        List<String> text = tokens.stream().map(OcrToken::text).toList();

        var response = service.suggestFromScan("flow", new OcrScanResult(
            true,
            text,
            0.9,
            List.of(),
            tokens,
            1024,
            768
        ));

        assertThat(response.available()).isTrue();
        assertThat(response.suggestedMetrics().flowNodeCount()).isEqualTo(4);
        assertThat(response.suggestedMetrics().relationshipCount()).isEqualTo(3);
    }

    private OcrScanResult buildScanFromFixture(JsonNode fixture) {
        List<OcrToken> tokens = new ArrayList<>();
        for (JsonNode tokenNode : fixture.path("tokens")) {
            List<Double> bbox = new ArrayList<>();
            for (JsonNode bboxValue : tokenNode.path("bbox")) {
                bbox.add(bboxValue.asDouble());
            }

            List<List<Double>> polygon = new ArrayList<>();
            for (JsonNode pointNode : tokenNode.path("polygon")) {
                if (pointNode.size() < 2) {
                    continue;
                }
                polygon.add(List.of(pointNode.get(0).asDouble(), pointNode.get(1).asDouble()));
            }

            tokens.add(new OcrToken(
                tokenNode.path("text").asText(),
                tokenNode.path("confidence").asDouble(0.0),
                bbox,
                polygon
            ));
        }

        List<String> recognizedText = tokens.stream().map(OcrToken::text).toList();
        return new OcrScanResult(
            true,
            recognizedText,
            fixture.path("averageConfidence").asDouble(0.0),
            List.of(),
            tokens,
            fixture.path("imageWidth").isNumber() ? fixture.path("imageWidth").asInt() : null,
            fixture.path("imageHeight").isNumber() ? fixture.path("imageHeight").asInt() : null
        );
    }

    private OcrToken token(String text, double confidence, List<Double> bbox) {
        return new OcrToken(
            text,
            confidence,
            bbox,
            List.of(
                List.of(bbox.get(0), bbox.get(1)),
                List.of(bbox.get(2), bbox.get(1)),
                List.of(bbox.get(2), bbox.get(3)),
                List.of(bbox.get(0), bbox.get(3))
            )
        );
    }

    private JsonNode readResourceAsJson(ObjectMapper objectMapper, String resourcePath) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(stream).as("Resource should exist: %s", resourcePath).isNotNull();
            return objectMapper.readTree(stream);
        }
    }
}
