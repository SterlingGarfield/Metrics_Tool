package com.metrics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metrics.client.RecognitionServiceClient;
import com.metrics.exception.RecognitionServiceException;
import com.metrics.model.request.StructuredDiagramAnalyzeRequest;
import com.metrics.model.response.ConfidenceSummary;
import com.metrics.model.response.DiagramAnalysisResponse;
import com.metrics.model.response.DiagramElement;
import com.metrics.model.response.DiagramMetricValue;
import com.metrics.model.response.DiagramRelation;
import com.metrics.model.response.RecognitionIssue;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DesignAnalysisController.class)
class DesignAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecognitionServiceClient recognitionServiceClient;

    @Test
    void analyzeStructuredRejectsMissingDiagramType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "library-domain.puml",
            MediaType.TEXT_PLAIN_VALUE,
            "@startuml\nclass Demo\n@enduml".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/structured")
                .file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeStructuredRejectsMissingFile() throws Exception {
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "class".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/structured")
                .file(diagramType))
            .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeStructuredRejectsInvalidDiagramType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "library-domain.puml",
            MediaType.TEXT_PLAIN_VALUE,
            "@startuml\nclass Demo\n@enduml".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "sequence".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/structured")
                .file(file)
                .file(diagramType))
            .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeStructuredReturnsReservedContractFieldsAndDiagramPayload() throws Exception {
        DiagramAnalysisResponse response = new DiagramAnalysisResponse(
            "class",
            "structured",
            List.of(new DiagramElement("Class", "Book", "entity", 0.98)),
            List.of(new DiagramRelation("Book", "Author", "association", 0.95)),
            List.of(new DiagramMetricValue("classCount", 2.0, "count", "Detected classes")),
            new ConfidenceSummary(0.94, true),
            List.of(new RecognitionIssue("warning", "LOW_CONFIDENCE_LABEL", "Label confidence is low"))
        );
        when(recognitionServiceClient.analyzeStructured(any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "library-domain.puml",
            MediaType.TEXT_PLAIN_VALUE,
            "@startuml\nclass Book\nclass Author\nBook --> Author\n@enduml".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "class".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/structured")
                .file(file)
                .file(diagramType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codeMetrics").hasJsonPath())
            .andExpect(jsonPath("$.diagramAnalysis").hasJsonPath())
            .andExpect(jsonPath("$.projectEstimation").hasJsonPath())
            .andExpect(jsonPath("$.riskFindings").isArray())
            .andExpect(jsonPath("$.diagramAnalysis.diagramType").value("class"))
            .andExpect(jsonPath("$.diagramAnalysis.sourceType").value("structured"))
            .andExpect(jsonPath("$.diagramAnalysis.metrics[0].name").value("classCount"))
            .andExpect(jsonPath("$.diagramAnalysis.confidence.overall").value(0.94))
            .andExpect(jsonPath("$.diagramAnalysis.issues[0].level").value("warning"));

        ArgumentCaptor<StructuredDiagramAnalyzeRequest> requestCaptor = ArgumentCaptor.forClass(StructuredDiagramAnalyzeRequest.class);
        verify(recognitionServiceClient).analyzeStructured(requestCaptor.capture());
        StructuredDiagramAnalyzeRequest forwarded = requestCaptor.getValue();
        Assertions.assertEquals("class", forwarded.diagramType().value());
        Assertions.assertEquals("library-domain.puml", forwarded.fileName());
        Assertions.assertEquals("puml", forwarded.sourceSuffix());
        Assertions.assertTrue(forwarded.source().contains("class Book"));
    }

    @Test
    void analyzeImageRejectsMissingFile() throws Exception {
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "class".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/image")
                .file(diagramType)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeImageRejectsInvalidDiagramType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "flow.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-png-data".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "Class".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/image")
                .file(file)
                .file(diagramType))
            .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeImageReturnsDiagramPayload() throws Exception {
        DiagramAnalysisResponse response = new DiagramAnalysisResponse(
            "flow",
            "image",
            List.of(new DiagramElement("Decision", "Approved?", "diamond", 0.89)),
            List.of(new DiagramRelation("Review", "Approved?", "sequence", 0.86)),
            List.of(new DiagramMetricValue("decisionNodeCount", 1.0, "count", "Detected decisions")),
            new ConfidenceSummary(0.88, false),
            List.of(new RecognitionIssue("warning", "LOW_CONFIDENCE_OCR", "OCR confidence below threshold"))
        );
        when(recognitionServiceClient.analyzeImage(any(), any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "flow.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-png-data".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "flow".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/image")
                .file(file)
                .file(diagramType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.diagramAnalysis.diagramType").value("flow"))
            .andExpect(jsonPath("$.diagramAnalysis.sourceType").value("image"))
            .andExpect(jsonPath("$.diagramAnalysis.metrics[0].name").value("decisionNodeCount"))
            .andExpect(jsonPath("$.diagramAnalysis.confidence.directlyMeasurable").value(false))
            .andExpect(jsonPath("$.diagramAnalysis.issues[0].code").value("LOW_CONFIDENCE_OCR"));
    }

    @Test
    void analyzeImageReturnsServiceUnavailableWhenRecognitionFails() throws Exception {
        when(recognitionServiceClient.analyzeImage(any(), any()))
            .thenThrow(new RecognitionServiceException("Recognition service unavailable"));

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "flow.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-png-data".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile diagramType = new MockMultipartFile(
            "diagramType",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "flow".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/design/analyze/image")
                .file(file)
                .file(diagramType))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.service").value("diagram-recognition"))
            .andExpect(jsonPath("$.status").value("DOWN"));
    }
}
