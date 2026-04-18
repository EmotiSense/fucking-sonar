package com.bookvault.service;

import com.bookvault.domain.enums.BorrowStatus;
import com.bookvault.domain.enums.FineStatus;
import com.bookvault.repository.BorrowRecordRepository;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.FineRepository;
import com.bookvault.repository.MemberRepository;
import com.bookvault.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates library statistics for dashboard and reporting use cases.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Constructs the service with its required repository dependencies.
     *
     * @param bookRepository         book data store
     * @param memberRepository       member data store
     * @param borrowRecordRepository borrow record data store
     * @param fineRepository         fine data store
     * @param reservationRepository  reservation data store
     */
    public ReportService(BookRepository bookRepository,
                         MemberRepository memberRepository,
                         BorrowRecordRepository borrowRecordRepository,
                         FineRepository fineRepository,
                         ReservationRepository reservationRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Returns a high-level summary of key library metrics.
     *
     * @return an ordered map of metric name to value
     */
    public Map<String, Object> getLibrarySummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBooks", bookRepository.count());
        summary.put("totalMembers", memberRepository.count());
        summary.put("activeBorrows", countBorrowsByStatus(BorrowStatus.ACTIVE));
        summary.put("overdueRecords", countBorrowsByStatus(BorrowStatus.OVERDUE));
        summary.put("outstandingFines", countFinesByStatus(FineStatus.OUTSTANDING));
        summary.put("totalReservations", reservationRepository.count());
        return summary;
    }

    /**
     * Returns a breakdown of borrow counts per status.
     *
     * @return map of borrow status name to count
     */
    public Map<String, Long> getBorrowStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (BorrowStatus status : BorrowStatus.values()) {
            breakdown.put(status.name(), countBorrowsByStatus(status));
        }
        return breakdown;
    }

    /**
     * Returns a breakdown of fine counts per status.
     *
     * @return map of fine status name to count
     */
    public Map<String, Long> getFineStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (FineStatus status : FineStatus.values()) {
            breakdown.put(status.name(), countFinesByStatus(status));
        }
        return breakdown;
    }

    /**
     * Returns inventory statistics including available vs total copies.
     *
     * @return map of inventory metric names to values
     */
    public Map<String, Object> getInventoryStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        long totalBooks = bookRepository.count();
        long availableBooks = bookRepository.findAllAvailable().size();
        stats.put("totalBookTitles", totalBooks);
        stats.put("titlesWithAvailableCopies", availableBooks);
        stats.put("titlesFullyCheckedOut", totalBooks - availableBooks);
        return stats;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private long countBorrowsByStatus(BorrowStatus status) {
        return borrowRecordRepository.findByStatus(
                status, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private long countFinesByStatus(FineStatus status) {
        return fineRepository.findByStatus(
                status, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }
}
