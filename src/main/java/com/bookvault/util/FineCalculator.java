package com.bookvault.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Stateless utility for calculating library late-return fines.
 * <p>
 * The daily rate is configurable; the default matches the library policy
 * defined in {@code application.yml} ({@code library.borrow.fine-per-day}).
 * </p>
 */
public final class FineCalculator {

    /** Default fine rate applied per overdue day. */
    private static final BigDecimal DEFAULT_DAILY_RATE = new BigDecimal("1.00");

    /** Scale for monetary values (2 decimal places). */
    private static final int MONETARY_SCALE = 2;

    private FineCalculator() {
        // utility class — do not instantiate
    }

    /**
     * Calculates the fine for a late return using the default daily rate.
     *
     * @param dueDate    the date the book was due
     * @param returnDate the actual date the book was returned
     * @return the fine amount, or {@link BigDecimal#ZERO} if not overdue
     */
    public static BigDecimal calculate(LocalDate dueDate, LocalDate returnDate) {
        return calculate(dueDate, returnDate, DEFAULT_DAILY_RATE);
    }

    /**
     * Calculates the fine for a late return using a custom daily rate.
     *
     * @param dueDate    the date the book was due
     * @param returnDate the actual date the book was returned
     * @param dailyRate  the penalty per overdue day
     * @return the fine amount, or {@link BigDecimal#ZERO} if not overdue
     * @throws IllegalArgumentException if dailyRate is negative
     */
    public static BigDecimal calculate(LocalDate dueDate, LocalDate returnDate, BigDecimal dailyRate) {
        validateRate(dailyRate);
        long overdueDays = computeOverdueDays(dueDate, returnDate);
        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }
        return dailyRate
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Returns the number of days a book is overdue.
     * Returns 0 when the book was returned on time or before the due date.
     *
     * @param dueDate    the due date
     * @param returnDate the actual return date
     * @return overdue days (non-negative)
     */
    public static long computeOverdueDays(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(dueDate, returnDate);
    }

    /**
     * Computes the fine that would currently apply if a book were returned today.
     *
     * @param dueDate the due date of the borrow record
     * @return the projected fine amount as of today
     */
    public static BigDecimal projectCurrentFine(LocalDate dueDate) {
        return calculate(dueDate, LocalDate.now(), DEFAULT_DAILY_RATE);
    }

    private static void validateRate(BigDecimal dailyRate) {
        if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily rate must be non-negative");
        }
    }
}
