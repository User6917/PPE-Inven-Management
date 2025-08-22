package com.projectinventorymanagement.models;

import java.time.LocalDateTime;
import org.mindrot.jbcrypt.BCrypt;

public class User {
    private int userID;
    protected String name; 
    protected String username;
    private String password;
    private String userType;
    protected String email;
    protected String phone;
    private LocalDateTime createdAt;
    private boolean isActive;

    // Constructor for New Users (Password is Hashed)
    public User(String name, String username, String password, String userType, String email, 
                String phone, boolean isActive) {
        this.name = name;
        this.username = username;
        this.password = hashPassword(password);
        this.userType = userType;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for Loading Users from File (Password is Already Hashed)
    public User(int userID, String name, String username, String hashedPassword, String userType, 
                String email, String phone, boolean isActive) {
        this.userID = userID;
        this.name = name;
        this.username = username;
        this.password = hashedPassword; // Already hashed
        this.userType = userType;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for Users (Extends into Suppliers)
    public User(String name, String username, String email, String phone) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    // Password Hashing using BCrypt
    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Verify Password
    public boolean checkPassword(String inputPassword) {
        return BCrypt.checkpw(inputPassword, this.password);
    }

    // Getters
    public int getUserID() { return userID; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() {return password; }
    public String getUserType() { return userType; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }


    // Setter for User ID (Used in UserDatabase)
    public void setUserID(int userID) {
        this.userID = userID;
    }

    // Setters
    public void setActive(boolean isActive) { this.isActive = isActive; }

    // Improved toString() Method (No Password Exposure)
    @Override
    public String toString() {
        return "User ID: " + userID + " | Name: " + name + " | Username: " + username +
               " | User Type: " + userType + " | Email: " + email + " | Phone: " + phone + 
               " | Created At: " + createdAt + " | Status: " + (isActive ? "Active" : "Inactive");
    }

    // Display Debug Info
    public void display() {
        System.out.println(this.toString());
    }
}
