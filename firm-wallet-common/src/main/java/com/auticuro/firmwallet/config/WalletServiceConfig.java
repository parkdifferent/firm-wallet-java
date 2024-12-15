package com.auticuro.firmwallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wallet.service")
public class WalletServiceConfig {
    private String serviceId;
    private String serviceType;
    private String storageEndpoint;
    private int maxRetries;
    private long retryIntervalMs;
    private EventLogGC eventLogGC = new EventLogGC();

    @Data
    public static class EventLogGC {
        private boolean enabled = true;
        private long countLimit = 1000000;
        private long batchSize = 1000;
        private long pollIntervalMillis = 1000;
    }
}
