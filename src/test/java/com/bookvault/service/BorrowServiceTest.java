package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Category;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.BorrowStatus;
import com.bookvault.dto.request.BorrowRequest;
import com.bookvault.dto.response.BorrowRecordResponse;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BookRepository;
import com.bookvault.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BorrowService}.
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private BorrowService borrowService;

    private Member activeMember;
    private Book availableBook;
    private BorrowRecord activeRecord;

    @BeforeEach
    void setUp() {
        activeMember = new Member("BV-202401-000001", "Alice", "Smith",
                "alice@example.com", "555-0100", "1 Main St");
        availableBook = new Book("978-0-06-112008-4", "To Kill a Mockingbird",
                "Harper Lee", "HarperCollins", 1960, 3,
                new Category("Fiction", "Fictional works"));
        activeRecord = new BorrowRecord(availableBook, activeMember,
                LocalDate.now(), LocalDate.now().plusDays(14));

        ReflectionTestUtils.setField(borrowService, "maxBorrowDays", 14);
        ReflectionTestUtils.setField(borrowService, "maxBooksPerMember", 5);
    }

    @Test
    void borrowBook_validRequest_shouldCreateRecord() {
        BorrowRequest request = buildBorrowRequest(1L, 1L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(1L)).willReturn(Optional.of(availableBook));
        given(borrowRecordRepository.countByMemberAndStatus(activeMember, BorrowStatus.ACTIVE))
                .willReturn(0L);
        given(borrowRecordRepository.findByMemberAndBookAndStatus(activeMember, availableBook, BorrowStatus.ACTIVE))
                .willReturn(Collections.emptyList());
        given(bookRepository.save(any(Book.class))).willReturn(availableBook);
        given(borrowRecordRepository.save(any(BorrowRecord.class))).willReturn(activeRecord);
        BorrowRecordResponse result = borrowService.borrowBook(request);
        assertThat(result).isNotNull();
        verify(bookRepository).save(availableBook);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_suspendedMember_shouldThrow() {
        activeMember.suspend();
        BorrowRequest request = buildBorrowRequest(1L, 1L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(1L)).willReturn(Optional.of(availableBook));
        assertThatThrownBy(() -> borrowService.borrowBook(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void borrowBook_noAvailableCopies_shouldThrow() {
        Book noStock = new Book("978-0", "Book", "Author", "Pub", 2020, 1,
                new Category("Cat", "Desc"));
        noStock.decrementAvailable();
        BorrowRequest request = buildBorrowRequest(1L, 2L);
        given(memberService.loadById(1L)).willReturn(activeMember);
        given(bookRepository.findById(2L)).willReturn(Optional.of(noStock));
        given(borrowRecordRepository.countByMemberAndStatus(activeMember, BorrowStatus.ACTIVE))
                .willReturn(0L);
        assertThatThrownBy(() -> borrowService.borrowBook(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no available copies");
    }

    @Test
    void returnBook_activeRecord_shouldMarkReturned() {
        // simulate one copy checked out so incrementAvailable() succeeds
        availableBook.decrementAvailable();
        given(borrowRecordRepository.findById(1L)).willReturn(Optional.of(activeRecord));
        given(bookRepository.save(any(Book.class))).willReturn(availableBook);
        given(borrowRecordRepository.save(any(BorrowRecord.class))).willReturn(activeRecord);
        BorrowRecordResponse result = borrowService.returnBook(1L);
        assertThat(result).isNotNull();
        verify(borrowRecordRepository).save(activeRecord);
    }

    @Test
    void returnBook_nonExistingRecord_shouldThrow() {
        given(borrowRecordRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> borrowService.returnBook(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findOverdue_shouldReturnOverdueList() {
        given(borrowRecordRepository.findOverdueRecords(any(LocalDate.class)))
                .willReturn(List.of(activeRecord));
        List<BorrowRecordResponse> result = borrowService.findOverdue();
        assertThat(result).hasSize(1);
    }

    @Test
    void markOverdueRecords_shouldUpdateAndReturnCount() {
        given(borrowRecordRepository.findOverdueRecords(any(LocalDate.class)))
                .willReturn(List.of(activeRecord));
        given(borrowRecordRepository.saveAll(any())).willReturn(List.of(activeRecord));
        int count = borrowService.markOverdueRecords();
        assertThat(count).isEqualTo(1);
    }

    private BorrowRequest buildBorrowRequest(Long memberId, Long bookId) {
        BorrowRequest req = new BorrowRequest();
        req.setMemberId(memberId);
        req.setBookId(bookId);
        return req;
    }
}
