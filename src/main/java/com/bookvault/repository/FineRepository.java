package com.bookvault.repository;

import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Fine;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.FineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Fine} entities.
 */
@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    /**
     * Returns all fines for a specific member.
     *
     * @param member   the member
     * @param pageable pagination specification
     * @return page of fines
     */
    Page<Fine> findByMember(Member member, Pageable pageable);

    /**
     * Returns all outstanding fines for a specific member.
     *
     * @param member the member
     * @return list of outstanding fines
     */
    List<Fine> findByMemberAndStatus(Member member, FineStatus status);

    /**
     * Finds the fine associated with a borrow record.
     *
     * @param borrowRecord the borrow record
     * @return optional containing the fine, if any
     */
    Optional<Fine> findByBorrowRecord(BorrowRecord borrowRecord);

    /**
     * Calculates the total outstanding fine amount for a member.
     *
     * @param member the member
     * @return total outstanding amount, or 0 if none
     */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f"
            + " WHERE f.member = :member AND f.status = 'OUTSTANDING'")
    BigDecimal sumOutstandingByMember(@Param("member") Member member);

    /**
     * Returns all fines by status.
     *
     * @param status   the fine status filter
     * @param pageable pagination specification
     * @return page of fines with the given status
     */
    Page<Fine> findByStatus(FineStatus status, Pageable pageable);
}
