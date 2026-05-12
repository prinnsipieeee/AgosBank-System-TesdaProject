package com.agosbank.main;

/**
 * UserSession serves as a global state for the logged-in user.
 * Updated to support Account Details view.
 */
public class UserSession {
    private static UserSession instance;
    
    private String fullName;
    private String accountId;
    private String phoneNumber; // Dagdag para sa Account Details
    private String email;       // Dagdag para sa Account Details
    private String memberSince; // Dagdag para sa Account Details
    private double balance; 

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // --- Getters and Setters ---

    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setAccountId(String id) {
        this.accountId = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setBalance(double currentBalance) {
        this.balance = currentBalance;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Resets the session data upon logout.
     * Mahalaga ito para walang maiwang data sa susunod na mag-login.
     */
    public void cleanUserSession() {
        fullName = null;
        accountId = null;
        phoneNumber = null;
        email = null;
        memberSince = null;
        balance = 0.0;
    }
}