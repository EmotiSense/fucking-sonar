package com.bookvault.domain.entity;

import com.bookvault.domain.enums.BorrowStatus;
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
 * Records a single borrowing transaction between a {@link Member} and a {@link Book}.
 * <p>
 * When a member borrows a book a new record is created with status {@link BorrowStatus#ACTIVE}.
 * On return the status transitions to {@link BorrowStatus#RETURNED} or
 * {@link BorrowStatus#RETURNED_LATE} depending on the return date.
 * </p>
 */
@Entity
@Table(name = "borrow_records")
public class BorrowRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BorrowStatus status;

    /** Required by JPA. */
    protected BorrowRecord() {
    }

    /**
     * Creates an active borrow record.
     *
     * @param book       the borrowed book
     * @param member     the borrowing member
     * @param borrowDate the date the book was checked out
     * @param dueDate    the date the book must be returned by
     */
    public BorrowRecord(Book book, Member member,
                        LocalDate borrowDate, LocalDate dueDate) {
        this.book = book;
        this.member = member;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = BorrowStatus.ACTIVE;
    }

    /**
     * Records the return of the book and transitions the status accordingly.
     *
     * @param returnDate the actual date the book was returned
     */
    public void recordReturn(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.status = returnDate.isAfter(dueDate)
                ? BorrowStatus.RETURNED_LATE
                : BorrowStatus.RETURNED;
    }

    /**
     * Marks this record as overdue.
     */
    public void markOverdue() {
        this.status = BorrowStatus.OVERDUE;
    }

    /**
     * Checks whether this borrow record is currently active (not yet returned).
     *
     * @return {@code true} if the record is active or overdue
     */
    public boolean isActive() {
        return status == BorrowStatus.ACTIVE || status == BorrowStatus.OVERDUE;
    }

    /**
     * Calculates the number of days the return was overdue.
     * Returns 0 when the book was not returned late.
     *
     * @return number of overdue days
     */
    public long calculateOverdueDays() {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0L;
        }
        return dueDate.until(returnDate).getDays();
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

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
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
        return "BorrowRecord{id=" + getId()
                + ", book=" + (book != null ? book.getIsbn() : "null")
                + ", member=" + (member != null ? member.getMemberNumber() : "null")
                + ", status=" + status + "}";
    }
}
