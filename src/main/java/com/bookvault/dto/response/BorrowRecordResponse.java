package com.bookvault.dto.response;

import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.enums.BorrowStatus;

import java.time.LocalDate;

/**
 * Read-only view of a {@link BorrowRecord} entity returned to API clients.
 */
public class BorrowRecordResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long memberId;
    private String memberNumber;
    private String memberFullName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private long overdueDays;

    public BorrowRecordResponse() {
        // Required for JSON deserialisation by Jackson
    }

    /**
     * Maps a {@link BorrowRecord} entity to a response DTO.
     *
     * @param record the source entity
     * @return the populated response
     */
    public static BorrowRecordResponse from(BorrowRecord borrowRecord) {
        BorrowRecordResponse response = new BorrowRecordResponse();
        response.id = borrowRecord.getId();
        response.borrowDate = borrowRecord.getBorrowDate();
        response.dueDate = borrowRecord.getDueDate();
        response.returnDate = borrowRecord.getReturnDate();
        response.status = borrowRecord.getStatus();
        response.overdueDays = borrowRecord.calculateOverdueDays();
        populateBookFields(response, borrowRecord);
        populateMemberFields(response, borrowRecord);
        return response;
    }

    private static void populateBookFields(BorrowRecordResponse response, BorrowRecord borrowRecord) {
        if (borrowRecord.getBook() != null) {
            response.bookId = borrowRecord.getBook().getId();
            response.bookTitle = borrowRecord.getBook().getTitle();
            response.bookIsbn = borrowRecord.getBook().getIsbn();
        }
    }

    private static void populateMemberFields(BorrowRecordResponse response, BorrowRecord borrowRecord) {
        if (borrowRecord.getMember() != null) {
            response.memberId = borrowRecord.getMember().getId();
            response.memberNumber = borrowRecord.getMember().getMemberNumber();
            response.memberFullName = borrowRecord.getMember().getFullName();
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

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }

    public long getOverdueDays() { return overdueDays; }
    public void setOverdueDays(long overdueDays) { this.overdueDays = overdueDays; }
}
