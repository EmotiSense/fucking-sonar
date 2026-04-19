package com.bookvault.dto.response;

import com.bookvault.domain.entity.Reservation;
import com.bookvault.domain.enums.ReservationStatus;

import java.time.LocalDate;

/**
 * Read-only view of a {@link Reservation} entity returned to API clients.
 */
public record ReservationResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String bookIsbn,
        Long memberId,
        String memberNumber,
        String memberFullName,
        LocalDate reservationDate,
        LocalDate expiryDate,
        ReservationStatus status) {

    public static ReservationResponse from(Reservation reservation) {
        Long bookId = reservation.getBook() != null ? reservation.getBook().getId() : null;
        String bookTitle = reservation.getBook() != null ? reservation.getBook().getTitle() : null;
        String bookIsbn = reservation.getBook() != null ? reservation.getBook().getIsbn() : null;
        Long memberId = reservation.getMember() != null ? reservation.getMember().getId() : null;
        String memberNumber = reservation.getMember() != null ? reservation.getMember().getMemberNumber() : null;
        String memberFullName = reservation.getMember() != null ? reservation.getMember().getFullName() : null;
        return new ReservationResponse(
                reservation.getId(),
                bookId, bookTitle, bookIsbn,
                memberId, memberNumber, memberFullName,
                reservation.getReservationDate(),
                reservation.getExpiryDate(),
                reservation.getStatus());
    }
}
