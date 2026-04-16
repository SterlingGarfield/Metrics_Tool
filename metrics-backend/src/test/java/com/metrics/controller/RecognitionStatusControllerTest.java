package com.metrics.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.metrics.client.RecognitionServiceClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecognitionStatusController.class)
class RecognitionStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecognitionServiceClient recognitionServiceClient;

    @Test
    void healthProxiesPythonReadinessIntoSimpleResponse() throws Exception {
        when(recognitionServiceClient.health()).thenReturn(Map.of("status", "UP", "service", "diagram-recognition"));
        when(recognitionServiceClient.modelsStatus()).thenReturn(Map.of("ready", true, "modelsLoaded", true));

        mockMvc.perform(get("/api/recognition/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("diagram-recognition"))
            .andExpect(jsonPath("$.ready").value(true))
            .andExpect(jsonPath("$.modelsLoaded").value(true));

        verify(recognitionServiceClient).health();
        verify(recognitionServiceClient).modelsStatus();
    }
}
