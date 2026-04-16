package com.metrics.controller;

import com.metrics.client.RecognitionServiceClient;
import java.util.LinkedHashMap;
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
        Map<String, Object> health = recognitionServiceClient.health();
        Map<String, Object> modelsStatus = recognitionServiceClient.modelsStatus();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", health.getOrDefault("status", "DOWN"));
        response.put("service", health.getOrDefault("service", "diagram-recognition"));
        response.put("ready", modelsStatus.getOrDefault("ready", false));
        response.put("modelsLoaded", modelsStatus.getOrDefault("modelsLoaded", false));
        return response;
    }
}
