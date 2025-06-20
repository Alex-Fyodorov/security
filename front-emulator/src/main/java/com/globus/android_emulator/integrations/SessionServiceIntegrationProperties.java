package com.globus.android_emulator.integrations;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@ConfigurationProperties(prefix = "integrations.session-service")
@Data
public class SessionServiceIntegrationProperties {
    private String url;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer writeTimeout;
}
