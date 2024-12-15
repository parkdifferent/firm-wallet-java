package com.auticuro.firmwallet.exception;

public class BalanceOperationException extends RuntimeException {
    public BalanceOperationException(String message) {
        super(message);
    }
    
    public BalanceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
