package com.bookvault.domain.enums;

/**
 * Represents the lifecycle states of a book borrowing record.
 */
public enum BorrowStatus {

    /** The book has been borrowed and not yet returned. */
    ACTIVE,

    /** The book was returned on or before the due date. */
    RETURNED,

    /** The book was returned after the due date. */
    RETURNED_LATE,

    /** The due date has passed and the book has not been returned. */
    OVERDUE
}
