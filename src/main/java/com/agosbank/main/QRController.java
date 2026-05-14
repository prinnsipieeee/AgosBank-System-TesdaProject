package com.agosbank.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

import com.agosbank.utils.QRGenerator;
import com.agosbank.utils.SceneSwitcher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class QRController {

    @FXML private ImageView imgQRCode;
    @FXML private Label lblUserName;
    @FXML private Label lblphoneNumber;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();

        String fullName = UserSession.getInstance().getFullName();
        String phoneNumber = UserSession.getInstance().getPhoneNumber();

        System.out.println("DEBUG: Session Phone: " + session.getPhoneNumber());
    
        if (fullName != null) lblUserName.setText(fullName.toUpperCase());
        if (phoneNumber != null) lblphoneNumber.setText(maskPhoneNumber(phoneNumber));

        loadQRCode(phoneNumber);
    }
    private void loadQRCode(String phoneNumber) {
        try {
            byte[] qrBytes = QRGenerator.getQRCodeBytes("AGOS-" + phoneNumber, 300, 300);
            Image qrImage = new Image(new ByteArrayInputStream(qrBytes));
            imgQRCode.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Error generating QR: " + e.getMessage());
        }
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.substring(0, 2) + "*****" + phone.substring(phone.length() - 4);
    }

    @FXML
    private void handleDownload() {
        String phoneNumber = UserSession.getInstance().getPhoneNumber();
        if (phoneNumber == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save AgosBank QR");
        fileChooser.setInitialFileName("Agos_QR_" + phoneNumber + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(imgQRCode.getScene().getWindow());

        if (file != null) {
            try {
                byte[] qrBytes = QRGenerator.getQRCodeBytes("AGOS-" + phoneNumber, 500, 500);
                Files.write(file.toPath(), qrBytes);
                System.out.println("QR saved successfully for: " + phoneNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleNavigation(MouseEvent event) {
        VBox source = (VBox) event.getSource();
        String id = source.getId();

        switch (id) {
            case "navHome" -> SceneSwitcher.switchScene(source, "dashboard.fxml");
            case "navHistory" -> SceneSwitcher.switchScene(source, "history.fxml");
            case "navQR" -> System.out.println();
            case "navProfile" -> SceneSwitcher.switchScene(source, "account.fxml");
            default -> System.out.println();
        }
    }
}