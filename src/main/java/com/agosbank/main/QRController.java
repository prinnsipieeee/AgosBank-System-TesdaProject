package com.agosbank.main;

import com.agosbank.utils.QRGenerator;
import com.agosbank.main.UserSession; // Siguraduhin na tama ang path nito
import com.agosbank.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import javafx.scene.input.MouseEvent;

public class QRController {

    @FXML private ImageView imgQRCode;
    @FXML private Label lblUserName;
    @FXML private Label lblAccountID;

    @FXML
    public void initialize() {
        // 1. Get Data from Session
        String fullName = UserSession.getInstance().getFullName();
        String accId = UserSession.getInstance().getAccountId();

        // 2. Set Labels (Executive Formatting)
        if (fullName != null) lblUserName.setText(fullName.toUpperCase());
        if (accId != null) lblAccountID.setText(maskAccountID(accId));

        // 3. Generate and Display QR
        loadQRCode(accId);
    }

    private void loadQRCode(String accId) {
        try {
            // "AGOS-" prefix para sa internal tracking
            byte[] qrBytes = QRGenerator.getQRCodeBytes("AGOS-" + accId, 300, 300);
            Image qrImage = new Image(new ByteArrayInputStream(qrBytes));
            imgQRCode.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Error generating QR: " + e.getMessage());
        }
    }

    // Executive Masking Logic: 09123456789 -> 09*****6789
    private String maskAccountID(String id) {
        if (id == null || id.length() < 8) return id;
        return id.substring(0, 2) + "*****" + id.substring(id.length() - 4);
    }

    @FXML
    private void handleDownload() {
        String accId = UserSession.getInstance().getAccountId();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save AgosBank QR");
        fileChooser.setInitialFileName("AgosBank_QR_" + accId + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(imgQRCode.getScene().getWindow());

        if (file != null) {
            try {
                // High-quality version para sa download (500x500)
                byte[] qrBytes = QRGenerator.getQRCodeBytes("AGOS-" + accId, 500, 500);
                Files.write(file.toPath(), qrBytes);
                System.out.println("Executive QR saved to: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleNavigation(MouseEvent event) {
        // Kuhanin natin kung anong VBox ang pinindot
        VBox source = (VBox) event.getSource();
        String id = source.getId();

        switch (id) {
            case "navHome" -> SceneSwitcher.switchScene(source, "dashboard.fxml");
            case "navHistory" -> SceneSwitcher.switchScene(source, "history.fxml");
            case "navQR" -> System.out.println();
            case "navProfile" -> System.out.println();
            default -> System.out.println();
        }
    }
}