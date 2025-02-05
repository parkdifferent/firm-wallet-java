package com.auticuro.firmwallet.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.auticuro.firmwallet")
public class FirmWalletAccessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirmWalletAccessApplication.class, args);
    }
}
