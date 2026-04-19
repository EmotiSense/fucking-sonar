package com.bookvault.domain.entity;

import com.bookvault.domain.enums.BorrowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Records a single borrowing transaction between a {@link Member} and a {@link Book}.
 */
@Entity
@Table(name = "borrow_records")
public class BorrowRecord extends LibraryTransaction {

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

    public BorrowRecord(Book book, Member member,
                        LocalDate borrowDate, LocalDate dueDate) {
        super(book, member);
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = BorrowStatus.ACTIVE;
    }

    public void recordReturn(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.status = returnDate.isAfter(dueDate)
                ? BorrowStatus.RETURNED_LATE
                : BorrowStatus.RETURNED;
    }

    public void markOverdue() {
        this.status = BorrowStatus.OVERDUE;
    }

    public boolean isActive() {
        return status == BorrowStatus.ACTIVE || status == BorrowStatus.OVERDUE;
    }

    public long calculateOverdueDays() {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0L;
        }
        return dueDate.until(returnDate).getDays();
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
                + ", book=" + (getBook() != null ? getBook().getIsbn() : "null")
                + ", member=" + (getMember() != null ? getMember().getMemberNumber() : "null")
                + ", status=" + status + "}";
    }
}
