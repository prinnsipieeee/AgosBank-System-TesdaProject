package com.agosbank.main;

import java.util.Scanner;

import com.agosbank.models.User;
import com.agosbank.services.AuthService;
import com.agosbank.services.TransactionService;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final AuthService as = new AuthService();
    private static final  TransactionService ts = new TransactionService();
    private static User currentUser = null;

    public static void main(String[] args) {
        while(true){
            if(currentUser == null){
                showWelcomeMenu();
            } else {
                showDashboardMenu();
            }
        }
    }

    private static void showWelcomeMenu(){
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
        System.out.println("    Welcome, " + currentUser.getFullName());
        System.out.println("==================================");
        System.out.println("Account ID: " + currentUser.getAccountId());
        System.out.println("Current Balance: ₱" + currentUser.getBalance());
        System.out.println("----------------------------------");
        System.out.println("[1] Cash In");
        System.out.println("[2] Send Money");
        System.out.println("[3] Withdraw");
        System.out.println("[4] Transaction History");
        System.out.println("[5] Logout");
        System.out.print("\nChoose an option: ");

        String choice = sc.nextLine();

        switch (choice) {
            case "1" -> handleCashin();
            case "2" -> handleSendMoney();
            case "3" -> handleWithdraw();
            case "4" -> ts.showHistory(currentUser.getId());
            case "5" -> {
                currentUser = null;
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

        currentUser = (User) as.loginUser(phone, pin);

        if (currentUser != null) {
            System.out.println("Login Successful!");
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
        
        // Randomly generate an Account ID (Agos Style)
        String accId = "AGOS-" + (int)(Math.random() * 9000 + 1000);

        boolean success = as.registerUser(name, accId, phone, email, pin);

        if (success) {
            System.out.println("Account Created! Your ID is: " + accId);
        }
    }

    private static void handleCashin() {
        System.out.println("\n--- CASH IN ---");
        System.out.print("Enter amount to deposit: ");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        boolean success = ts.deposit(currentUser.getId(), amount);

        if (success) {
            // I-update natin ang balance ng currentUser object para reflect agad sa UI
            currentUser.setBalance(currentUser.getBalance() + amount);
            System.out.println("Cash In Successful! New Balance: ₱" + currentUser.getBalance());
        } else {
            System.out.println("Cash In Failed. Please try again.");
        }
    }

    private static void handleSendMoney() {
        System.out.println("\n--- SEND MONEY ---");
        System.out.print("Enter Receiver Account ID (e.g., AGOS-1234): ");
        String receiverId = sc.nextLine();
        
        System.out.print("Enter amount to send: ");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        boolean success = ts.sendMoney(currentUser.getId(), receiverId, amount);

        if (success) {
            // I-update ang balance sa local object para reflect agad sa UI
            currentUser.setBalance(currentUser.getBalance() - amount);
            System.out.println("Transfer Successful! Your new balance: " + currentUser.getBalance());
        } else {
            System.out.println("Transfer Failed. Check receiver ID or your balance.");
        }
    }

    private static void handleWithdraw() {
        System.out.println("\n--- 🏧 WITHDRAW ---");
        System.out.print("Enter amount to withdraw: ₱");
        double amount = Double.parseDouble(sc.nextLine());

        if (amount <= 0) {
            System.out.println("❌ Invalid amount.");
            return;
        }

        boolean success = ts.withdraw(currentUser.getId(), amount);

        if (success) {
            currentUser.setBalance(currentUser.getBalance() - amount);
            System.out.println("✅ Please take your cash. New Balance: ₱" + currentUser.getBalance());
        } else {
            System.out.println("❌ Withdraw Failed. Check your balance.");
        }
    }
}