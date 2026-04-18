package com.bookvault.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FineCalculator}.
 */
class FineCalculatorTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2024, 1, 10);

    @Test
    void returnedOnDueDate_shouldReturnZeroFine() {
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, DUE_DATE);
        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void returnedBeforeDueDate_shouldReturnZeroFine() {
        LocalDate earlyReturn = DUE_DATE.minusDays(3);
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, earlyReturn);
        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void returnedOneDayLate_shouldReturnDefaultDailyRate() {
        LocalDate returnDate = DUE_DATE.plusDays(1);
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, returnDate);
        assertThat(fine).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void returnedTenDaysLate_shouldReturnTenTimesDefault() {
        LocalDate returnDate = DUE_DATE.plusDays(10);
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, returnDate);
        assertThat(fine).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2.50, 2.50",
            "5, 0.50, 2.50",
            "7, 1.00, 7.00",
            "3, 2.00, 6.00"
    })
    void calculateWithCustomRate(long overdueDays, String rateStr, String expectedStr) {
        LocalDate returnDate = DUE_DATE.plusDays(overdueDays);
        BigDecimal rate = new BigDecimal(rateStr);
        BigDecimal expected = new BigDecimal(expectedStr);
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, returnDate, rate);
        assertThat(fine).isEqualByComparingTo(expected);
    }

    @Test
    void nullReturnDate_shouldReturnZeroFine() {
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, null);
        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void negativeRate_shouldThrowException() {
        LocalDate lateReturn = DUE_DATE.plusDays(1);
        BigDecimal negativeRate = new BigDecimal("-1.00");
        assertThatThrownBy(() -> FineCalculator.calculate(DUE_DATE, lateReturn, negativeRate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void zeroRate_shouldReturnZeroFine() {
        LocalDate returnDate = DUE_DATE.plusDays(5);
        BigDecimal fine = FineCalculator.calculate(DUE_DATE, returnDate, BigDecimal.ZERO);
        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void computeOverdueDays_onTimeReturn_shouldReturnZero() {
        long days = FineCalculator.computeOverdueDays(DUE_DATE, DUE_DATE);
        assertThat(days).isZero();
    }

    @Test
    void computeOverdueDays_lateReturn_shouldReturnPositive() {
        long days = FineCalculator.computeOverdueDays(DUE_DATE, DUE_DATE.plusDays(5));
        assertThat(days).isEqualTo(5L);
    }

    @Test
    void computeOverdueDays_nullReturn_shouldReturnZero() {
        long days = FineCalculator.computeOverdueDays(DUE_DATE, null);
        assertThat(days).isZero();
    }
}
