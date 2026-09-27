package com.barbershop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes passwords with SHA-256 so that the {@code users} table never stores plain text.
 *
 * <p>A fixed application salt keeps the stored hashes from being trivially reversible through
 * a rainbow table. This is proportionate for a small desktop project; a production system would
 * use a per-user salt and a slow KDF such as bcrypt.</p>
 */
public final class PasswordUtil {

    private static final String SALT = "barbershop-dsa-project";

    private PasswordUtil() {
        // utility class
    }

    /** Returns the hex-encoded SHA-256 hash of {@code salt + password}. */
    public static String hash(String password) {
        if (password == null) {
            password = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM.", e);
        }
    }

    /** Constant-time-ish comparison used when checking a login. */
    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        return hash(plainPassword).equalsIgnoreCase(storedHash.trim());
    }
}
