package com.metrics.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metrics.model.SourceInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class SourceNormalizationUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SourceInput> normalizeFiles(List<MultipartFile> files) throws IOException {
        List<SourceInput> inputs = new ArrayList<>();
        for (MultipartFile file : files) {
            inputs.add(new SourceInput(file.getOriginalFilename(), new String(file.getBytes(), StandardCharsets.UTF_8)));
        }
        return inputs;
    }

    public List<SourceInput> normalizeFolder(List<MultipartFile> files, String relativePathsJson) throws IOException {
        List<String> relativePaths = objectMapper.readValue(relativePathsJson, new TypeReference<List<String>>() {});
        List<SourceInput> inputs = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String relativePath = index < relativePaths.size() ? relativePaths.get(index) : file.getOriginalFilename();
            inputs.add(new SourceInput(relativePath, new String(file.getBytes(), StandardCharsets.UTF_8)));
        }
        return inputs;
    }
}
