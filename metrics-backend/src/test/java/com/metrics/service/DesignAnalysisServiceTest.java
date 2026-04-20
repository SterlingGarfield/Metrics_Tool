package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.exception.RecognitionServiceException;
import com.metrics.model.DiagramType;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.AnalysisResponse;
import com.metrics.model.response.ConfidenceSummary;
import com.metrics.model.response.DiagramAnalysisResponse;
import com.metrics.model.response.DiagramMetricValue;
import com.metrics.model.response.RecognitionIssue;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DesignAnalysisServiceTest {

    @Mock
    private RecognitionServiceClient recognitionServiceClient;

    @InjectMocks
    private DesignAnalysisService designAnalysisService;

    @Test
    void analyzeStructuredWrapsRecognitionPayload() {
        StructuredDiagramAnalyzeRequest request = new StructuredDiagramAnalyzeRequest(
            DiagramType.CLASS,
            "library-domain.puml",
            "puml",
            "@startuml\nclass Book\n@enduml"
        );
        DiagramAnalysisResponse diagramAnalysis = new DiagramAnalysisResponse(
            "class",
            "structured",
            List.of(),
            List.of(),
            List.of(new DiagramMetricValue("classCount", 1.0, "count", "Detected classes")),
            new ConfidenceSummary(0.99, true),
            List.of(new RecognitionIssue("info", "PARSE_OK", "Structured parsing succeeded"))
        );
        when(recognitionServiceClient.analyzeStructured(request)).thenReturn(diagramAnalysis);

        AnalysisResponse response = designAnalysisService.analyzeStructured(request);

        assertThat(response.diagramAnalysis()).isEqualTo(diagramAnalysis);
        assertThat(response.riskFindings()).isEmpty();
        verify(recognitionServiceClient).analyzeStructured(request);
    }

    @Test
    void analyzeStructuredPropagatesRecognitionFailures() {
        StructuredDiagramAnalyzeRequest request = new StructuredDiagramAnalyzeRequest(
            DiagramType.CLASS,
            "library-domain.puml",
            "puml",
            "@startuml\nclass Book\n@enduml"
        );
        when(recognitionServiceClient.analyzeStructured(request))
            .thenThrow(new RecognitionServiceException("Recognition service unavailable"));

        assertThatThrownBy(() -> designAnalysisService.analyzeStructured(request))
            .isInstanceOf(RecognitionServiceException.class)
            .hasMessage("Recognition service unavailable");
    }

    @Test
    void analyzeImageWrapsRecognitionPayload() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "flow.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-png-data".getBytes(StandardCharsets.UTF_8)
        );
        DiagramAnalysisResponse diagramAnalysis = new DiagramAnalysisResponse(
            "flow",
            "image",
            List.of(),
            List.of(),
            List.of(new DiagramMetricValue("nodeCount", 12.0, "count", "Detected flow nodes")),
            new ConfidenceSummary(0.81, true),
            List.of()
        );
        when(recognitionServiceClient.analyzeImage("flow", file)).thenReturn(diagramAnalysis);

        AnalysisResponse response = designAnalysisService.analyzeImage("flow", file);

        assertThat(response.diagramAnalysis()).isEqualTo(diagramAnalysis);
        assertThat(response.riskFindings()).isEmpty();
        verify(recognitionServiceClient).analyzeImage("flow", file);
    }
}
