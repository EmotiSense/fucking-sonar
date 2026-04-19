package com.bookvault.dto.response;

import com.bookvault.domain.entity.Fine;
import com.bookvault.domain.enums.FineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only view of a {@link Fine} entity returned to API clients.
 */
public record FineResponse(
        Long id,
        Long memberId,
        String memberNumber,
        String memberFullName,
        Long borrowRecordId,
        BigDecimal amount,
        String reason,
        FineStatus status,
        LocalDate issuedDate,
        LocalDate paidDate) {

    public static FineResponse from(Fine fine) {
        Long memberId = fine.getMember() != null ? fine.getMember().getId() : null;
        String memberNumber = fine.getMember() != null ? fine.getMember().getMemberNumber() : null;
        String memberFullName = fine.getMember() != null ? fine.getMember().getFullName() : null;
        Long borrowRecordId = fine.getBorrowRecord() != null ? fine.getBorrowRecord().getId() : null;
        return new FineResponse(
                fine.getId(),
                memberId,
                memberNumber,
                memberFullName,
                borrowRecordId,
                fine.getAmount(),
                fine.getReason(),
                fine.getStatus(),
                fine.getIssuedDate(),
                fine.getPaidDate());
    }
}
