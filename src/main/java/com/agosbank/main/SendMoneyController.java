package com.agosbank.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.agosbank.database.DBConnection;
import com.agosbank.services.TransactionService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SendMoneyController {

    @FXML private Label balanceLabel, statusMsgLabel;
    @FXML private TextField recipientAccField, amountField;
    @FXML private TextField mobilefield;
    @FXML private TextField nameField;

    @FXML private VBox notificationOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button notificationBtn;

    private double pendingAmount;
    private String pendingRecipient;

    @FXML private VBox confirmationOverlay;
    @FXML private Label confirmMsgLabel;

    private double currentBalance = 0.0;
    private final String currentUserAcc = UserSession.getInstance().getAccountId();

    @FXML
    public void initialize() {
        loadCurrentBalance();
        amountField.setAlignment(javafx.geometry.Pos.CENTER);
    }

    private void loadCurrentBalance() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT balance FROM users WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentUserAcc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentBalance = rs.getDouble("balance");
                balanceLabel.setText("Available Balance: ₱ " + String.format("%,.2f", currentBalance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendMoney() {
        System.out.println("Validation Started...");
        
        String recipientInput = recipientAccField.getText().trim();
        String amountInput = amountField.getText().trim();

        // 1. UI Validation
        if (recipientInput.isEmpty() || amountInput.isEmpty()) {
            showNotification("Please fill in all required fields.", false);
            return;
        }

        try {
            double amount = Double.parseDouble(amountInput);

            // 2. Business Logic Validation
            if (amount < 100) { showNotification("Min ₱100.00", false); return; }
            if (amount > currentBalance) { showNotification("Insufficient balance", false); return; }

            // 3. I-store ang data at Ipakita ang Confirmation Overlay
            this.pendingAmount = amount;
            this.pendingRecipient = recipientInput;

            confirmMsgLabel.setText("Are you sure you want to send\n₱" + 
                                   String.format("%,.2f", amount) + " to " + recipientInput + "?");
            
            confirmationOverlay.setVisible(true);

        } catch (NumberFormatException e) {
            showNotification("Invalid amount format.", false);
        }
    }

    // ETO YUNG TATAWAGIN NG "CONFIRM" BUTTON SA VBOX MO
    @FXML
    private void processTransfer() {
        confirmationOverlay.setVisible(false); // Itago na ang confirmation
        
        String senderAccId = UserSession.getInstance().getAccountId();
        
        // TAWAG SA BACKEND (TransactionService)
        TransactionService transService = new TransactionService();
        
        // Ginamit natin yung 'pendingRecipient' na nag-match sa 'receiverAccId' ng backend mo
        boolean isSuccess = transService.sendMoney(senderAccId, pendingRecipient, pendingAmount);

        if (isSuccess) {
            showNotification("Transfer Successful!", true);
            loadCurrentBalance();
            recipientAccField.clear();
            amountField.clear();
        } else {
            showNotification("Transaction failed. Check recipient details.", false);
        }
    }

    @FXML
    private void cancelTransfer() {
        confirmationOverlay.setVisible(false);
        System.out.println("Transfer cancelled by user.");
    }

    private void showNotification(String message, boolean isSuccess) {
        statusMsgLabel.setText(message);
        statusMsgLabel.setStyle(isSuccess ? "-fx-text-fill: #00d9a5;" : "-fx-text-fill: #ff4d4d;");
        notificationOverlay.setVisible(true);
    }

    @FXML
    private void closeNotification() {
        notificationOverlay.setVisible(false);
    }

    @FXML
    private void handleGenerateReceipt() {
        openReceipt(Double.parseDouble(amountField.getText()), recipientAccField.getText());
    }

    private void openReceipt(double amount, String accId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/receipt.fxml"));
            Parent root = loader.load();
            ReceiptController controller = loader.getController();
            
            // Pass the data to receipt
            controller.setData("Send Money", amount, accId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AgosBank - Official Receipt");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToDashboard(MouseEvent event) {
        try {
            String path = "/com/agosbank/fxml/dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}