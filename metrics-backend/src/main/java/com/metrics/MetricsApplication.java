package com.metrics;

import com.metrics.desktop.DesktopCliExecutor;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MetricsApplication {

    public static void main(String[] args) throws IOException {
        if (isDesktopCliMode(args)) {
            ConfigurableApplicationContext context = new SpringApplicationBuilder(MetricsApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .properties(desktopCliDefaultProperties())
                .run(args);
            int exitCode;
            try {
                exitCode = context.getBean(DesktopCliExecutor.class).execute(System.in, System.out);
            } finally {
                context.close();
            }
            System.exit(exitCode);
        }

        SpringApplication.run(MetricsApplication.class, args);
    }

    private static boolean isDesktopCliMode(String[] args) {
        return Arrays.stream(args)
            .anyMatch(argument -> "--metrics.desktop.mode=cli".equals(argument) || "metrics.desktop.mode=cli".equals(argument));
    }

    static Map<String, Object> desktopCliDefaultProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.main.banner-mode", "off");
        properties.put("spring.main.log-startup-info", "false");
        properties.put("logging.level.root", "OFF");
        return properties;
    }
}
