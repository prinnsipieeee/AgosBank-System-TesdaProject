package com.agosbank.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.agosbank.database.DBConnection;
import com.agosbank.services.TransactionService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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

public class WithdrawController {

    @FXML private Label availableBalanceLabel;
    @FXML private TextField recipientAccountField;
    @FXML private TextField amountField;
    @FXML private TextField sourceNameField;
    @FXML private Button confirmBtn;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private VBox notificationOverlay; 
    @FXML private Label notificationLabel;
    @FXML private Button notificationBtn;

    @FXML private Hyperlink receiptLink;

    private double lastAmount;
    private String lastAccId;
    private double currentBalance = 0.0;
    private final String currentUserAcc = UserSession.getInstance().getAccountId();

    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize(){
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.startsWith("0")) {
                amountField.setText(newValue.substring(1));
            }
        });

        refreshBalanceDisplay();

        if (notificationOverlay != null){
            notificationOverlay.setVisible(false);
            notificationOverlay.setManaged(false);
            receiptLink.setVisible(false);
            receiptLink.setManaged(false);
        }

        recipientAccountField.setText(UserSession.getInstance().getAccountId());
        sourceNameField.setText(UserSession.getInstance().getFullName());

        setupValidationListeners();
        checkFormValidity();

        amountField.setAlignment(Pos.CENTER);
        recipientAccountField.setAlignment(Pos.CENTER);
    }

    private void refreshBalanceDisplay(){
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT balance FROM users WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, currentUserAcc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentBalance = rs.getDouble("balance");
                availableBalanceLabel.setText("Available Balance: ₱ " + String.format("%,.2f", currentBalance));
            }
        } catch (SQLException e) {
        }
    }

    private void setupValidationListeners(){
        recipientAccountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateStyle(recipientAccountField, newVal.matches("AGOS-\\\\d{4}"));
            checkFormValidity();
        });

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double val = Double.parseDouble(newVal);
                updateStyle(amountField, val >= 100); // Min. 100
            } catch (NumberFormatException e) {
                updateStyle(amountField, false);
            }
            checkFormValidity();
        });
    }

    private void updateStyle(TextField field, boolean isValid) {
        field.getStyleClass().removeAll("text-field-success", "text-field-error");
        field.getStyleClass().add(isValid ? "text-field-success" : "text-field-error");
    }

    private void checkFormValidity() {
        boolean isAccountValid = recipientAccountField.getText().matches("AGOS-\\d{4}");
        boolean isAmountValid = false;
        try {
            isAmountValid = Double.parseDouble(amountField.getText()) >= 100;
        } catch (Exception e) {}

        confirmBtn.setDisable(!isAccountValid || !isAmountValid || sourceNameField.getText().isEmpty());
    }

    private void showNotification(String message, String buttonText, boolean showReceipt) {
        notificationLabel.setText(message);
        notificationBtn.setText(buttonText);

        receiptLink.setVisible(showReceipt);
        receiptLink.setManaged(showReceipt);

        notificationOverlay.setVisible(true);
        notificationOverlay.setManaged(true);
        notificationOverlay.toFront();
    }

    @FXML
    private void closeNotification(){
        notificationOverlay.setVisible(false);
        notificationOverlay.setManaged(false);

        receiptLink.setVisible(false);
        receiptLink.setManaged(false);
    }

   @FXML
   @SuppressWarnings("unused")
    private void handleConfirmWithdraw() {
        String amountText = amountField.getText();
        
        UserSession session = UserSession.getInstance();
        double currentBalance = session.getBalance();
        String userAccId = session.getAccountId();
        String sourceName = session.getFullName();

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showNotification("Please enter a valid amount.", "TRY AGAIN", false);
                return;
            }

            if (amount > currentBalance) {
                showNotification("Insufficient balance to proceed with this transaction. Your available balance is ₱" + String.format("%,.2f", currentBalance) + ".", "INSUFFICIENT FUNDS", false);
                return;
            }
            executeWithdrawal(userAccId, amount, sourceName);

        } catch (NumberFormatException e) {
            showNotification("Invalid amount format.", "OK", false);
        }
    }

    private void executeWithdrawal(String accountId, double amount, String sourceName) {
        confirmBtn.setVisible(false);
        loadingIndicator.setVisible(true);

            Task<Boolean> withdrawTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    Thread.sleep(1500);
                    return transactionService.withdraw(accountId, amount, sourceName);
                }
            };

            withdrawTask.setOnSucceeded(e -> {
                loadingIndicator.setVisible(false);
                confirmBtn.setVisible(true);

                if (withdrawTask.getValue()) {
                    this.lastAmount = amount;
                    this.lastAccId = accountId;

                    UserSession session = UserSession.getInstance();
                    session.setBalance(session.getBalance() - amount);
                    
                    refreshBalanceDisplay(); 

                    showNotification("Withdrawal successful! ₱" + String.format("%,.2f", amount) + " has been deducted.", "DONE", true);
                    
                    amountField.clear();
                } else {
                    showNotification("Withdrawal failed. Please check your database connection.", "TRY AGAIN", false);
                }
            });

            withdrawTask.setOnFailed(e -> {
                loadingIndicator.setVisible(false);
                confirmBtn.setVisible(true);
                showNotification("Unable to connect to the banking server. Please check your network connection and try again.", "CONNECTION TIMEOUT", false);
                withdrawTask.getException().printStackTrace();
            });

            new Thread(withdrawTask).start();
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

    @FXML
    @SuppressWarnings("unused")
    private void handlePresetAmount(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String amountText = clickedButton.getText();
        String cleanAmount = amountText.replace("P", "").replace("₱", "").replace(",", "").trim();
        amountField.setText(cleanAmount);
    }
    @FXML
    @SuppressWarnings("unused")
    private void handleOpenReceipt() {
        System.out.println("Hyperlink Clicked!"); // TEST 1
        closeNotification();
        System.out.println("Opening Receipt with: " + lastAmount + " for " + lastAccId); // TEST 2
        openReceipt(lastAmount, lastAccId);
    }

    private void openReceipt(double amount, String accId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/receipt.fxml"));
            Parent root = loader.load();
            ReceiptController controller = loader.getController();
            controller.setData("Withdraw", amount, accId);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AgosBank - Official Receipt");
            stage.show();
        } catch (IOException e) {
        }
    }
}

