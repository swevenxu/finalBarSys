package com.barbershop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {
    private static final String SALT = "barbershop-dsa-project";

    private PasswordUtil() { }

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

    public static boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        return hash(plainPassword).equalsIgnoreCase(storedHash.trim());
    }
}
