package com.agosbank.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ReceiptController {

    @FXML private Label typeLabel;
    @FXML private Label amountLabel;
    @FXML private Label refIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label recipientValue;

    @FXML private Label recipientKey; 
    @FXML private Button downloadBtn;

    public void setData(String type, double amount, String recipient) {
        typeLabel.setText(type);
        amountLabel.setText("₱ " + String.format("%,.2f", amount));

        String refId = "AGOS-REF: " + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
        // Sa ngayon, gagawin muna nating "Close" button ito.
        // Pwede mong dagdagan ng logic dito para mag-save as Image o PDF sa future.
        System.out.println("Receipt processing for download...");
        
        Stage stage = (Stage) downloadBtn.getScene().getWindow();
        stage.close();
    }
}