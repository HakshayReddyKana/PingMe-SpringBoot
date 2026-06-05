package com.hakshay.chat.config;

import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class MetricsConfig {

    @Value("${management.otlp.metrics.export.url}")
    private String url;

    @Value("${management.otlp.metrics.export.headers.Authorization}")
    private String authHeader;

    @Bean
    public OtlpMeterRegistry otlpMeterRegistry() {
        OtlpConfig config = new OtlpConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String url() {
                return url;
            }

            @Override
            public Map<String, String> headers() {
                return Map.of("Authorization", authHeader);
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }
        };

        return new OtlpMeterRegistry(config, io.micrometer.core.instrument.Clock.SYSTEM);
    }
}
