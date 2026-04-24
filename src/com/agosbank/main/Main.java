package com.agosbank.main;

import com.agosbank.models.User;
import com.agosbank.services.AuthService;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService = new AuthService();
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
        
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                System.out.println("Thank you for using AgosBank. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Try again.");
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
        System.out.println("[3] Transaction History");
        System.out.println("[4] Notifications");
        System.out.println("[5] Logout");
        System.out.print("\nChoose an option: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.println("Feature coming soon: Cash In");
                break;
            case "2":
                System.out.println("Feature coming soon: Send Money");
                break;
            case "5":
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("ption not available yet.");
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter 4-digit PIN: ");
        String pin = scanner.nextLine();

        currentUser = authService.loginUser(phone, pin);

        if (currentUser != null) {
            System.out.println("Login Successful!");
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private static void handleRegister() {
        System.out.println("\n--- CREATE ACCOUNT ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();
        System.out.print("Create 4-digit PIN: ");
        String pin = scanner.nextLine();
        
        // Randomly generate an Account ID (Agos Style)
        String accId = "AGOS-" + (int)(Math.random() * 9000 + 1000);

        boolean success = authService.registerUser(name, accId, phone, pin);

        if (success) {
            System.out.println("Account Created! Your ID is: " + accId);
        } else {
            System.out.println("Registration failed. Make sure PIN is 4 digits.");
        }
    }
}