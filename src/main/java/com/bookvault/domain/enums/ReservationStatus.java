package com.bookvault.domain.enums;

/**
 * Represents the lifecycle states of a book reservation.
 */
public enum ReservationStatus {

    /** The reservation is active and awaiting book availability. */
    PENDING,

    /** The reserved book is available and ready for pickup. */
    READY,

    /** The member collected the reserved book (converted to a borrow record). */
    FULFILLED,

    /** The reservation was cancelled by the member or system. */
    CANCELLED,

    /** The reservation window expired before the member collected the book. */
    EXPIRED
}
