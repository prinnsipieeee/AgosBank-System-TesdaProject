package com.agosbank.main;

import java.io.IOException;

import com.agosbank.utils.SceneSwitcher;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AccountController {

    @FXML private Label lblFullName;
    @FXML private Label lblAccountIdValue;
    @FXML private Label lblMobileValue;
    @FXML private Label lblMemberSinceValue;
    @FXML private Label lblEmailValue;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            lblFullName.setText(session.getFullName().toUpperCase());
            lblAccountIdValue.setText(session.getAccountId());
            lblMobileValue.setText(session.getPhoneNumber());
            lblMemberSinceValue.setText(session.getMemberSince());
            lblEmailValue.setText(session.getEmail());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLogout() {
        UserSession.getInstance().cleanUserSession();
        SceneSwitcher.switchScene(lblFullName, "login.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleNavigation(MouseEvent event) {
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

    @FXML
    @SuppressWarnings("unused")
    private void ChangePassword() {
        try {
            String path = "/com/agosbank/fxml/security.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            SecurityController controller = loader.getController();
            controller.showCreatePinDirectly();
            
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();         
        } catch (IOException e) {
        }
    }
}