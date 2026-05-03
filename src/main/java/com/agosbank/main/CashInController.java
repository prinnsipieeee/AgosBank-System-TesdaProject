package com.agosbank.main;

import java.io.IOException;
import java.util.Optional;

import com.agosbank.services.TransactionService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CashInController {

    @FXML private Label availableBalanceLabel; // Bagong display para sa balance
    @FXML private TextField recipientAccountField;
    @FXML private TextField amountField;
    @FXML private TextField sourceNameField;
    @FXML private Button confirmBtn;
    @FXML private ProgressIndicator loadingIndicator;

    private TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        refreshBalanceDisplay();


        recipientAccountField.setText(UserSession.getAccountId());
        sourceNameField.setText(UserSession.getFullName());

        // 3. Real-time Input Validation Visuals
        setupValidationListeners();

        checkFormValidity();
    }

    private void refreshBalanceDisplay() {
        // Kunin ang balance sa UserSession at i-format nang may pesong sign
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
                updateStyle(amountField, val > 0);
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
        // Siguraduhin na 'yung regex sa listener ay tumutugma sa account ID mo
        boolean isAccountValid = recipientAccountField.getText().matches("AGOS-\\d{4}");
        boolean isAmountValid = false;
        try {
            isAmountValid = Double.parseDouble(amountField.getText()) >= 100; // Min. 100 base sa UI mo
        } catch (Exception e) {}

        // I-disable ang button kung may mali
        confirmBtn.setDisable(!isAccountValid || !isAmountValid || sourceNameField.getText().isEmpty());
    }

    @FXML
    private void handleConfirmCashIn() {
        String targetAcc = recipientAccountField.getText();
        String amountText = amountField.getText();
        String source = sourceNameField.getText();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Transaction");
        confirmAlert.setHeaderText("AgosBank - Verification");
        confirmAlert.setContentText("Sigurado ka bang maghuhulog ka ng ₱" + amountText + " kay " + targetAcc + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            executeTransaction(targetAcc, Double.parseDouble(amountText), source);
        }
    }

    private void executeTransaction(String targetAcc, double amount, String source) {
        // 🔥 FIX 1: Tago ang button, Labas ang loading
        confirmBtn.setVisible(false); 
        loadingIndicator.setVisible(true);

        Task<Boolean> depositTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Thread.sleep(1500); 
                return transactionService.deposit(targetAcc, amount, source);
            }
        };

        depositTask.setOnSucceeded(e -> {
            // 🔥 FIX 2: Tago ang loading, Labas ulit ang button
            loadingIndicator.setVisible(false);
            confirmBtn.setVisible(true); 
            
            if (depositTask.getValue()) {
                UserSession.setBalance(UserSession.getBalance() + amount);
                refreshBalanceDisplay(); 
                showSuccessWithReceipt(amount, targetAcc);
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Transaction failed.");
            }
        });

        // Sa setOnFailed, dapat ibalik din ang button para hindi "stuck" ang user
        depositTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            confirmBtn.setVisible(true);
            showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong.");
        });

        new Thread(depositTask).start();
    }

    private void showSuccessWithReceipt(double amount, String accId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AgosBank Success");
        
        Label msg = new Label("Transaction successful!");
        Hyperlink receiptLink = new Hyperlink("Generate Receipt");
        receiptLink.setOnAction(e -> {
            dialog.close();
            openReceipt(amount, accId);
        });

        VBox box = new VBox(10, msg, receiptLink);
        box.setStyle("-fx-padding: 20; -fx-alignment: center;");
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        
        dialog.showAndWait(); // Tinanggal natin ang backToDashboard dito para makita yung updated balance
    }

    private void openReceipt(double amount, String accId) {
        System.out.println("Opening Receipt...");
        // Dito papasok yung logic para sa receipt.fxml mo
    }

    @FXML
    private void backToDashboard(MouseEvent event) {
        try {
            // Siguraduhin na ang path ay nagsisimula sa "/" at match sa folder sa VS Code sidebar
            String path = "/com/agosbank/fxml/dashboard.fxml";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // Kunin ang Stage mula sa mouse event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
            
            System.out.println("Returned to Dashboard via Icon Click");
            
        } catch (IOException e) {
            System.err.println("Error: Hindi mahanap ang file sa location: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Error: Null ang location! Pakicheck kung tama ang spelling ng path.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handlePresetAmount(ActionEvent event) {
        // 1. Kunin ang button na pinindot
        Button clickedButton = (Button) event.getSource();
        
        // 2. Kunin ang text sa loob ng button (halimbawa: "100" o "P100")
        String amountText = clickedButton.getText();
        
        // 3. Linisin ang text (alisin ang "P" o "₱" kung mayroon man)
        String cleanAmount = amountText.replace("P", "").replace("₱", "").replace(",", "").trim();
        
        // 4. Ilagay ang value sa TextField
        amountField.setText(cleanAmount);
        
        System.out.println("Preset selected: ₱" + cleanAmount);
    }
}