package com.bookvault.exception;

/**
 * Base exception for all application-specific errors in BookVault.
 */
public class BookVaultException extends RuntimeException {

    private final String errorCode;

    /**
     * Creates a new exception with a message and error code.
     *
     * @param message   human-readable description of the error
     * @param errorCode machine-readable error code for clients
     */
    public BookVaultException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates a new exception wrapping a cause.
     *
     * @param message   human-readable description of the error
     * @param errorCode machine-readable error code for clients
     * @param cause     the original exception
     */
    public BookVaultException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code.
     *
     * @return error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
