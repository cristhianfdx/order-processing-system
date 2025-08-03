package com.cristhianfdx.orderworker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "external.api")
public class ExternalAPIProperties {
    private String customer;
    private String product;
}
