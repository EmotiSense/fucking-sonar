package com.bookvault.domain.entity;

import com.bookvault.domain.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Records a member's reservation for a book that is currently unavailable.
 */
@Entity
@Table(name = "reservations")
public class Reservation extends LibraryTransaction {

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

    public Reservation(Book book, Member member, LocalDate reservationDate) {
        super(book, member);
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.PENDING;
    }

    public void markReady(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        this.status = ReservationStatus.READY;
    }

    public void markFulfilled() {
        this.status = ReservationStatus.FULFILLED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    public boolean isOpen() {
        return status == ReservationStatus.PENDING
                || status == ReservationStatus.READY;
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
                + ", book=" + (getBook() != null ? getBook().getIsbn() : "null")
                + ", member=" + (getMember() != null ? getMember().getMemberNumber() : "null")
                + ", status=" + status + "}";
    }
}
