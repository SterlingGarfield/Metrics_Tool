package com.metrics.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(RecognitionServiceProperties.class)
public class RestClientConfig {

    @Bean
    public RestTemplate recognitionRestTemplate(RestTemplateBuilder builder, RecognitionServiceProperties properties) {
        return builder
            .rootUri(properties.getBaseUrl())
            .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
            .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
            .build();
    }
}
