package com.auticuro.firmwallet.common.command;

import lombok.Data;
import java.math.BigDecimal;

public class WalletCommand {
    
    @Data
    public static class CreateAccount {
        private String accountId;
        private String accountType;
        private String currency;
        private BigDecimal upperLimit;
        private BigDecimal lowerLimit;
    }
    
    @Data
    public static class Transfer {
        private String sourceAccountId;
        private String targetAccountId;
        private BigDecimal amount;
        private String currency;
        private String transactionId;
    }
    
    @Data
    public static class BatchOperation {
        private String[] accountIds;
        private BigDecimal[] amounts;
        private String currency;
        private String transactionId;
    }
    
    @Data
    public static class LockAccount {
        private String accountId;
        private String reason;
    }
    
    @Data
    public static class UnlockAccount {
        private String accountId;
        private String reason;
    }
}
