package com.agosbank.main;

import com.agosbank.models.User;
import com.agosbank.services.AuthService;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private TextField usernameField; 

    @FXML
    private PasswordField passwordField;

    @FXML 
    private VBox errorCard; // Ang overlay card natin

    @FXML 
    private Label errorMessageLabel; // Ang text sa loob ng card

    private final AuthService authService = new AuthService();
    
    @FXML
    public void initialize() {
        System.out.println("DEBUG: LoginController initialized!");
        
        // Siguraduhing tago ang error card sa simula
        if (errorCard != null) {
            errorCard.setVisible(false);
            errorCard.setOpacity(0);
        }
    }

    @FXML
    private void handleLogin() {
    String user = usernameField.getText().trim();
    String pass = passwordField.getText().trim();

    // 1. Check kung may laman
    if (user.isEmpty() || pass.isEmpty()) {
        showError("Authentication required. Please enter your phone number and PIN.");
        return;
    }

    try {
        User loggedInUser = authService.loginUser(user, pass);

        if (loggedInUser != null) {
            // SUCCESS: Pwedeng palitan ang kulay ng card or diretso Dashboard
            System.out.println("Login Success!"); 
        } else {
            // FAILURE: Heto ang maglalagay ng message sa Label
            showError("Authentication failed. Please verify your phone number and PIN.");
        }
    } catch (Exception e) {
        showError("Database Error: Check mo kung naka-ON ang XAMPP.");
    }
    }

    private void showError(String message) {
    errorMessageLabel.setText(message);
    
    // Gawin nating visible at managed ulit
    errorCard.setVisible(true);
    errorCard.setManaged(true); 
    
    // IMPORTANTE: Dalhin sa pinaka-harap at siguraduhing hindi transparent sa mouse
    errorCard.toFront(); 
    errorCard.setMouseTransparent(false); 
    
    // Animation
    FadeTransition ft = new FadeTransition(Duration.millis(300), errorCard);
    ft.setFromValue(0.0);
    ft.setToValue(1.0);
    ft.play();
    }

    @FXML
    private void closeErrorCard() {
        System.out.println("DEBUG: Close button clicked!"); // Para makita sa terminal kung gumagana
        errorCard.setVisible(false);
        errorCard.setManaged(false);
        errorCard.setMouseTransparent(true); // Gawing "ghost" ulit para ma-click yung login button
    }
}