package com.agosbank.main;

import com.agosbank.utils.SceneSwitcher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class AccountController {

    // Profile Section
    @FXML private Label lblFullName;
    
    // Info Section (Yung mga Value Labels sa baba)
    @FXML private Label lblAccountIdValue;
    @FXML private Label lblMobileValue;
    @FXML private Label lblMemberSinceValue;
    @FXML private Label lblEmailValue;

    @FXML
    public void initialize() {
        // 1. Kuhanin ang data mula sa UserSession
        UserSession session = UserSession.getInstance();

        // 2. I-populate ang mga Labels
        if (session != null) {
            lblFullName.setText(session.getFullName().toUpperCase());
            
            // I-set ang mga specific values
            lblAccountIdValue.setText(session.getAccountId());
            lblMobileValue.setText(session.getPhoneNumber());
            lblMemberSinceValue.setText(session.getMemberSince());
            lblEmailValue.setText(session.getEmail());
        }
    }

    @FXML
    private void handleChangePassword() {
        // I-link mo dito yung change password fxml mo
        System.out.println("Switching to Change Password...");
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        SceneSwitcher.switchScene(lblFullName, "login.fxml");
    }

     @FXML
    private void handleNavigation(MouseEvent event) {
        // Kuhanin natin kung anong VBox ang pinindot
        VBox source = (VBox) event.getSource();
        String id = source.getId();

        switch (id) {
            case "navHome" -> SceneSwitcher.switchScene(source, "dashboard.fxml");
            case "navHistory" -> SceneSwitcher.switchScene(source, "history.fxml");
            case "navQR" -> SceneSwitcher.switchScene(source, "qrcode.fxml");
            case "navProfile" -> System.out.println();
            default -> System.out.println();
        }
    }
}