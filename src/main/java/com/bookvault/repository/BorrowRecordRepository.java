package com.bookvault.repository;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link BorrowRecord} entities.
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    /**
     * Returns all borrow records for a specific member.
     *
     * @param member   the member
     * @param pageable pagination specification
     * @return page of borrow records for the member
     */
    Page<BorrowRecord> findByMember(Member member, Pageable pageable);

    /**
     * Returns all borrow records for a specific book.
     *
     * @param book     the book
     * @param pageable pagination specification
     * @return page of borrow records for the book
     */
    Page<BorrowRecord> findByBook(Book book, Pageable pageable);

    /**
     * Counts how many books a member is currently borrowing.
     *
     * @param member the member
     * @param status the borrow status to count
     * @return the count of active borrow records
     */
    long countByMemberAndStatus(Member member, BorrowStatus status);

    /**
     * Finds an active borrow record for a specific member and book combination.
     *
     * @param member the member
     * @param book   the book
     * @param status the borrow status
     * @return list of matching records (should contain at most one)
     */
    List<BorrowRecord> findByMemberAndBookAndStatus(Member member, Book book, BorrowStatus status);

    /**
     * Finds all overdue records (active records where due date has passed).
     *
     * @param today the current date used for comparison
     * @return list of overdue borrow records
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'ACTIVE' AND br.dueDate < :today")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    /**
     * Returns all borrow records with a given status.
     *
     * @param status   the borrow status filter
     * @param pageable pagination specification
     * @return page of records with the specified status
     */
    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);

    /**
     * Counts total borrow records for a member in a date range.
     *
     * @param member    the member
     * @param startDate start of the period (inclusive)
     * @param endDate   end of the period (inclusive)
     * @return the count of borrow records
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.member = :member"
            + " AND br.borrowDate BETWEEN :startDate AND :endDate")
    long countByMemberAndDateRange(@Param("member") Member member,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}
