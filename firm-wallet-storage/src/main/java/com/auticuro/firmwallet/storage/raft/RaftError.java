package com.auticuro.firmwallet.storage.raft;

public class RaftError extends RuntimeException {
    public static final int UNKNOWN = -1;
    public static final int EINVAL = 22;  // Invalid argument
    public static final int EIO = 5;      // I/O error
    public static final int EBUSY = 16;   // Device or resource busy

    private final int errorCode;

    public RaftError(String message) {
        this(UNKNOWN, message);
    }

    public RaftError(String message, Throwable cause) {
        this(UNKNOWN, message, cause);
    }

    public RaftError(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RaftError(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
