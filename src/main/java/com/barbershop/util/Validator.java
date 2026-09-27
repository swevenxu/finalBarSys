package com.barbershop.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-() ]{7,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("H:mm");

    private Validator() { }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    public static boolean isOptionalEmail(String value) {
        return isBlank(value) || EMAIL.matcher(value.trim()).matches();
    }

    public static LocalTime parseTime(String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Time is required (format HH:mm, e.g. 10:30).");
        }
        String cleaned = text.trim();
        try {
            return LocalTime.parse(cleaned, TIME_INPUT);
        } catch (DateTimeParseException first) {
            try {
                return LocalTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeParseException second) {
                throw new IllegalArgumentException(
                        "Invalid time \"" + text + "\". Use 24-hour HH:mm, e.g. 10:30 or 14:15.");
            }
        }
    }

    public static String normalizeEmail(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
