package com.barbershop.util;

import com.barbershop.model.User;

/**
 * Keeps track of who is logged in for the duration of the session.
 */
public final class Session {

    private static User currentUser;

    private Session() {
        // utility class
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getDisplayName() {
        if (currentUser == null) {
            return "Guest";
        }
        return currentUser.getFullName() == null || currentUser.getFullName().isBlank()
                ? currentUser.getUsername()
                : currentUser.getFullName();
    }
}
