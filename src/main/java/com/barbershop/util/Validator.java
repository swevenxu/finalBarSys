package com.barbershop.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Input validation helpers shared by the controllers.
 */
public final class Validator {

    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-() ]{7,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final DateTimeFormatter TIME_INPUT = DateTimeFormatter.ofPattern("H:mm");

    private Validator() {
        // utility class
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE.matcher(value.trim()).matches();
    }

    /** Email is optional, but if present it must look like an address. */
    public static boolean isOptionalEmail(String value) {
        return isBlank(value) || EMAIL.matcher(value.trim()).matches();
    }

    /**
     * Parses a time typed as {@code H:mm} or {@code HH:mm}.
     *
     * @throws IllegalArgumentException with a readable message when the text is invalid
     */
    public static LocalTime parseTime(String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Time is required (format HH:mm, e.g. 10:30).");
        }
        String cleaned = text.trim();
        try {
            return LocalTime.parse(cleaned, TIME_INPUT);
        } catch (DateTimeParseException first) {
            try {
                // Fall back to the fully padded form, for example "09:05".
                return LocalTime.parse(cleaned, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeParseException second) {
                throw new IllegalArgumentException(
                        "Invalid time \"" + text + "\". Use 24-hour HH:mm, e.g. 10:30 or 14:15.");
            }
        }
    }

    /** Formats an optional email for storage (empty string becomes {@code null}). */
    public static String normalizeEmail(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
