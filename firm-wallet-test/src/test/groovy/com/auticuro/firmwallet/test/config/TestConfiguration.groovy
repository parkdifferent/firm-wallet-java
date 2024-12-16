package com.auticuro.firmwallet.test.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.SpringBootConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Import

@SpringBootConfiguration
@ComponentScan(
    basePackages = [
        "com.auticuro.firmwallet.access",
        "com.auticuro.firmwallet.transaction",
        "com.auticuro.firmwallet.query",
        "com.auticuro.firmwallet.storage",
        "com.auticuro.firmwallet.common",
        "com.auticuro.firmwallet.test.config"]
)
@PropertySource("classpath:application-test.yml")
@ActiveProfiles("test")
@Import([TestMockConfiguration.class, TestMyBatisConfiguration.class])
class TestConfiguration {
    // Configuration moved to TestMockConfiguration
}
