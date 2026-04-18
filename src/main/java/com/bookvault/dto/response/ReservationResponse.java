package com.bookvault.dto.response;

import com.bookvault.domain.entity.Reservation;
import com.bookvault.domain.enums.ReservationStatus;

import java.time.LocalDate;

/**
 * Read-only view of a {@link Reservation} entity returned to API clients.
 */
public class ReservationResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long memberId;
    private String memberNumber;
    private String memberFullName;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private ReservationStatus status;

    public ReservationResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link Reservation} entity to a response DTO.
     *
     * @param reservation the source entity
     * @return the populated response
     */
    public static ReservationResponse from(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.id = reservation.getId();
        response.reservationDate = reservation.getReservationDate();
        response.expiryDate = reservation.getExpiryDate();
        response.status = reservation.getStatus();
        populateBookFields(response, reservation);
        populateMemberFields(response, reservation);
        return response;
    }

    private static void populateBookFields(ReservationResponse response, Reservation reservation) {
        if (reservation.getBook() != null) {
            response.bookId = reservation.getBook().getId();
            response.bookTitle = reservation.getBook().getTitle();
            response.bookIsbn = reservation.getBook().getIsbn();
        }
    }

    private static void populateMemberFields(ReservationResponse response, Reservation reservation) {
        if (reservation.getMember() != null) {
            response.memberId = reservation.getMember().getId();
            response.memberNumber = reservation.getMember().getMemberNumber();
            response.memberFullName = reservation.getMember().getFullName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberNumber() { return memberNumber; }
    public void setMemberNumber(String memberNumber) { this.memberNumber = memberNumber; }

    public String getMemberFullName() { return memberFullName; }
    public void setMemberFullName(String memberFullName) { this.memberFullName = memberFullName; }

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
