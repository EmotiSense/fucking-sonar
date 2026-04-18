package com.bookvault.service;

import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Fine;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.BorrowStatus;
import com.bookvault.domain.enums.FineStatus;
import com.bookvault.dto.response.FineResponse;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BorrowRecordRepository;
import com.bookvault.repository.FineRepository;
import com.bookvault.util.FineCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for issuing, paying, and waiving library fines.
 */
@Service
@Transactional(readOnly = true)
public class FineService {

    private static final String FINE_ENTITY = "Fine";
    private static final String BORROW_ENTITY = "BorrowRecord";

    @Value("${library.borrow.fine-per-day:1.00}")
    private BigDecimal finePerDay;

    private final FineRepository fineRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final MemberService memberService;

    /**
     * Constructs the service with its required dependencies.
     *
     * @param fineRepository         the fine data store
     * @param borrowRecordRepository the borrow record data store
     * @param memberService          the member service
     */
    public FineService(FineRepository fineRepository,
                       BorrowRecordRepository borrowRecordRepository,
                       MemberService memberService) {
        this.fineRepository = fineRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.memberService = memberService;
    }

    /**
     * Returns a paginated list of all fines.
     *
     * @param pageable pagination parameters
     * @return paged fine responses
     */
    public PageResponse<FineResponse> findAll(Pageable pageable) {
        Page<FineResponse> page = fineRepository.findAll(pageable)
                .map(FineResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns a single fine by its ID.
     *
     * @param id the fine ID
     * @return the fine response
     * @throws ResourceNotFoundException if no fine exists with the given ID
     */
    public FineResponse findById(Long id) {
        return FineResponse.from(loadById(id));
    }

    /**
     * Returns all fines for a specific member.
     *
     * @param memberId the member ID
     * @param pageable pagination parameters
     * @return paged fine responses
     */
    public PageResponse<FineResponse> findByMember(Long memberId, Pageable pageable) {
        Member member = memberService.loadById(memberId);
        Page<FineResponse> page = fineRepository.findByMember(member, pageable)
                .map(FineResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns all outstanding fines for a member.
     *
     * @param memberId the member ID
     * @return list of outstanding fine responses
     */
    public List<FineResponse> findOutstandingByMember(Long memberId) {
        Member member = memberService.loadById(memberId);
        return fineRepository.findByMemberAndStatus(member, FineStatus.OUTSTANDING)
                .stream()
                .map(FineResponse::from)
                .toList();
    }

    /**
     * Calculates the total outstanding fine amount for a member.
     *
     * @param memberId the member ID
     * @return the total outstanding amount
     */
    public BigDecimal getTotalOutstanding(Long memberId) {
        Member member = memberService.loadById(memberId);
        return fineRepository.sumOutstandingByMember(member);
    }

    /**
     * Issues a fine for a late-returned borrow record.
     *
     * @param borrowRecordId the borrow record ID
     * @return the created fine response
     * @throws ResourceNotFoundException if the record does not exist
     * @throws BusinessRuleException     if the record was not returned late or already has a fine
     */
    @Transactional
    public FineResponse issueFine(Long borrowRecordId) {
        BorrowRecord borrowRecord = loadBorrowRecord(borrowRecordId);
        assertRecordReturnedLate(borrowRecord);
        assertNoExistingFine(borrowRecord);
        BigDecimal amount = computeFineAmount(borrowRecord);
        String reason = buildFineReason(borrowRecord);
        Fine fine = new Fine(borrowRecord, borrowRecord.getMember(), amount, reason);
        Fine saved = fineRepository.save(fine);
        return FineResponse.from(saved);
    }

    /**
     * Records payment of a fine.
     *
     * @param fineId the fine ID
     * @return the updated fine response
     * @throws ResourceNotFoundException if no fine exists with the given ID
     * @throws BusinessRuleException     if the fine is not outstanding
     */
    @Transactional
    public FineResponse payFine(Long fineId) {
        Fine fine = loadById(fineId);
        assertFineIsOutstanding(fine);
        fine.markPaid(LocalDate.now());
        Fine saved = fineRepository.save(fine);
        return FineResponse.from(saved);
    }

    /**
     * Waives a fine (no payment required).
     *
     * @param fineId the fine ID
     * @return the updated fine response
     * @throws ResourceNotFoundException if no fine exists with the given ID
     * @throws BusinessRuleException     if the fine is not outstanding
     */
    @Transactional
    public FineResponse waiveFine(Long fineId) {
        Fine fine = loadById(fineId);
        assertFineIsOutstanding(fine);
        fine.waive();
        Fine saved = fineRepository.save(fine);
        return FineResponse.from(saved);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Fine loadById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FINE_ENTITY, id));
    }

    private BorrowRecord loadBorrowRecord(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BORROW_ENTITY, id));
    }

    private void assertRecordReturnedLate(BorrowRecord borrowRecord) {
        boolean isLate = borrowRecord.getStatus() == BorrowStatus.RETURNED_LATE
                || borrowRecord.getStatus() == BorrowStatus.OVERDUE;
        if (!isLate) {
            throw new BusinessRuleException(
                    "Borrow record " + borrowRecord.getId() + " was not returned late");
        }
    }

    private void assertNoExistingFine(BorrowRecord borrowRecord) {
        fineRepository.findByBorrowRecord(borrowRecord).ifPresent(existing -> {
            throw new BusinessRuleException(
                    "A fine already exists for borrow record " + borrowRecord.getId());
        });
    }

    private void assertFineIsOutstanding(Fine fine) {
        if (!fine.isOutstanding()) {
            throw new BusinessRuleException(
                    "Fine " + fine.getId() + " is not outstanding");
        }
    }

    private BigDecimal computeFineAmount(BorrowRecord borrowRecord) {
        return FineCalculator.calculate(
                borrowRecord.getDueDate(),
                borrowRecord.getReturnDate() != null ? borrowRecord.getReturnDate() : LocalDate.now(),
                finePerDay);
    }

    private String buildFineReason(BorrowRecord borrowRecord) {
        long days = FineCalculator.computeOverdueDays(
                borrowRecord.getDueDate(),
                borrowRecord.getReturnDate() != null ? borrowRecord.getReturnDate() : LocalDate.now());
        return "Late return of '" + borrowRecord.getBook().getTitle()
                + "' — " + days + " day(s) overdue";
    }
}
