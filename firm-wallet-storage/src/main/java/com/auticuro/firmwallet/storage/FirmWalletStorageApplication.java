package com.auticuro.firmwallet.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
/*@ComponentScan({
    "com.auticuro.firmwallet.common",
    "com.auticuro.firmwallet.storage.rocksdb",
    "com.auticuro.firmwallet.storage.raft",
    "com.auticuro.firmwallet.storage.auticuro"
})*/
@ComponentScan("com.auticuro.firmwallet")
@MapperScan("com.auticuro.firmwallet.repository")
public class FirmWalletStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirmWalletStorageApplication.class, args);
    }
}
