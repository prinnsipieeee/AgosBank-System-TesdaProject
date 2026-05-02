package com.agosbank.main;

import java.io.IOException;

import com.agosbank.services.AuthService;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {
    @FXML private TextField nameField, mobileField, emailField;
    
    // Set PIN Fields
    @FXML private PasswordField pinField;
    @FXML private TextField visiblePinField;
    
    // Confirm PIN Fields
    @FXML private PasswordField confirmPinField;
    @FXML private TextField visibleConfirmPinField;

    private AuthService authService = new AuthService();

    @FXML private VBox errorCard;
    @FXML private Label errorMessageLabel;
    @FXML private Button popup_btn;

    @FXML
    private void handleSignUp() {

        syncFieldsBeforeSubmit();

        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String email = emailField.getText().trim();
        String pin = pinField.getText();
        String confirmPin = confirmPinField.getText();
        
        // Generate 4-digit Account ID (halimbawa: AGOS-1234)
        String accId = "AGOS-" + (int)(Math.random() * 9000 + 1000);

        // 2. Error: Validation para sa blankong fields
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || pin.isEmpty() || confirmPin.isEmpty()) {
            showError("Please fill out all text fields to proceed.");
            return;
        }

        // 3. Error: Validation kung match ang PIN
        if (!pin.equals(confirmPin)) {
            showError("The PINs you entered do not match. Please try again.");
            return;
        }

        // 4. Error: Check sa Database kung existing na ang account
        if (authService.isAccountExists(mobile, email)) {
            showError("An account with this mobile number or email already exists.");
            return;
        }

        // 5. Success Logic: I-save sa Database (FullName, AccID, Phone, Email, PIN)
        boolean isSaved = authService.registerUser(name, accId, mobile, email, pin);

        if (isSaved) {
            showSuccessAlert("Account created successfully. You can now login.");
        } else {
            showError("Something went wrong while saving your account. Please try again.");
        }
    }

    // Custom Alert: Para sa Error
    private void showError(String message) {
        errorMessageLabel.setText(message);
        popup_btn.getStyleClass().remove("success-btn");
        errorCard.setVisible(true);
        errorCard.setMouseTransparent(false);

         // Animation
    FadeTransition ft = new FadeTransition(Duration.millis(300), errorCard);
    ft.setFromValue(0.0);
    ft.setToValue(1.0);
    ft.play();
    }

    // Custom Alert: Para sa Success
    private void showSuccessAlert(String message) {
        errorMessageLabel.setText(message);

        popup_btn.setText("LOGIN NOW");
    
        // 2. Dagdagan ng CSS class para maging green/gold ang kulay
        if (!popup_btn.getStyleClass().contains("success-btn")) {
            popup_btn.getStyleClass().add("success-btn");
        }

        errorCard.setMouseTransparent(false);
        errorCard.setVisible(true);
    }

    @FXML
    private void togglePinVisibility() {
        if (pinField.isVisible()) {
            // Ipakita ang text
            visiblePinField.setText(pinField.getText());
            visibleConfirmPinField.setText(confirmPinField.getText());
            visiblePinField.setVisible(true);
            visibleConfirmPinField.setVisible(true);
            pinField.setVisible(false);
            confirmPinField.setVisible(false);
        } else {
            pinField.setText(visiblePinField.getText());
            confirmPinField.setText(visibleConfirmPinField.getText());
            pinField.setVisible(true);
            confirmPinField.setVisible(true);
            visiblePinField.setVisible(false);
            visibleConfirmPinField.setVisible(false);
        }
    }
    
    // Ito ang nawawalang "utak" ng method na tinatawag mo
    private void syncFieldsBeforeSubmit() {
        // Tinitignan kung naka-show ang visible text fields
        if (visiblePinField != null && visiblePinField.isVisible()) {
            pinField.setText(visiblePinField.getText());
            confirmPinField.setText(visibleConfirmPinField.getText());
        }
    }

    private void navigateToLogin() {
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/agosbank/fxml/login.fxml"));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Unable to load the login screen.");
        }
    }

    @FXML
    private void handleClosePopup() {
        errorCard.setVisible(false);
        errorCard.setMouseTransparent(true);

        if (errorMessageLabel.getText().contains("successfully") || 
            errorMessageLabel.getText().contains("Welcome")) {
            
            navigateToLogin(); // Dito lang siya lilipat
        } 
    }

    @FXML
    private void handleBackToLogin() {
        // Tatawagin lang nito yung existing method mo para bumalik sa login screen
        navigateToLogin();
    }
}