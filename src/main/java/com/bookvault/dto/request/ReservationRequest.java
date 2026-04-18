package com.bookvault.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for placing a book reservation.
 */
public class ReservationRequest {

    @NotNull(message = "Book ID must not be null")
    private Long bookId;

    @NotNull(message = "Member ID must not be null")
    private Long memberId;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
}
