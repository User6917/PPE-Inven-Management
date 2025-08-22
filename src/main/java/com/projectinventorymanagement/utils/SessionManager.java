package com.projectinventorymanagement.utils;

import com.projectinventorymanagement.models.User;

public class SessionManager {
    private static User currentUser; // Global storage for logged-in user

    static {
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            clearSession();
            // System.out.println("Session cleared on shutdown.");
        }));
    }

    // Set (store) the user in session
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get (retrieve) the user from session
    public static User getCurrentUser() {
        // System.out.println(currentUser);
        return currentUser;
    }

    // Optional: Clear user session
    public static void clearSession() {
        currentUser = null;
    }
}
