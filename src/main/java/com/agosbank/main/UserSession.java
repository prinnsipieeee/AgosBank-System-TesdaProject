package com.agosbank.main;

public class UserSession {
    private static UserSession instance;
    
    private String fullName;
    private String accountId;
    private String phoneNumber;
    private String email;       
    private String memberSince; 
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

    public void cleanUserSession() {
        fullName = null;
        accountId = null;
        phoneNumber = null;
        email = null;
        memberSince = null;
        balance = 0.0;
    }
}