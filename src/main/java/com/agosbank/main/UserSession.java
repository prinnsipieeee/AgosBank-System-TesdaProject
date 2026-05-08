package com.agosbank.main;

/**
 * UserSession serves as a global state for the logged-in user.
 * Singleton pattern implementation for AgosBank.
 */
public class UserSession {
    private static UserSession instance;
    
    // Tinanggal ang 'static' dito para maging pag-aari sila ng instance
    private String fullName;
    private String accountId;
    private double balance; 

    // Private constructor: Hindi pwedeng i-new sa ibang class
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Instance getters and setters (tinanggal ang static keyword)
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

    public void setBalance(double currentBalance) {
        this.balance = currentBalance;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Resets the session data upon logout.
     */
    public void cleanUserSession() {
        fullName = null;
        accountId = null;
        balance = 0.0;
    }
}