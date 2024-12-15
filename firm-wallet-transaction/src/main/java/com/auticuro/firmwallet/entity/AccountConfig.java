package com.auticuro.firmwallet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountConfig {
    private String assetClass;
    private BalanceLimit balanceLimit;
    
    @Data
    public static class BalanceLimit {
        private String minAvailable;
        private String maxAvailable;
        private String maxReserved;
    }
}
