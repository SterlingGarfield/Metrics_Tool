package com.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MetricsApplicationTest {

    @Test
    void desktopCliDefaultsDisableConsoleLoggingNoise() {
        Map<String, Object> properties = MetricsApplication.desktopCliDefaultProperties();

        assertThat(properties)
            .containsEntry("spring.main.banner-mode", "off")
            .containsEntry("spring.main.log-startup-info", "false")
            .containsEntry("logging.level.root", "OFF");
    }
}
