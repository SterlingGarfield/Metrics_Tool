package com.metrics.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsControllerMultipartTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzeFolderContinuesWhenOneFileHasParseProblems() throws Exception {
        MockMultipartFile good = new MockMultipartFile(
            "files",
            "demo/BaseAccount.java",
            MediaType.TEXT_PLAIN_VALUE,
            "public class BaseAccount { void deposit() {} }".getBytes()
        );
        MockMultipartFile bad = new MockMultipartFile(
            "files",
            "demo/Broken.java",
            MediaType.TEXT_PLAIN_VALUE,
            "public class Broken { void oops( }".getBytes()
        );
        MockMultipartFile paths = new MockMultipartFile(
            "relativePaths",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            "[\"demo/BaseAccount.java\",\"demo/Broken.java\"]".getBytes()
        );

        mockMvc.perform(multipart("/api/metrics/analyze/folder").file(good).file(bad).file(paths))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codeMetrics.projectSummary.totalFiles").value(2))
            .andExpect(jsonPath("$.parseIssues.length()").value(1));
    }
}
