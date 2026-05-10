package com.agosbank.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.agosbank.database.DBConnection;
import com.agosbank.services.TransactionService;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SendMoneyController {

    @FXML private Label balanceLabel, statusMsgLabel;
    @FXML private TextField recipientAccField, amountField;
    @FXML private TextField mobileField;
    @FXML private TextField nameField;
    @FXML private VBox notificationOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button notificationBtn;
    @FXML private Hyperlink receiptLink;
    @FXML private VBox confirmationOverlay;
    @FXML private Label confirmMsgLabel;
    @FXML private Button sendBtn;

    private double pendingAmount;
    private String pendingRecipient;
    private String pendingRecipientName;
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
        String recipientName = nameField.getText().trim();
        String mobileNumInput = mobileField.getText().trim();
        String amountInput = amountField.getText().trim();

        // 1. UI Validation
        if (recipientInput.isEmpty() || recipientName.isEmpty() || amountInput.isEmpty() || mobileNumInput.isEmpty()) {
            showNotification("Please fill in all required fields.", false);
            return;
        }

        try {
            double amount = Double.parseDouble(amountInput);

            // 2. Business Logic Validation
            if (amount < 100) { 
                showNotification("Min ₱100.00", false); return; }
            if (amount > currentBalance) { 
                showNotification("Insufficient balance", false); return; }
            
            sendBtn.setVisible(false);
            loadingIndicator.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(event -> {

            TransactionService service = new TransactionService();

            boolean isValid = service.validateRecipient(recipientInput, recipientName, mobileNumInput);

            loadingIndicator.setVisible(false);

            if (!isValid) {
                showNotification("Check recipient details. Account Number, Mobile Number or Name of Recipient is mismatch.", false);
                sendBtn.setVisible(true);
                return; 
            }

            // 3. I-store ang data at Ipakita ang Confirmation Overlay
            this.pendingAmount = amount;
            this.pendingRecipient = recipientInput;
            this.pendingRecipientName = recipientName;

            confirmMsgLabel.setText("Are you sure you want to send\n₱" + 
                                   String.format("%,.2f", amount) + " to " + recipientInput + "?");

            confirmationOverlay.setVisible(true);
        });
        pause.play();
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
            showNotification(String.format("Transfer Successful!\n₱%,.2f has been sent\nto %s", 
                                   pendingAmount, pendingRecipient ), true);
            loadCurrentBalance();
            recipientAccField.clear();
            amountField.clear();
        }
    }

    @FXML
    private void cancelTransfer() {
        confirmationOverlay.setVisible(false);
        sendBtn.setVisible(true);
        System.out.println("Transfer cancelled by user.");
    }

    private void showNotification(String message, boolean isSuccess) {
        statusMsgLabel.setText(message);
        statusMsgLabel.setWrapText(true);
        statusMsgLabel.setMaxWidth(260);
        statusMsgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        receiptLink.setVisible(isSuccess);
        receiptLink.setManaged(isSuccess);

        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        sendBtn.setVisible(true);
        if(isSuccess){
            notificationBtn.setText("DONE");
            statusMsgLabel.setStyle("-fx-text-fill: #ffffff;");

        } else {
            notificationBtn.setText("TRY AGAIN");
            statusMsgLabel.setStyle("-fx-text-fill: #ff6b6b;");
            receiptLink.setVisible(false);
        }
        notificationOverlay.setVisible(true);
    }

    @FXML
    private void closeNotification() {
        notificationOverlay.setVisible(false);
        sendBtn.setVisible(true);
    }

    @FXML
    private void handleGenerateReceipt() {
        openReceipt(this.pendingAmount, this.pendingRecipient);
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