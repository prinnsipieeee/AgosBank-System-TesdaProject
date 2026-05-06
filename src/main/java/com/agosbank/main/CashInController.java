package com.agosbank.main;

import java.io.IOException;

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

public class CashInController {

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

    private TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        refreshBalanceDisplay();
        
        // Siguraduhin na tago ang notification sa simula
        if (notificationOverlay != null) {
            notificationOverlay.setVisible(false);
            notificationOverlay.setManaged(false);
            receiptLink.setVisible(false); // Siguraduhin na tago ang link
            receiptLink.setManaged(false);
        }

        recipientAccountField.setText(UserSession.getAccountId());
        sourceNameField.setText(UserSession.getFullName());

        setupValidationListeners();
        checkFormValidity();

        amountField.setAlignment(Pos.CENTER);
        recipientAccountField.setAlignment(Pos.CENTER);
    }

    private void refreshBalanceDisplay() {
        double currentBalance = UserSession.getBalance();
        availableBalanceLabel.setText("Available Balance: ₱ " + String.format("%,.2f", currentBalance));
    }

    private void setupValidationListeners() {
        recipientAccountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateStyle(recipientAccountField, newVal.matches("AGOS-\\d{4}"));
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

    // --- CUSTOM NOTIFICATION LOGIC ---

    private void showNotification(String message, String buttonText, boolean showReceipt) {
        notificationLabel.setText(message);
        notificationBtn.setText(buttonText);

        receiptLink.setVisible(showReceipt);
        receiptLink.setManaged(showReceipt);
        
        notificationOverlay.setVisible(true);
        notificationOverlay.setManaged(true);
        notificationOverlay.toFront(); // Siguraduhin na nasa pinaka-ibabaw
    }

    @FXML
    private void closeNotification() {
        notificationOverlay.setVisible(false);
        notificationOverlay.setManaged(false);

         receiptLink.setVisible(false);
        receiptLink.setManaged(false);
    }

    @FXML
    private void handleConfirmCashIn() {
        String targetAcc = recipientAccountField.getText();
        String amountText = amountField.getText();
        String source = sourceNameField.getText();

        // Imbes na Alert Confirmation, rekta na tayo sa execute (o pwede ka gumawa ng confirm overlay)
        executeTransaction(targetAcc, Double.parseDouble(amountText), source);
    }

    private void executeTransaction(String targetAcc, double amount, String source) {
        // 1. Tago muna ang button at ipakita ang loading indicator
        confirmBtn.setVisible(false); 
        loadingIndicator.setVisible(true);

        Task<Boolean> depositTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(1500); // Konting delay para sa "executive" feel ng processing
                // Tatawagin ang service. Kung false, ibig sabihin may mali sa database check.
                return transactionService.deposit(targetAcc, amount, source);
            }
        };

        depositTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            confirmBtn.setVisible(true); 
            
            if (depositTask.getValue()) {

                this.lastAmount = amount;
                this.lastAccId = targetAcc;

                UserSession.setBalance(UserSession.getBalance() + amount);
                refreshBalanceDisplay(); 
                
                showNotification("Transaction successful! ₱" + String.format("%,.2f", amount) + " has been added.", "DONE" , true);
            } else {
                // FAILURE: Dito papasok 'yung error message para sa maling ID o Name
                showNotification("Transaction failed. Walang ganitong ID or Name sa database.", "TRY AGAIN" , false);
            }
        });

        depositTask.setOnFailed(e -> {
            // SYSTEM ERROR: Kapag patay ang XAMPP o may SQL error
            loadingIndicator.setVisible(false);
            confirmBtn.setVisible(true);
            showNotification("System Error. Pakicheck kung naka-ON ang XAMPP o database connection.", "OK", false);
            
            // Para makita mo ang actual error sa console habang nagde-debug
            depositTask.getException().printStackTrace();
        });

        new Thread(depositTask).start();
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

    @FXML
    private void handlePresetAmount(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String amountText = clickedButton.getText();
        String cleanAmount = amountText.replace("P", "").replace("₱", "").replace(",", "").trim();
        amountField.setText(cleanAmount);
    }
    @FXML
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

            // Kunin ang controller ng Receipt
            ReceiptController controller = loader.getController();
            // Ipasa ang data (Type, Amount, Recipient, RefID)
            controller.setData("Cash In", amount, accId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("AgosBank - Official Receipt");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}