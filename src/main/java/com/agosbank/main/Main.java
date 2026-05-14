package com.agosbank.main;

import java.util.Scanner;

import com.agosbank.models.User;
import com.agosbank.services.AuthService;
import com.agosbank.services.TransactionService;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final AuthService as = new AuthService();
    private static final TransactionService ts = new TransactionService();

    public static void main(String[] args) {
        while (true) {
            if (UserSession.getInstance().getAccountId() == null) {
                showWelcomeMenu();
            } else {
                showDashboardMenu();
            }
        }
    }

    private static void showWelcomeMenu() {
        System.out.println("\n==================================");
        System.out.println("        AGOSBANK MOBILE       ");
        System.out.println("==================================");
        System.out.println("[1] Login");
        System.out.println("[2] Register");
        System.out.println("[3] Exit");
        System.out.print("\nChoose an option: ");
        
        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> handleRegister();
            case "3" -> {
                System.out.println("Thank you for using AgosBank. Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid option. Try again.");
        }
    }

    private static void showDashboardMenu() {
        System.out.println("\n==================================");
        System.out.println("        AGOSBANK DASHBOARD");
        System.out.println("    Welcome, " + UserSession.getInstance().getFullName());
        System.out.println("==================================");
        System.out.println("Account ID:      " + UserSession.getInstance().getAccountId());
        System.out.println("Current Balance: ₱" + String.format("%,.2f", UserSession.getInstance().getBalance()));
        System.out.println("----------------------------------");
        System.out.println("[1] Cash In");
        System.out.println("[2] Send Money");
        System.out.println("[3] Withdraw");
        System.out.println("[4] Transaction History");
        System.out.println("[5] Logout");
        System.out.print("\nChoose an option: ");

        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> handleCashIn();
            case "2" -> handleSendMoney();
            case "3" -> handleWithdraw();
            case "4" -> {
                System.out.println("Transaction history feature is currently unavailable.");
            }
            case "5" -> {
                UserSession.getInstance().cleanUserSession();
                System.out.println("Logged out successfully.");
            }
            default -> System.out.println("Option not available yet.");
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();
        System.out.print("Enter 4-digit PIN: ");
        String pin = sc.nextLine();

        User user = as.loginUser(phone, pin);

        if (user != null) {
            UserSession.getInstance().setAccountId(user.getAccountId());
            UserSession.getInstance().setFullName(user.getFullName());
            UserSession.getInstance().setBalance(user.getBalance()); 
            
            System.out.println("Login Successful! Welcome back, " + UserSession.getInstance().getFullName());
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private static void handleRegister() {
        System.out.println("\n--- CREATE ACCOUNT ---");
        System.out.print("Full Name: ");
        String name = sc.nextLine();
        System.out.print("Phone Number: ");
        String phone = sc.nextLine();
        System.out.print("Email Address: ");
        String email = sc.nextLine();
        System.out.print("Create 4-digit PIN: ");
        String pin = sc.nextLine();
        
        String accId = "AGOS-" + (int)(Math.random() * 9000 + 1000);

        boolean success = as.registerUser(name, accId, phone, email, pin);

        if (success) {
            System.out.println("Account Created! Your ID is: " + accId);
        }
    }

    private static void handleCashIn() {
        System.out.print("\nEnter Recipient Account ID: ");
        String accountId = sc.nextLine();

        System.out.print("Enter amount to deposit: ");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Enter Deposit Source (Fullname): ");
        String sourceName = sc.nextLine();

        boolean success = ts.deposit(accountId, amount, sourceName);

        if (success) {
            if (accountId.equals(UserSession.getInstance().getAccountId())) {
                UserSession.getInstance().setBalance(UserSession.getInstance().getBalance() + amount);
            }
            System.out.println("Cash In Successful! New Balance: ₱" + String.format("%,.2f", UserSession.getInstance().getBalance()));
        } else {
            System.out.println("Cash In Failed. Please check the Account ID.");
        }
    }

    private static void handleSendMoney() {
        System.out.println("\n--- SEND MONEY ---");
        System.out.print("Enter Receiver Account ID: ");
        String receiverId = sc.nextLine();
        
        System.out.print("Enter amount to send: ");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0 || amount > UserSession.getInstance().getBalance()) {
            System.out.println("Invalid amount or insufficient balance.");
            return;
        }

        boolean success = ts.sendMoney(UserSession.getInstance().getAccountId(), receiverId, amount);

        if (success) {
            UserSession.getInstance().setBalance(UserSession.getInstance().getBalance() - amount);
            System.out.println("Transfer Successful! New Balance: ₱" + String.format("%,.2f", UserSession.getInstance().getBalance()));
        } else {
            System.out.println("Transfer Failed. Check receiver ID or your balance.");
        }
    }

    private static void handleWithdraw() {
        System.out.println("\n--- 🏧 WITHDRAW ---");
        System.out.print("Enter amount to withdraw: ₱");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0 || amount > UserSession.getInstance().getBalance()) {
            System.out.println("❌ Invalid amount or insufficient balance.");
            return;
        }

        boolean success = ts.withdraw(UserSession.getInstance().getAccountId(), amount, UserSession.getInstance().getFullName());

        if (success) {
            UserSession.getInstance().setBalance(UserSession.getInstance().getBalance() - amount);
            System.out.println("✅ Please take your cash. New Balance: ₱" + String.format("%,.2f", UserSession.getInstance().getBalance()));
        } else {
            System.out.println("❌ Withdraw Failed.");
        }
    }
}