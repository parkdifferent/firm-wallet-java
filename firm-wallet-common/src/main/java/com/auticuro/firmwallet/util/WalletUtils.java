package com.auticuro.firmwallet.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WalletUtils {
    public static String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isValidCurrency(String currency) {
        return currency != null && currency.matches("[A-Z]{3}");
    }
}
