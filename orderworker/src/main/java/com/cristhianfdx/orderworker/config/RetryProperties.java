package com.cristhianfdx.orderworker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.retry")
public class RetryProperties {
    private int maxAttempts;
    private long initialIntervalMs;
    private double multiplier;
}
