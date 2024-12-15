package com.auticuro.firmwallet.util;

import java.math.BigDecimal;
import java.util.Map;

public class BalanceCalculator {
    public static BigDecimal calculateAvailable(BigDecimal total, BigDecimal reserved) {
        return total.subtract(reserved);
    }

    public static BigDecimal calculateReserved(Map<String, BigDecimal> reservations) {
        return reservations.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static boolean isValidTransfer(BigDecimal available, BigDecimal amount, BigDecimal minAvailable) {
        return available.subtract(amount).compareTo(minAvailable) >= 0;
    }
}
