package com.bookvault.dto.response;

import com.bookvault.domain.entity.Fine;
import com.bookvault.domain.enums.FineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only view of a {@link Fine} entity returned to API clients.
 */
public class FineResponse {

    private Long id;
    private Long memberId;
    private String memberNumber;
    private String memberFullName;
    private Long borrowRecordId;
    private BigDecimal amount;
    private String reason;
    private FineStatus status;
    private LocalDate issuedDate;
    private LocalDate paidDate;

    public FineResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link Fine} entity to a response DTO.
     *
     * @param fine the source entity
     * @return the populated response
     */
    public static FineResponse from(Fine fine) {
        FineResponse response = new FineResponse();
        response.id = fine.getId();
        response.amount = fine.getAmount();
        response.reason = fine.getReason();
        response.status = fine.getStatus();
        response.issuedDate = fine.getIssuedDate();
        response.paidDate = fine.getPaidDate();
        if (fine.getMember() != null) {
            response.memberId = fine.getMember().getId();
            response.memberNumber = fine.getMember().getMemberNumber();
            response.memberFullName = fine.getMember().getFullName();
        }
        if (fine.getBorrowRecord() != null) {
            response.borrowRecordId = fine.getBorrowRecord().getId();
        }
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberNumber() { return memberNumber; }
    public void setMemberNumber(String memberNumber) { this.memberNumber = memberNumber; }

    public String getMemberFullName() { return memberFullName; }
    public void setMemberFullName(String memberFullName) { this.memberFullName = memberFullName; }

    public Long getBorrowRecordId() { return borrowRecordId; }
    public void setBorrowRecordId(Long borrowRecordId) { this.borrowRecordId = borrowRecordId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) { this.status = status; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
}
