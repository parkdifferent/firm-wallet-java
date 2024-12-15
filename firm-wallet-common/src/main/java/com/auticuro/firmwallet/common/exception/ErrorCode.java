package com.auticuro.firmwallet.common.exception;

public enum ErrorCode {
    // System errors (1000-1999)
    INTERNAL_ERROR("E001", "Internal server error"),
    INVALID_REQUEST("E002", "Invalid request"),
    SERVICE_UNAVAILABLE("E003", "Service temporarily unavailable"),
    TIMEOUT("E004", "Operation timed out"),
    
    // Account errors (2000-2999)
    ACCOUNT_NOT_FOUND("E005", "Account not found"),
    ACCOUNT_ALREADY_EXISTS("E006", "Account already exists"),
    ACCOUNT_LOCKED("E007", "Account is locked"),
    ACCOUNT_DELETED("E008", "Account is deleted"),
    INVALID_ACCOUNT_STATE("E009", "Invalid account state"),
    INVALID_ACCOUNT_CONFIG("E010", "Invalid account configuration"),
    
    // Balance errors (3000-3999)
    INSUFFICIENT_BALANCE("E011", "Insufficient balance"),
    INVALID_BALANCE("E012", "Invalid balance"),
    BALANCE_LIMIT_EXCEEDED("E013", "Balance limit exceeded"),
    INVALID_AMOUNT("E014", "Invalid amount"),
    
    // Reservation errors (4000-4999)
    RESERVATION_NOT_FOUND("E015", "Reservation not found"),
    RESERVATION_ALREADY_EXISTS("E016", "Reservation already exists"),
    INVALID_RESERVATION("E017", "Invalid reservation"),
    RESERVATION_EXPIRED("E018", "Reservation expired"),
    
    // Storage errors (5000-5999)
    STORAGE_ERROR("E019", "Storage error"),
    SNAPSHOT_ERROR("E020", "Snapshot error"),
    DATA_CORRUPTION("E021", "Data corruption detected"),
    
    // Cluster errors (6000-6999)
    NOT_LEADER("E022", "Node is not leader"),
    CLUSTER_ERROR("E023", "Cluster error"),
    CONSENSUS_ERROR("E024", "Consensus error"),
    
    // Message broker errors (7000-7999)
    MESSAGE_DELIVERY_ERROR("E025", "Message delivery error"),
    INVALID_TOPIC("E026", "Invalid topic"),
    SUBSCRIBER_ERROR("E027", "Subscriber error"),
    INVALID_CURRENCY("E028", "Invalid currency"),
    INVALID_OPERATION("E029", "Invalid operation"),
    SYSTEM_ERROR("E999", "System error");

    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }
}
