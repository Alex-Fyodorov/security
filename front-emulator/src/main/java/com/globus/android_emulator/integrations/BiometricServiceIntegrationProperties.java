package com.globus.android_emulator.integrations;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@ConfigurationProperties(prefix = "integrations.biometric-service")
@Data
public class BiometricServiceIntegrationProperties {
    private String url;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer writeTimeout;
}
