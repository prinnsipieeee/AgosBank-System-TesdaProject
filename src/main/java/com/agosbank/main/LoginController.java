package com.agosbank.main;

import com.agosbank.models.User;
import com.agosbank.services.AuthService; // I-import natin para malinis tingnan

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private AuthService authService = new AuthService();
    
    @FXML
    public void initialize() {
        // Ito ay tatakbo kapag nag-load ang window.
        System.out.println("DEBUG: LoginController initialized!");
    }

    @FXML
    private void handleLogin() {
        // CHECK 1: Kung null ba ang mga fields (Wiring check)
        if (usernameField == null || passwordField == null) {
            System.err.println("CRITICAL ERROR: UI Fields are NULL! Check fx:id in Scene Builder.");
            return;
        }

        String user = usernameField.getText();
        String pass = passwordField.getText();

        System.out.println("DEBUG: Attempting login for: " + user);

        try {
            // CHECK 2: Tawagin ang AuthService
            User loggedInUser = authService.loginUser(user, pass);

            if (loggedInUser != null) {
                System.out.println("SUCCESS: Welcome, " + loggedInUser.getFullName());
                // TODO: Transition to Dashboard
            } else {
                System.out.println("FAILED: Invalid Phone Number or PIN.");
            }
        } catch (Exception e) {
            System.err.println("DATABASE ERROR: May problema sa koneksyon!");
            e.printStackTrace();
        }
    }
}