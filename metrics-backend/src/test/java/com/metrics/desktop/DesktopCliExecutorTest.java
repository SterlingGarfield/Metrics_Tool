package com.metrics.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DesktopCliExecutorTest {

    @Autowired
    private DesktopCliExecutor executor;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void analyzeTextCommandReturnsStructuredAnalysisJson() throws Exception {
        String request = """
            {
              "command": "analyzeText",
              "fileName": "Sample.java",
              "sourceCode": "public class Sample { void go() {} }"
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/analysis/codeMetrics/projectSummary/totalFiles").asInt()).isEqualTo(1);
        assertThat(response.at("/analysis/codeMetrics/projectSummary/totalClasses").asInt()).isEqualTo(1);
    }

    @Test
    void analyzeDesignCommandReturnsStructuredDesignJson() throws Exception {
        String request = """
            {
              "command": "analyzeDesign",
              "diagramType": "use-case",
              "classCount": 0,
              "relationshipCount": 0,
              "useCaseCount": 3,
              "actorCount": 2,
              "flowNodeCount": 0,
              "imageProvided": true,
              "notes": "Registration flow"
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/analysis/designMetrics/useCaseCount").asInt()).isEqualTo(3);
        assertThat(response.at("/analysis/designMetrics/actorCount").asInt()).isEqualTo(2);
    }

    @Test
    void analyzeEstimationCommandReturnsStructuredEstimationJson() throws Exception {
        String request = """
            {
              "command": "analyzeEstimation",
              "loc": 1200,
              "staffCount": 6,
              "devMonths": 2,
              "cost": 12000.0
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/analysis/estimationMetrics/loc").asInt()).isEqualTo(1200);
        assertThat(response.at("/analysis/estimationMetrics/staffCount").asInt()).isEqualTo(6);
        assertThat(response.at("/analysis/estimationMetrics/workloadPersonMonths").asDouble()).isEqualTo(12.0);
    }

    @Test
    void analyzeUseCasePointsCommandReturnsStructuredEstimationJson() throws Exception {
        String request = """
            {
              "command": "analyzeUseCasePoints",
              "simpleActors": 1,
              "averageActors": 2,
              "complexActors": 1,
              "simpleUseCases": 2,
              "averageUseCases": 1,
              "complexUseCases": 1,
              "technicalComplexityFactor": 1.1,
              "environmentalComplexityFactor": 0.9
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/analysis/estimationMetrics/mode").asText()).isEqualTo("use-case-points");
        assertThat(response.at("/analysis/estimationMetrics/useCasePoints/uaw").asInt()).isEqualTo(8);
        assertThat(response.at("/analysis/estimationMetrics/useCasePoints/uucw").asInt()).isEqualTo(35);
    }

    @Test
    void suggestDesignMetricsFromScanReturnsStructuredSuggestionJson() throws Exception {
        String request = """
            {
              "command": "suggestDesignMetricsFromScan",
              "diagramType": "use-case",
              "ocrAvailable": true,
              "ocrRecognizedText": ["User", "Register Account", "Reset Password"],
              "ocrAverageConfidence": 0.9,
              "ocrWarnings": [],
              "ocrTokens": [
                {
                  "text": "User",
                  "confidence": 0.93,
                  "bbox": [120, 80, 240, 120],
                  "polygon": [[120, 80], [240, 80], [240, 120], [120, 120]]
                },
                {
                  "text": "Register Account",
                  "confidence": 0.95,
                  "bbox": [420, 210, 660, 250],
                  "polygon": [[420, 210], [660, 210], [660, 250], [420, 250]]
                }
              ],
              "ocrImageWidth": 960,
              "ocrImageHeight": 720
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/payload/available").asBoolean()).isTrue();
        assertThat(response.at("/payload/suggestedMetrics/actorCount").asInt()).isEqualTo(1);
        assertThat(response.at("/payload/suggestedMetrics/useCaseCount").asInt()).isEqualTo(2);
    }

    @Test
    void suggestDesignMetricsFromScanUsesTokenLayoutForClassInference() throws Exception {
        String request = """
            {
              "command": "suggestDesignMetricsFromScan",
              "diagramType": "class",
              "ocrAvailable": true,
              "ocrRecognizedText": ["Order", "-id: String", "+submit()", "Customer", "-name: String", "+getName()", "关联"],
              "ocrAverageConfidence": 0.92,
              "ocrWarnings": [],
              "ocrTokens": [
                {"text": "Order", "confidence": 0.99, "bbox": [100, 80, 240, 120], "polygon": [[100, 80], [240, 80], [240, 120], [100, 120]]},
                {"text": "-id: String", "confidence": 0.98, "bbox": [90, 150, 260, 190], "polygon": [[90, 150], [260, 150], [260, 190], [90, 190]]},
                {"text": "+submit()", "confidence": 0.97, "bbox": [96, 210, 230, 245], "polygon": [[96, 210], [230, 210], [230, 245], [96, 245]]},
                {"text": "Customer", "confidence": 0.99, "bbox": [700, 80, 900, 120], "polygon": [[700, 80], [900, 80], [900, 120], [700, 120]]},
                {"text": "-name: String", "confidence": 0.98, "bbox": [690, 150, 930, 190], "polygon": [[690, 150], [930, 150], [930, 190], [690, 190]]},
                {"text": "+getName()", "confidence": 0.97, "bbox": [696, 210, 890, 245], "polygon": [[696, 210], [890, 210], [890, 245], [696, 245]]},
                {"text": "关联", "confidence": 0.96, "bbox": [430, 165, 500, 205], "polygon": [[430, 165], [500, 165], [500, 205], [430, 205]]}
              ],
              "ocrImageWidth": 1024,
              "ocrImageHeight": 768
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isZero();
        assertThat(response.path("status").asText()).isEqualTo("ok");
        assertThat(response.at("/payload/available").asBoolean()).isTrue();
        assertThat(response.at("/payload/suggestedMetrics/classCount").asInt()).isEqualTo(2);
        assertThat(response.at("/payload/suggestedMetrics/relationshipCount").asInt()).isEqualTo(1);
    }

    @Test
    void rejectsUnsupportedCommandsWithAnErrorPayload() throws Exception {
        String request = """
            {
              "command": "unknownCommand"
            }
            """;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int exitCode = executor.execute(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), output);

        var response = objectMapper.readTree(output.toByteArray());
        assertThat(exitCode).isEqualTo(1);
        assertThat(response.path("status").asText()).isEqualTo("error");
        assertThat(response.path("error").asText()).contains("Unsupported desktop command");
    }
}
