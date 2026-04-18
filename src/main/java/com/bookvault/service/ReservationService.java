package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.entity.Reservation;
import com.bookvault.domain.enums.ReservationStatus;
import com.bookvault.dto.request.ReservationRequest;
import com.bookvault.dto.response.PageResponse;
import com.bookvault.dto.response.ReservationResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for managing book reservations.
 */
@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final String RESERVATION_ENTITY = "Reservation";
    private static final String BOOK_ENTITY = "Book";

    @Value("${library.reservation.expiry-days:3}")
    private int reservationExpiryDays;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final MemberService memberService;

    /**
     * Constructs the service with its required dependencies.
     *
     * @param reservationRepository the reservation data store
     * @param bookRepository        the book data store
     * @param memberService         the member service
     */
    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.memberService = memberService;
    }

    /**
     * Returns a paginated list of all reservations.
     *
     * @param pageable pagination parameters
     * @return paged reservation responses
     */
    public PageResponse<ReservationResponse> findAll(Pageable pageable) {
        Page<ReservationResponse> page = reservationRepository.findAll(pageable)
                .map(ReservationResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns a single reservation by its ID.
     *
     * @param id the reservation ID
     * @return the reservation response
     * @throws ResourceNotFoundException if no reservation exists with the given ID
     */
    public ReservationResponse findById(Long id) {
        return ReservationResponse.from(loadById(id));
    }

    /**
     * Returns paginated reservations for a specific member.
     *
     * @param memberId the member ID
     * @param pageable pagination parameters
     * @return paged reservation responses
     */
    public PageResponse<ReservationResponse> findByMember(Long memberId, Pageable pageable) {
        Member member = memberService.loadById(memberId);
        Page<ReservationResponse> page = reservationRepository
                .findByMember(member, pageable)
                .map(ReservationResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Returns paginated reservations for a specific book.
     *
     * @param bookId   the book ID
     * @param pageable pagination parameters
     * @return paged reservation responses
     */
    public PageResponse<ReservationResponse> findByBook(Long bookId, Pageable pageable) {
        Book book = loadBook(bookId);
        Page<ReservationResponse> page = reservationRepository
                .findByBook(book, pageable)
                .map(ReservationResponse::from);
        return PageResponse.from(page);
    }

    /**
     * Places a reservation for a book.
     *
     * @param request the reservation request
     * @return the created reservation response
     * @throws BusinessRuleException     if the member already has an open reservation for the book
     * @throws ResourceNotFoundException if the book or member does not exist
     */
    @Transactional
    public ReservationResponse placeReservation(ReservationRequest request) {
        Member member = memberService.loadById(request.getMemberId());
        Book book = loadBook(request.getBookId());
        assertMemberCanBorrow(member);
        assertNoOpenReservation(member, book);
        Reservation reservation = new Reservation(book, member, LocalDate.now());
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    /**
     * Cancels a reservation.
     *
     * @param reservationId the reservation ID
     * @return the updated reservation response
     * @throws ResourceNotFoundException if no reservation exists with the given ID
     * @throws BusinessRuleException     if the reservation is not open
     */
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = loadById(reservationId);
        assertReservationIsOpen(reservation);
        reservation.cancel();
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    /**
     * Marks the first pending reservation for a book as READY.
     * Called when a returned book is checked back in.
     *
     * @param book the book that became available
     */
    @Transactional
    public void notifyNextReservation(Book book) {
        List<Reservation> pending = reservationRepository
                .findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING);
        if (!pending.isEmpty()) {
            Reservation next = pending.get(0);
            next.markReady(LocalDate.now().plusDays(reservationExpiryDays));
            reservationRepository.save(next);
        }
    }

    /**
     * Expires all READY reservations whose expiry date has passed.
     *
     * @return the number of reservations expired
     */
    @Transactional
    public int expireOutdatedReservations() {
        List<Reservation> expired =
                reservationRepository.findExpiredReadyReservations(LocalDate.now());
        expired.forEach(Reservation::expire);
        reservationRepository.saveAll(expired);
        return expired.size();
    }

    /**
     * Returns all pending reservations for a book, ordered by date.
     *
     * @param bookId the book ID
     * @return ordered list of pending reservation responses
     */
    public List<ReservationResponse> findPendingByBook(Long bookId) {
        Book book = loadBook(bookId);
        return reservationRepository
                .findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Reservation loadById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESERVATION_ENTITY, id));
    }

    private Book loadBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(BOOK_ENTITY, bookId));
    }

    private void assertMemberCanBorrow(Member member) {
        if (!member.canBorrow()) {
            throw new BusinessRuleException(
                    "Member " + member.getMemberNumber() + " is not allowed to place reservations");
        }
    }

    private void assertNoOpenReservation(Member member, Book book) {
        reservationRepository.findOpenReservationByMemberAndBook(member, book)
                .ifPresent(existing -> {
                    throw new BusinessRuleException(
                            "Member " + member.getMemberNumber()
                                    + " already has an open reservation for book '"
                                    + book.getTitle() + "'");
                });
    }

    private void assertReservationIsOpen(Reservation reservation) {
        if (!reservation.isOpen()) {
            throw new BusinessRuleException(
                    "Reservation " + reservation.getId() + " is not open");
        }
    }
}
