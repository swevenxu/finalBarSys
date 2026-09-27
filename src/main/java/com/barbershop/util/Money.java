package com.barbershop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats and rounds peso amounts (₱).
 */
public final class Money {

    private static final char PESO = '\u20B1';

    private Money() {
        // utility class
    }

    /** Formats an amount as {@code ₱1,234.50}. */
    public static String format(double amount) {
        return PESO + String.format("%,.2f", amount);
    }

    /** Rounds to two decimal places, which is what gets stored in SQLite. */
    public static double round(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Parses user input such as {@code 150}, {@code 150.5} or {@code ₱150}.
     *
     * @throws NumberFormatException when the text is not a valid amount
     */
    public static double parse(String text) {
        if (text == null) {
            throw new NumberFormatException("Amount is required.");
        }
        String cleaned = text.replace(String.valueOf(PESO), "")
                .replace(",", "")
                .trim();
        if (cleaned.isEmpty()) {
            throw new NumberFormatException("Amount is required.");
        }
        return round(Double.parseDouble(cleaned));
    }
}
