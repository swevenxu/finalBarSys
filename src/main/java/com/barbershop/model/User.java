package com.barbershop.model;

public class User {
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_STAFF = "Staff";

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;

    public User() {
        this(0, "", "", "", ROLE_STAFF);
    }

    public User(String username, String passwordHash, String fullName, String role) {
        this(0, username, passwordHash, fullName, role);
    }

    public User(int userId, String username, String passwordHash, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return username;
    }
}
