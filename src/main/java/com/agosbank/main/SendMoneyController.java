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
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.startsWith("0")) {
                amountField.setText(newValue.substring(1));
            }
        });
        
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
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSendMoney() {
        System.out.println("Validation Started...");
        
        String recipientInput = recipientAccField.getText().trim();
        String recipientName = nameField.getText().trim();
        String mobileNumInput = mobileField.getText().trim();
        String amountInput = amountField.getText().trim();

        if (recipientInput.isEmpty() || recipientName.isEmpty() || amountInput.isEmpty() || mobileNumInput.isEmpty()) {
            showNotification("Please fill in all required fields.", false);
            return;
        }

        if(recipientInput.equals(currentUserAcc)) {
            showNotification("Transaction restricted. You cannot send money to your own account using this feature.", false);
            return;
        }

        try {
            double amount = Double.parseDouble(amountInput);
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
                showNotification("The recipient information provided does not match our records. Please review the details and try again.", false);
                sendBtn.setVisible(true);
                return; 
            }

            this.pendingAmount = amount;
            this.pendingRecipient = recipientInput;
            this.pendingRecipientName = recipientName;

            confirmMsgLabel.setText("Are you sure you want to send\n₱" + String.format("%,.2f", amount) + " to " + recipientInput + "?");
            confirmationOverlay.setVisible(true);
        });
        pause.play();
        } catch (NumberFormatException e) {
            showNotification("Invalid amount format.", false);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void processTransfer() {
        confirmationOverlay.setVisible(false); 
        String senderAccId = UserSession.getInstance().getAccountId();
        TransactionService transService = new TransactionService();
        
        boolean isSuccess = transService.sendMoney(senderAccId, pendingRecipient, pendingAmount);

        if (isSuccess) {
            showNotification(String.format("Your transfer of ₱%,.2f to %s has been processed successfully.", pendingAmount, pendingRecipient), true);
            loadCurrentBalance();
            recipientAccField.clear();
            amountField.clear();
        }
    }

    @FXML
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    private void closeNotification() {
        notificationOverlay.setVisible(false);
        sendBtn.setVisible(true);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleGenerateReceipt() {
        openReceipt(this.pendingAmount, this.pendingRecipient);
    }

    private void openReceipt(double amount, String accId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/receipt.fxml"));
            Parent root = loader.load();
            ReceiptController controller = loader.getController();
            controller.setData("Send Money", amount, accId);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AgosBank - Official Receipt");
            stage.show();
        } catch (IOException | NullPointerException e) {
        }
    }

    @FXML
    @SuppressWarnings("unused")
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
        }
    }
}