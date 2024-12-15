package com.auticuro.firmwallet.query;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.auticuro.firmwallet"})
@MapperScan("com.auticuro.firmwallet.query.repository")
public class FirmWalletQueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirmWalletQueryApplication.class, args);
    }
}
