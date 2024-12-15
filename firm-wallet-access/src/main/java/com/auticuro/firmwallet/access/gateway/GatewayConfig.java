package com.auticuro.firmwallet.access.gateway;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GatewayConfig {
    
    @Value("${gateway.firm-wallet.host:localhost}")
    private String firmWalletHost;
    
    @Value("${gateway.firm-wallet.port:9090}")
    private int firmWalletPort;
    
    @Value("${gateway.timeout:5000}")
    private long timeoutMillis;
    
    @Bean(destroyMethod = "shutdown")
    public ManagedChannel firmWalletChannel() {
        return ManagedChannelBuilder.forAddress(firmWalletHost, firmWalletPort)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();
    }
}
