package com.bookvault.domain.entity;

import com.bookvault.domain.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Records a member's reservation for a book that is currently unavailable.
 * <p>
 * When the book becomes available the reservation transitions to
 * {@link ReservationStatus#READY} and the member may collect it.
 * Uncollected reservations expire automatically.
 * </p>
 */
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    /** Required by JPA. */
    protected Reservation() {
    }

    /**
     * Creates a pending reservation.
     *
     * @param book             the reserved book
     * @param member           the reserving member
     * @param reservationDate  the date the reservation was made
     */
    public Reservation(Book book, Member member, LocalDate reservationDate) {
        this.book = book;
        this.member = member;
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.PENDING;
    }

    /**
     * Transitions the reservation to READY when the book becomes available,
     * and sets the expiry window.
     *
     * @param expiryDate the last date the member may collect the book
     */
    public void markReady(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        this.status = ReservationStatus.READY;
    }

    /**
     * Marks this reservation as fulfilled (book collected and borrowed).
     */
    public void markFulfilled() {
        this.status = ReservationStatus.FULFILLED;
    }

    /**
     * Cancels this reservation.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /**
     * Expires this reservation (member did not collect in time).
     */
    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    /**
     * Checks whether this reservation is still pending or ready (i.e., open).
     *
     * @return {@code true} if the reservation has not been resolved
     */
    public boolean isOpen() {
        return status == ReservationStatus.PENDING
                || status == ReservationStatus.READY;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Reservation{id=" + getId()
                + ", book=" + (book != null ? book.getIsbn() : "null")
                + ", member=" + (member != null ? member.getMemberNumber() : "null")
                + ", status=" + status + "}";
    }
}
