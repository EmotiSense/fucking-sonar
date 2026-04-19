package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.Category;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.entity.Reservation;
import com.bookvault.domain.enums.ReservationStatus;
import com.bookvault.dto.request.ReservationRequest;
import com.bookvault.dto.response.ReservationResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ReservationService}.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ReservationService reservationService;

    private Member activeMember;
    private Book fullyBorrowedBook;
    private Reservation pendingReservation;

    @BeforeEach
    void setUp() {
        activeMember = new Member("BV-202401-000001", "Alice", "Smith",
                "alice@example.com", "555-0100", "1 Main St");
        fullyBorrowedBook = new Book("978-0-06-112008-4", "Some Book",
                "Author", "Publisher", 2020, 1,
                new Category("Fiction", "Desc"));
        fullyBorrowedBook.decrementAvailable();
        pendingReservation = new Reservation(fullyBorrowedBook, activeMember, LocalDate.now());
    }

    @Test
    void findById_existing_shouldReturn() {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(pendingReservation));
        ReservationResponse result = reservationService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void findById_missing_shouldThrow() {
        given(reservationRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> reservationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void placeReservation_noExistingOpen_shouldCreate() {
        ReservationRequest request = buildRequest(1L, 1L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(1L)).willReturn(Optional.of(fullyBorrowedBook));
        given(reservationRepository.findOpenReservationByMemberAndBook(activeMember, fullyBorrowedBook))
                .willReturn(Optional.empty());
        given(reservationRepository.save(any(Reservation.class))).willReturn(pendingReservation);
        ReservationResponse result = reservationService.placeReservation(request);
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(result).isNotNull();
    }

    @Test
    void placeReservation_alreadyHasOpen_shouldThrow() {
        ReservationRequest request = buildRequest(1L, 1L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(1L)).willReturn(Optional.of(fullyBorrowedBook));
        given(reservationRepository.findOpenReservationByMemberAndBook(activeMember, fullyBorrowedBook))
                .willReturn(Optional.of(pendingReservation));
        assertThatThrownBy(() -> reservationService.placeReservation(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has an open reservation");
    }

    @Test
    void placeReservation_suspendedMember_shouldThrow() {
        activeMember.suspend();
        ReservationRequest request = buildRequest(1L, 1L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(1L)).willReturn(Optional.of(fullyBorrowedBook));
        assertThatThrownBy(() -> reservationService.placeReservation(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void cancelReservation_openReservation_shouldCancel() {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(pendingReservation));
        given(reservationRepository.save(any(Reservation.class))).willReturn(pendingReservation);
        reservationService.cancelReservation(1L);
        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelReservation_alreadyCancelled_shouldThrow() {
        pendingReservation.cancel();
        given(reservationRepository.findById(1L)).willReturn(Optional.of(pendingReservation));
        assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not open");
    }

    @Test
    void expireOutdatedReservations_shouldExpireAndReturnCount() {
        pendingReservation.markReady(LocalDate.now().minusDays(1));
        given(reservationRepository.findExpiredReadyReservations(any(LocalDate.class)))
                .willReturn(List.of(pendingReservation));
        given(reservationRepository.saveAll(any())).willReturn(List.of(pendingReservation));
        int count = reservationService.expireOutdatedReservations();
        assertThat(count).isEqualTo(1);
        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    void notifyNextReservation_pendingExists_shouldMarkReady() {
        given(reservationRepository.findByBookAndStatusOrderByReservationDateAsc(
                fullyBorrowedBook, ReservationStatus.PENDING))
                .willReturn(List.of(pendingReservation));
        given(reservationRepository.save(any(Reservation.class))).willReturn(pendingReservation);
        reservationService.notifyNextReservation(fullyBorrowedBook);
        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.READY);
    }

    @Test
    void notifyNextReservation_noPending_shouldDoNothing() {
        given(reservationRepository.findByBookAndStatusOrderByReservationDateAsc(
                fullyBorrowedBook, ReservationStatus.PENDING))
                .willReturn(Collections.emptyList());
        reservationService.notifyNextReservation(fullyBorrowedBook);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void findPendingByBook_shouldReturnPendingOnly() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(fullyBorrowedBook));
        given(reservationRepository.findByBookAndStatusOrderByReservationDateAsc(
                fullyBorrowedBook, ReservationStatus.PENDING))
                .willReturn(List.of(pendingReservation));
        List<ReservationResponse> result = reservationService.findPendingByBook(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ReservationStatus.PENDING);
    }

    private ReservationRequest buildRequest(Long memberId, Long bookId) {
        return new ReservationRequest(bookId, memberId);
    }
}
