package com.auticuro.firmwallet.common.utils;

import com.auticuro.firmwallet.exception.BalanceOperationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BalanceCalculator {
    
    /**
     * Add two numbers, represented by type String
     * Example: "12.34" + "0.020" = "12.36"
     */
    public String add(String lhs, String rhs) {
        try {
            BigDecimal lhsDecimal = new BigDecimal(lhs);
            BigDecimal rhsDecimal = new BigDecimal(rhs);
            return lhsDecimal.add(rhsDecimal).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }

    public BigDecimal add(BigDecimal lhsDecimal, BigDecimal rhsDecimal) {
        try {
            return lhsDecimal.add(rhsDecimal).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }
    
    /**
     * Calculate the negative of a number, represented by type String
     * Example: neg("-12.34") = "12.34"
     */
    public String neg(String input) {
        try {
            BigDecimal decimal = new BigDecimal(input);
            return decimal.negate().stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }
    
    /**
     * Compare two numbers, represented by type String
     * Returns:
     * -1 if lhs < rhs
     * 0 if lhs == rhs
     * 1 if lhs > rhs
     */
    public int compare(String lhs, String rhs) {
        try {
            BigDecimal lhsDecimal = new BigDecimal(lhs);
            BigDecimal rhsDecimal = new BigDecimal(rhs);
            return lhsDecimal.compareTo(rhsDecimal);
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }
    
    /**
     * Subtract two numbers, represented by type String
     * Example: "12.34" - "0.020" = "12.32"
     */
    public BigDecimal subtract(String lhs, String rhs) {
        try {
            BigDecimal lhsDecimal = new BigDecimal(lhs);
            BigDecimal rhsDecimal = new BigDecimal(rhs);
            return lhsDecimal.subtract(rhsDecimal).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }

    public BigDecimal subtract(BigDecimal lhsDecimal, BigDecimal rhsDecimal) {
        try {
            return lhsDecimal.subtract(rhsDecimal).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }
    
    /**
     * Check if a number is greater than or equal to zero
     */
    public boolean isNonNegative(String number) {
        try {
            BigDecimal decimal = new BigDecimal(number);
            return decimal.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            throw new BalanceOperationException("Failed to parse number: " + e.getMessage());
        }
    }
}
