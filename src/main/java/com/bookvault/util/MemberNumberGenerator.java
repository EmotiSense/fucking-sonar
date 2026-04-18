package com.bookvault.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique library card numbers for new members.
 * <p>
 * The format is: {@code BV-YYYYMM-NNNNNN} where {@code YYYYMM} is the
 * registration year-month and {@code NNNNNN} is a zero-padded sequence number.
 * </p>
 * <p>
 * In a production system the sequence counter would be persisted to a database
 * sequence to survive application restarts.
 * </p>
 */
public final class MemberNumberGenerator {

    private static final String PREFIX = "BV";
    private static final String SEPARATOR = "-";
    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM");
    private static final int SEQUENCE_WIDTH = 6;

    private static final AtomicLong sequence = new AtomicLong(1L);

    private MemberNumberGenerator() {
        // utility class — do not instantiate
    }

    /**
     * Generates the next unique member number.
     *
     * @return a member number string in the format {@code BV-YYYYMM-NNNNNN}
     */
    public static String generate() {
        String monthPart = LocalDate.now().format(MONTH_FORMATTER);
        String seqPart = String.format("%0" + SEQUENCE_WIDTH + "d", sequence.getAndIncrement());
        return PREFIX + SEPARATOR + monthPart + SEPARATOR + seqPart;
    }

    /**
     * Resets the internal sequence counter. Intended for use in tests only.
     *
     * @param value the value to reset the counter to
     */
    static void resetSequence(long value) {
        sequence.set(value);
    }
}
