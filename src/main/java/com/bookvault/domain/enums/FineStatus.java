package com.bookvault.domain.enums;

/**
 * Represents the payment state of a library fine.
 */
public enum FineStatus {

    /** The fine has been issued but not yet paid. */
    OUTSTANDING,

    /** The fine has been paid in full. */
    PAID,

    /** The fine was waived by a librarian. */
    WAIVED
}
