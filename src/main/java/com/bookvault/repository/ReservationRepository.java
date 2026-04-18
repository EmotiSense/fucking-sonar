package com.bookvault.repository;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.entity.Reservation;
import com.bookvault.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Reservation} entities.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Returns all reservations for a specific member.
     *
     * @param member   the member
     * @param pageable pagination specification
     * @return page of reservations
     */
    Page<Reservation> findByMember(Member member, Pageable pageable);

    /**
     * Returns all reservations for a specific book.
     *
     * @param book     the book
     * @param pageable pagination specification
     * @return page of reservations
     */
    Page<Reservation> findByBook(Book book, Pageable pageable);

    /**
     * Finds open reservations (PENDING or READY) for a specific member and book.
     *
     * @param member the member
     * @param book   the book
     * @return optional containing the open reservation, if any
     */
    @Query("SELECT r FROM Reservation r WHERE r.member = :member AND r.book = :book"
            + " AND r.status IN ('PENDING', 'READY')")
    Optional<Reservation> findOpenReservationByMemberAndBook(
            @Param("member") Member member,
            @Param("book") Book book);

    /**
     * Returns all reservations for a book with PENDING status, ordered by reservation date.
     *
     * @param book the book
     * @return ordered list of pending reservations
     */
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(
            Book book, ReservationStatus status);

    /**
     * Finds all READY reservations that have passed their expiry date.
     *
     * @param today the current date
     * @return list of expired reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'READY' AND r.expiryDate < :today")
    List<Reservation> findExpiredReadyReservations(@Param("today") LocalDate today);

    /**
     * Counts open reservations for a member.
     *
     * @param member the member
     * @return number of open reservations
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.member = :member"
            + " AND r.status IN ('PENDING', 'READY')")
    long countOpenByMember(@Param("member") Member member);
}
