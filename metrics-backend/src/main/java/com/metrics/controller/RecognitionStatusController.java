package com.metrics.controller;

import com.metrics.client.RecognitionServiceClient;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/recognition")
public class RecognitionStatusController {

    private final RecognitionServiceClient recognitionServiceClient;

    public RecognitionStatusController(RecognitionServiceClient recognitionServiceClient) {
        this.recognitionServiceClient = recognitionServiceClient;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return recognitionServiceClient.health();
    }

    @GetMapping("/models/status")
    public Map<String, Object> modelsStatus() {
        return recognitionServiceClient.modelsStatus();
    }
}
