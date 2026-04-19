package com.bookvault.service;

import com.bookvault.domain.entity.Book;
import com.bookvault.domain.entity.BorrowRecord;
import com.bookvault.domain.entity.Category;
import com.bookvault.domain.entity.Fine;
import com.bookvault.domain.entity.Member;
import com.bookvault.domain.enums.FineStatus;
import com.bookvault.exception.BusinessRuleException;
import com.bookvault.exception.ResourceNotFoundException;
import com.bookvault.repository.BorrowRecordRepository;
import com.bookvault.repository.FineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link FineService}.
 */
@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private FineService fineService;

    private Member member;
    private Book book;
    private BorrowRecord lateBorrowRecord;
    private Fine outstandingFine;

    @BeforeEach
    void setUp() {
        member = new Member("BV-202401-000001", "Alice", "Smith",
                "alice@example.com", "555-0100", "1 Main St");
        book = new Book("978-0-06-112008-4", "To Kill a Mockingbird",
                "Harper Lee", "HarperCollins", 1960, 3,
                new Category("Fiction", "Desc"));
        lateBorrowRecord = new BorrowRecord(book, member,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(6));
        lateBorrowRecord.recordReturn(LocalDate.now());

        outstandingFine = new Fine(lateBorrowRecord, member,
                new BigDecimal("6.00"), "Late return — 6 days overdue");

        ReflectionTestUtils.setField(fineService, "finePerDay", new BigDecimal("1.00"));
    }

    @Test
    void findById_existing_shouldReturnFine() {
        given(fineRepository.findById(1L)).willReturn(Optional.of(outstandingFine));
        var result = fineService.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("6.00"));
    }

    @Test
    void findById_missing_shouldThrowNotFound() {
        given(fineRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> fineService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void issueFine_lateRecord_shouldCreateFine() {
        given(borrowRecordRepository.findById(1L)).willReturn(Optional.of(lateBorrowRecord));
        given(fineRepository.findByBorrowRecord(lateBorrowRecord)).willReturn(Optional.empty());
        given(fineRepository.save(any(Fine.class))).willReturn(outstandingFine);
        var result = fineService.issueFine(1L);
        verify(fineRepository).save(any(Fine.class));
        assertThat(result).isNotNull();
    }

    @Test
    void issueFine_alreadyHasFine_shouldThrow() {
        given(borrowRecordRepository.findById(1L)).willReturn(Optional.of(lateBorrowRecord));
        given(fineRepository.findByBorrowRecord(lateBorrowRecord))
                .willReturn(Optional.of(outstandingFine));
        assertThatThrownBy(() -> fineService.issueFine(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void payFine_outstanding_shouldMarkPaid() {
        given(fineRepository.findById(1L)).willReturn(Optional.of(outstandingFine));
        given(fineRepository.save(any(Fine.class))).willReturn(outstandingFine);
        fineService.payFine(1L);
        assertThat(outstandingFine.getStatus()).isEqualTo(FineStatus.PAID);
        verify(fineRepository).save(outstandingFine);
    }

    @Test
    void payFine_alreadyPaid_shouldThrow() {
        outstandingFine.markPaid(LocalDate.now());
        given(fineRepository.findById(1L)).willReturn(Optional.of(outstandingFine));
        assertThatThrownBy(() -> fineService.payFine(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not outstanding");
    }

    @Test
    void waiveFine_outstanding_shouldMarkWaived() {
        given(fineRepository.findById(1L)).willReturn(Optional.of(outstandingFine));
        given(fineRepository.save(any(Fine.class))).willReturn(outstandingFine);
        fineService.waiveFine(1L);
        assertThat(outstandingFine.getStatus()).isEqualTo(FineStatus.WAIVED);
    }

    @Test
    void getTotalOutstanding_shouldReturnSumFromRepo() {
        given(memberService.loadById(1L)).willReturn(member);
        given(fineRepository.sumOutstandingByMember(member))
                .willReturn(new BigDecimal("15.00"));
        BigDecimal total = fineService.getTotalOutstanding(1L);
        assertThat(total).isEqualByComparingTo(new BigDecimal("15.00"));
    }
}
