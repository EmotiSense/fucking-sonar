package com.bookvault.domain.entity;

import com.bookvault.domain.enums.FineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a financial penalty issued to a member for a late book return.
 * <p>
 * A fine is linked to the specific {@link BorrowRecord} that caused it and
 * to the {@link Member} who owes it. The status progresses from
 * {@link FineStatus#OUTSTANDING} to either {@link FineStatus#PAID} or
 * {@link FineStatus#WAIVED}.
 * </p>
 */
@Entity
@Table(name = "fines")
public class Fine extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, length = 300)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FineStatus status;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    /** Required by JPA. */
    protected Fine() {
    }

    /**
     * Creates an outstanding fine.
     *
     * @param borrowRecord the borrow record that triggered this fine
     * @param member       the member who owes the fine
     * @param amount       the monetary amount due
     * @param reason       a human-readable explanation
     */
    public Fine(BorrowRecord borrowRecord, Member member,
                BigDecimal amount, String reason) {
        this.borrowRecord = borrowRecord;
        this.member = member;
        this.amount = amount;
        this.reason = reason;
        this.status = FineStatus.OUTSTANDING;
        this.issuedDate = LocalDate.now();
    }

    /**
     * Records payment of the fine.
     *
     * @param paymentDate the date on which payment was received
     */
    public void markPaid(LocalDate paymentDate) {
        this.status = FineStatus.PAID;
        this.paidDate = paymentDate;
    }

    /**
     * Waives the fine (no payment required).
     */
    public void waive() {
        this.status = FineStatus.WAIVED;
    }

    /**
     * Returns whether the fine still requires payment.
     *
     * @return {@code true} when the fine status is {@link FineStatus#OUTSTANDING}
     */
    public boolean isOutstanding() {
        return FineStatus.OUTSTANDING == status;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public FineStatus getStatus() {
        return status;
    }

    public void setStatus(FineStatus status) {
        this.status = status;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
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
        return "Fine{id=" + getId()
                + ", member=" + (member != null ? member.getMemberNumber() : "null")
                + ", amount=" + amount
                + ", status=" + status + "}";
    }
}
