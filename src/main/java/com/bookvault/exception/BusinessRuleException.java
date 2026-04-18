package com.bookvault.exception;

/**
 * Thrown when a business rule is violated (e.g., borrowing a book with no available copies).
 */
public class BusinessRuleException extends BookVaultException {

    private static final String DEFAULT_CODE = "BUSINESS_RULE_VIOLATION";

    /**
     * Creates a business rule exception with a descriptive message.
     *
     * @param message human-readable explanation of the violated rule
     */
    public BusinessRuleException(String message) {
        super(message, DEFAULT_CODE);
    }

    /**
     * Creates a business rule exception with a custom error code.
     *
     * @param message   human-readable explanation
     * @param errorCode the specific business rule error code
     */
    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}
