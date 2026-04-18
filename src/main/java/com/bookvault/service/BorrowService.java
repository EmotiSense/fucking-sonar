package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.BorrowStatus;
import com.bookvault.dto.request.BorrowRequest;
import com.bookvault.dto.response.BorrowRecordResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.BorrowRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for checking books in and out.
 */
@Service
@Transactional(readOnly = true)
public class BorrowService {

    private static final String BORROW_RECORD_ENTITY = "BorrowRecord";
    private static final String BOOK_ENTITY = "Book";
    private static final String MEMBER_PREFIX = "Member ";

    @Value("${library.borrow.max-days:14}")
    private int maxBorrowDays;

    @Value("${library.borrow.max-books-per-member:5}")
    private int maxBooksPerMember;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final MemberService memberService;

    /**
     * Constructs the service with its required dependencies.
     *
     * @param borrowRecordRepository the borrow record data store
     * @param bookRepository         the book data store
     * @param memberService          the member service for loading member entities
     */
    public BorrowService(BorrowRecordRepository borrowRecordRepository,
                         BookRepository bookRepository,
                         MemberService memberService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.memberService = memberService;
    }

    /**
     * Returns a paginated list of all borrow records.
     *
     * @param pageable pagination parameters
     * @return paged borrow record responses
     */
    public PageResponse<BorrowRecordResponse> findAll(Pageable pageable) {
        Page<BorrowRecordResponse> page = borrowRecordRepository.findAll(pageable)
                .map(BorrowRecordResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns a single borrow record by ID.
     *
     * @param id the record ID
     * @return the borrow record response
     * @throws ResourceNotFoundException if no record exists with the given ID
     */
    public BorrowRecordResponse findById(Long id) {
        BorrowRecord borrowRecord = loadById(id);
        return BorrowRecordResponse.from(borrowRecord);
    }

    /**
     * Returns paginated borrow records for a specific member.
     *
     * @param memberId the member ID
     * @param pageable pagination parameters
     * @return paged borrow record responses
     */
    public PageResponse<BorrowRecordResponse> findByMember(Long memberId, Pageable pageable) {
        Member member = memberService.loadById(memberId);
        Page<BorrowRecordResponse> page = borrowRecordRepository
                .findByMember(member, pageable)
                .map(BorrowRecordResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns paginated borrow records for a specific book.
     *
     * @param bookId   the book ID
     * @param pageable pagination parameters
     * @return paged borrow record responses
     */
    public PageResponse<BorrowRecordResponse> findByBook(Long bookId, Pageable pageable) {
        Book book = loadBook(bookId);
        Page<BorrowRecordResponse> page = borrowRecordRepository
                .findByBook(book, pageable)
                .map(BorrowRecordResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all currently active borrow records (status = ACTIVE or OVERDUE).
     *
     * @param pageable pagination parameters
     * @return paged borrow record responses
     */
    public PageResponse<BorrowRecordResponse> findActive(Pageable pageable) {
        Page<BorrowRecordResponse> page = borrowRecordRepository
                .findByStatus(BorrowStatus.ACTIVE, pageable)
                .map(BorrowRecordResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all currently overdue records.
     *
     * @return list of overdue borrow record responses
     */
    public List<BorrowRecordResponse> findOverdue() {
        return borrowRecordRepository.findOverdueRecords(LocalDate.now())
                .stream()
                .map(BorrowRecordResponse::from)
                .toList();
    }

    /**
     * Checks out a book to a member.
     *
     * @param request the borrow request
     * @return the created borrow record response
     * @throws BusinessRuleException     if the member is not active, has reached the borrow
     *                                   limit, or the book has no available copies
     * @throws ResourceNotFoundException if the book or member does not exist
     */
    @Transactional
    public BorrowRecordResponse borrowBook(BorrowRequest request) {
        Member member = memberService.loadById(request.getMemberId());
        Book book = loadBook(request.getBookId());
        validateBorrow(member, book);
        book.decrementAvailable();
        bookRepository.save(book);
        BorrowRecord borrowRecord = createRecord(book, member);
        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordResponse.from(saved);
    }

    /**
     * Returns a borrowed book and closes the borrow record.
     *
     * @param recordId the borrow record ID
     * @return the updated borrow record response
     * @throws ResourceNotFoundException if the record does not exist
     * @throws BusinessRuleException     if the record is not currently active
     */
    @Transactional
    public BorrowRecordResponse returnBook(Long recordId) {
        BorrowRecord borrowRecord = loadById(recordId);
        assertRecordIsActive(borrowRecord);
        borrowRecord.recordReturn(LocalDate.now());
        borrowRecord.getBook().incrementAvailable();
        bookRepository.save(borrowRecord.getBook());
        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
        return BorrowRecordResponse.from(saved);
    }

    /**
     * Marks all active records whose due date has passed as overdue.
     * This is intended to be called by a scheduled task.
     *
     * @return the number of records updated
     */
    @Transactional
    public int markOverdueRecords() {
        List<BorrowRecord> overdue = borrowRecordRepository.findOverdueRecords(LocalDate.now());
        overdue.forEach(BorrowRecord::markOverdue);
        borrowRecordRepository.saveAll(overdue);
        return overdue.size();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private BorrowRecord loadById(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BORROW_RECORD_ENTITY, id));
    }

    private Book loadBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_ENTITY, bookId));
    }

    private void validateBorrow(Member member, Book book) {
        assertMemberCanBorrow(member);
        assertBorrowLimitNotReached(member);
        assertBookAvailable(book);
        assertNotAlreadyBorrowed(member, book);
    }

    private void assertMemberCanBorrow(Member member) {
        if (!member.canBorrow()) {
            throw new BusinessRuleException(
                    MEMBER_PREFIX + member.getMemberNumber() + " is not allowed to borrow books");
        }
    }

    private void assertBorrowLimitNotReached(Member member) {
        long active = borrowRecordRepository.countByMemberAndStatus(member, BorrowStatus.ACTIVE);
        if (active >= maxBooksPerMember) {
            throw new BusinessRuleException(
                    MEMBER_PREFIX + member.getMemberNumber()
                            + " has reached the borrow limit of " + maxBooksPerMember + " books");
        }
    }

    private void assertBookAvailable(Book book) {
        if (!book.isAvailable()) {
            throw new BusinessRuleException(
                    "Book '" + book.getTitle() + "' has no available copies");
        }
    }

    private void assertNotAlreadyBorrowed(Member member, Book book) {
        List<BorrowRecord> existing = borrowRecordRepository
                .findByMemberAndBookAndStatus(member, book, BorrowStatus.ACTIVE);
        if (!existing.isEmpty()) {
            throw new BusinessRuleException(
                    MEMBER_PREFIX + member.getMemberNumber()
                            + " already has an active borrow for book '" + book.getTitle() + "'");
        }
    }

    private void assertRecordIsActive(BorrowRecord borrowRecord) {
        if (!borrowRecord.isActive()) {
            throw new BusinessRuleException(
                    "Borrow record " + borrowRecord.getId() + " is not active");
        }
    }

    private BorrowRecord createRecord(Book book, Member member) {
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(maxBorrowDays);
        return new BorrowRecord(book, member, borrowDate, dueDate);
    }
}
