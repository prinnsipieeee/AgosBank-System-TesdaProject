package com.agosbank.main;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ReceiptController {

    @FXML private Label typeLabel;
    @FXML private Label amountLabel;
    @FXML private Label refIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label recipientValue;

    @FXML private Label recipientKey; 
    @FXML private Button downloadBtn;
    @FXML private VBox receiptContainer;

    public void setData(String type, double amount, String recipient) {
        typeLabel.setText(type);
        amountLabel.setText("₱ " + String.format("%,.2f", amount));

        String refId = "AGOS-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        refIdLabel.setText(refId);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateLabel.setText(LocalDateTime.now().format(formatter));

        if (type.equalsIgnoreCase("Cash In") || type.equalsIgnoreCase("Withdraw")) {
            hideRecipientRow(true);
        } else {
            hideRecipientRow(false);
            recipientValue.setText(recipient);
        }
    }
    private void hideRecipientRow(boolean shouldHide) {
        boolean isVisible = !shouldHide;
        
        recipientKey.setVisible(isVisible);
        recipientKey.setManaged(isVisible);
        
        recipientValue.setVisible(isVisible);
        recipientValue.setManaged(isVisible);
    }

    @FXML
    private void handleDownload() {
        System.out.println("Processing receipt download...");

        // 1. GUMAWA NG SNAPSHOT (Ang "Picture" ng resibo)
        WritableImage snapshot = receiptContainer.snapshot(new SnapshotParameters(), null);

        // 2. MAGBUKAS NG FILE CHOOSER (Para professional ang dating)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.setInitialFileName("AgosBank_Receipt_" + System.currentTimeMillis() + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        // Kunin ang window para sa dialog
        Window stage = downloadBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // 3. I-SAVE ANG IMAGE GAMIT ANG IMAGEIO
                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                        (int) snapshot.getWidth(),
                        (int) snapshot.getHeight(),
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                javafx.scene.image.PixelReader pixelReader = snapshot.getPixelReader();
                for (int y = 0; y < snapshot.getHeight(); y++) {
                    for (int x = 0; x < snapshot.getWidth(); x++) {
                        bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
                    }
                }
                javax.imageio.ImageIO.write(bufferedImage, "png", file);

                System.out.println("Receipt saved successfully to: " + file.getAbsolutePath());
                
                // I-close ang window pagkatapos i-save (Optional)
                ((Stage) stage).close();

            } catch (IOException e) {
                System.err.println("Error saving receipt: " + e.getMessage());
            }
        }
    }
}