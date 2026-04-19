package com.bookvault.dto.response;

import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.enums.BorrowStatus;

import java.time.LocalDate;

/**
 * Read-only view of a {@link BorrowRecord} entity returned to API clients.
 */
public record BorrowRecordResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String bookIsbn,
        Long memberId,
        String memberNumber,
        String memberFullName,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        BorrowStatus status,
        long overdueDays) {

    public static BorrowRecordResponse from(BorrowRecord borrowRecord) {
        Long bookId = borrowRecord.getBook() != null ? borrowRecord.getBook().getId() : null;
        String bookTitle = borrowRecord.getBook() != null ? borrowRecord.getBook().getTitle() : null;
        String bookIsbn = borrowRecord.getBook() != null ? borrowRecord.getBook().getIsbn() : null;
        Long memberId = borrowRecord.getMember() != null ? borrowRecord.getMember().getId() : null;
        String memberNumber = borrowRecord.getMember() != null ? borrowRecord.getMember().getMemberNumber() : null;
        String memberFullName = borrowRecord.getMember() != null ? borrowRecord.getMember().getFullName() : null;
        return new BorrowRecordResponse(
                borrowRecord.getId(),
                bookId, bookTitle, bookIsbn,
                memberId, memberNumber, memberFullName,
                borrowRecord.getBorrowDate(),
                borrowRecord.getDueDate(),
                borrowRecord.getReturnDate(),
                borrowRecord.getStatus(),
                borrowRecord.calculateOverdueDays());
    }
}
