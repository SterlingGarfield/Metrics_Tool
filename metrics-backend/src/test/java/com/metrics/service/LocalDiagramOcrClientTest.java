package com.metrics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LocalDiagramOcrClientTest {

    @Test
    void scanDoesNotFailWithQuotedPaddleOcrHomeEnvironmentKey() throws Exception {
        Path pythonBin = Paths.get("..", ".ocr311", "python.exe").toAbsolutePath().normalize();
        Path modelHome = Paths.get("..", "models", "paddleocr").toAbsolutePath().normalize();
        Path imagePath = Paths.get("..", "metrics-frontend", "src", "assets", "branding", "logo-desktop.png")
            .toAbsolutePath()
            .normalize();

        assumeTrue(Files.exists(pythonBin), "OCR runtime is not available for this workspace");
        assumeTrue(Files.exists(modelHome), "OCR model directory is not available for this workspace");
        assumeTrue(Files.exists(imagePath), "Test image is not available for this workspace");

        String previousPython = System.getProperty("metrics.ocr.python-bin");
        String previousModelHome = System.getProperty("metrics.ocr.model-home");
        System.setProperty("metrics.ocr.python-bin", pythonBin.toString());
        System.setProperty("metrics.ocr.model-home", modelHome.toString());

        try {
            LocalDiagramOcrClient client = new LocalDiagramOcrClient(new ObjectMapper());
            var result = client.scan(imagePath.getFileName().toString(), Files.readAllBytes(imagePath));

            assertThat(result.warnings())
                .noneMatch(warning -> warning.contains("PADDLEOCR_HOME"))
                .noneMatch(warning -> warning.contains("recognizedText"));
        } finally {
            restoreProperty("metrics.ocr.python-bin", previousPython);
            restoreProperty("metrics.ocr.model-home", previousModelHome);
        }
    }

    private void restoreProperty(String key, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previousValue);
        }
    }
}
