package com.barbershop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private static final char PESO = '\u20B1';

    private Money() { }

    public static String format(double amount) {
        return PESO + String.format("%,.2f", amount);
    }

    public static double round(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

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
