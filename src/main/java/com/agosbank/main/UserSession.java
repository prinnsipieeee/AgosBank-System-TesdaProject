package com.agosbank.main;

/**
 * UserSession serves as a global state for the logged-in user.
 * It stores the user's name, account ID, and current balance.
 */

public class UserSession {
    private static String fullName;
    private static String accountId;
    private static double balance; 

    public static void setFullName(String name) {
        fullName = name;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void setAccountId(String id) {
        accountId = id;
    }

    public static String getAccountId() {
        return accountId;
    }

    // New Getter and Setter for Balance
    public static void setBalance(double currentBalance) {
        balance = currentBalance;
    }

    public static double getBalance() {
        return balance;
    }

    /**
     * Resets the session data upon logout.
     */
    public static void cleanUserSession() {
        fullName = null;
        accountId = null;
        balance = 0.0;
    }
}