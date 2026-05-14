package com.agosbank.main;

import java.io.IOException;

import com.agosbank.services.AuthService;
import com.agosbank.utils.SceneSwitcher;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SecurityController {
    @FXML private VBox paneForgotPin;  
    @FXML private VBox paneCreateNewPin; 
    @FXML private VBox confirmationOverlay; 
    @FXML private VBox notifBox;
    @FXML private Label notifTitle, notifMessage;

    @FXML private TextField mobileField; 
    @FXML private TextField emailField;  

    @FXML private PasswordField newPinField;
    @FXML private TextField visiblePinField;

    @FXML private PasswordField confirmPinField;
    @FXML private TextField visibleConfirmField;
    @FXML private ImageView backbutton;

    private final AuthService authService = new AuthService();
    private String verifiedMobile; 

    @FXML
    public void initialize() {
        showPanel(paneForgotPin);
        confirmationOverlay.setVisible(false);
        confirmationOverlay.setManaged(false);

        backbutton.setVisible(false);
        backbutton.setManaged(false);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleRequestVerification() {
        String mobile = mobileField.getText().trim();
        String email = emailField.getText().trim();
        String name = UserSession.getInstance().getFullName();

        if (mobile.isEmpty() || email.isEmpty()) {
            showNotification("Both Mobile Number and Email are required.", "INPUT ERROR", false);
            return;
        }

        if (authService.verifyForgotPinIdentity(mobile, email)) {
            this.verifiedMobile = mobile; 
            showPanel(paneCreateNewPin);
            showNotification("Identity Verified! Please set your new 4-digit PIN.", "SUCCESS", true);
        } else {
            showNotification("Account details do not match our records.", "ERROR", false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleUpdatePin() {
        String pin = newPinField.getText();
        String confirm = confirmPinField.getText();
        if (pin.length() != 4 || !pin.matches("\\d+")) {
            showNotification("PIN must be exactly 4 digits.", "TRY AGAIN", false);
            return;
        }

        if (pin.equals(confirm)) {
            confirmationOverlay.setVisible(true);
            confirmationOverlay.setManaged(true);
        } else {
            showNotification("PINs do not match!", "ERROR", false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void togglePinVisibility() {
        if (newPinField.isVisible()) {
            visiblePinField.setText(newPinField.getText());
            visibleConfirmField.setText(confirmPinField.getText());
            visiblePinField.setVisible(true);
            visibleConfirmField.setVisible(true);
            newPinField.setVisible(false);
            confirmPinField.setVisible(false);
        } else {
            newPinField.setText(visiblePinField.getText());
            confirmPinField.setText(visibleConfirmField.getText());
            newPinField.setVisible(true);
            confirmPinField.setVisible(true);
            visiblePinField.setVisible(false);
            visibleConfirmField.setVisible(false);
        }
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void onConfirmYes() {
        String newPin = newPinField.getText();
        if (authService.updatePinByMobile(verifiedMobile, newPin)) {
            showNotification("PIN updated! Use your new PIN to login.", "DONE", true);
        } else {
            showNotification("System Error. Failed to update PIN.", "ERROR", false);
        }
        confirmationOverlay.setVisible(false);
        confirmationOverlay.setManaged(false);
    }

    @FXML
    @SuppressWarnings("unused")
    private void onConfirmCancel() {
        confirmationOverlay.setVisible(false);
        confirmationOverlay.setManaged(false);
    }

    private void showPanel(VBox panelToShow) {
        // Itago lahat muna
        paneForgotPin.setVisible(false);
        paneForgotPin.setManaged(false);
        paneCreateNewPin.setVisible(false);
        paneCreateNewPin.setManaged(false);

        panelToShow.setVisible(true);
        panelToShow.setManaged(true);
    }

    private void showNotification(String msg, String title, boolean success) {
        notifTitle.setText(title.toUpperCase());
        notifMessage.setText(msg);
        notifBox.getStyleClass().removeAll("notif-success", "notif-error");
        if (success) {
            notifBox.getStyleClass().add("notif-success");
        } else {
            notifBox.getStyleClass().add("notif-error");
        }

        notifBox.setVisible(true);
        notifBox.setManaged(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notifBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(100), notifBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                notifBox.setVisible(false);
                notifBox.setManaged(false);
            });
            fadeOut.play();
        });
        delay.play();
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void closeSecuritySettings() {
        try {
            String path = "/com/agosbank/fxml/login.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) newPinField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
            } catch (IOException | NullPointerException e) {
        }
    }

    public void showCreatePinDirectly() {

        paneForgotPin.setVisible(false);
        paneForgotPin.setManaged(false);

        paneCreateNewPin.setVisible(true);
        paneCreateNewPin.setManaged(true);

        backbutton.setVisible(true);
        backbutton.setManaged(true);

        this.verifiedMobile = UserSession.getInstance().getPhoneNumber();
    }

    @FXML
    private void backToAccount() {
        SceneSwitcher.switchScene(backbutton, "account.fxml");
    }
}