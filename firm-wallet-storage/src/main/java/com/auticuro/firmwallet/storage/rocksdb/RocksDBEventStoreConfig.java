package com.auticuro.firmwallet.storage.rocksdb;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RocksDBEventStoreConfig {
    @Value("${rocksdb.event.path}")
    private String eventPath;

    @Value("${rocksdb.path}")
    private String rocksdbPath;

}
