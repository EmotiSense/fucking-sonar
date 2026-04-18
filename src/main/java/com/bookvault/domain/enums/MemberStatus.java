package com.bookvault.domain.enums;

/**
 * Represents the active / inactive state of a library member.
 */
public enum MemberStatus {

    /** The member is in good standing and may borrow books. */
    ACTIVE,

    /** The member account has been suspended (e.g., unpaid fines). */
    SUSPENDED,

    /** The member has been permanently deactivated. */
    DEACTIVATED
}
