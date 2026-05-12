package com.agosbank.main;

import java.io.IOException;

import com.agosbank.models.User;
import com.agosbank.services.AuthService;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField usernameField; 
    @FXML private Hyperlink createAccountLink;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView eyeIcon;
    @FXML private VBox errorCard;
    @FXML private Label errorMessageLabel;

    private final AuthService authService = new AuthService();
    
    @FXML
    public void initialize() {
        System.out.println("DEBUG: LoginController initialized!");
        
        if (errorCard != null) {
            errorCard.setVisible(false);
            errorCard.setOpacity(0);
        }
    }
    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Authentication required. Please enter your phone number and PIN.");
            return;
        }

        try {
            User loggedInUser = authService.loginUser(user, pass);

            if (loggedInUser != null) {
                UserSession.getInstance().setFullName(loggedInUser.getFullName());
                UserSession.getInstance().setAccountId(String.valueOf(loggedInUser.getAccountId()));
                UserSession.getInstance().setBalance(loggedInUser.getBalance());
                UserSession.getInstance().setPhoneNumber(loggedInUser.getPhoneNumber());
                UserSession.getInstance().setEmail(loggedInUser.getEmail());
                UserSession.getInstance().setMemberSince(loggedInUser.getMemberSince());

                System.out.println("Login Success! Welcome, " + UserSession.getInstance().getFullName());

                navigateToDashboard();
                
            } else {
                showError("Authentication failed. Please verify your phone number and PIN.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Para makita mo ang actual error sa console
            showError("Database Error: Check mo kung naka-ON ang XAMPP.");
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        
        errorCard.setVisible(true);
        errorCard.setManaged(true); 
        
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
        System.out.println("DEBUG: Close button clicked!");
        errorCard.setVisible(false);
        errorCard.setManaged(false);
        errorCard.setMouseTransparent(true);
    }

    @FXML
    private void handleCreateAccount() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/agosbank/fxml/register.fxml"));
            Stage stage = (Stage) createAccountLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
            // Dito mo rin pwedeng palitan yung icon ng "Eye Close"
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            visiblePasswordField.setVisible(false);
        }
    }

    private void navigateToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/agosbank/fxml/dashboard.fxml"));

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgosBank - Dashboard");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error: Hindi mahanap ang dashboard.fxml.");
        }
    }
}