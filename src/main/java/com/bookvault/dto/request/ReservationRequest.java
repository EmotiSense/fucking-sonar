package com.bookvault.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "Book ID must not be null") Long bookId,
        @NotNull(message = "Member ID must not be null") Long memberId) {
}
