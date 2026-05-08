package com.agosbank.main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.agosbank.services.TransactionService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    @FXML private Label greetingLabel;
    @FXML private Label balanceLabel;
    @FXML private ImageView toggleIcon; 

    private TransactionService transactionService = new TransactionService();
    private double currentBalance = 0.0;
    private boolean isBalanceShown = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String userName = UserSession.getInstance().getFullName();
        if (userName != null && !userName.isEmpty()) {
            greetingLabel.setText("Hi, " + userName + "!");
        } else {
            greetingLabel.setText("Hi, User!"); 
        }

        loadBalanceFromDB();

        updateBalanceDisplay();
    }

    private void loadBalanceFromDB() {
        String accountId = UserSession.getInstance().getAccountId();
        
        if (accountId != null) {
            this.currentBalance = transactionService.getUserBalance(accountId);
        }
    }

    @FXML
    private void toggleBalanceVisibility() {
        isBalanceShown = !isBalanceShown;
        updateBalanceDisplay();
    }

    private void updateBalanceDisplay() {
        if (isBalanceShown) {
            balanceLabel.setText(String.format("₱ %,.2f", currentBalance));
        } else {
            balanceLabel.setText("₱ ••••••");
        }
    }

    // 5. Placeholders para sa mga Buttons
    @FXML
    private void handleCashIn(ActionEvent event) {
        try {
            // 1. I-load ang cash_in.fxml mula sa resources folder mo
            // Base sa structure mo: /com/agosbank/fxml/cash_in.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/cash_in.fxml"));
            Parent root = loader.load();

            // 2. Kunin ang kasalukuyang Stage (Window) gamit ang button na pinindot
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. I-set ang bagong Scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen(); // Para laging nasa gitna ang window
            stage.show();

            System.out.println("Navigating to Cash In... Success!");
            
        } catch (IOException e) {
            System.err.println("Error loading Cash In screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendMoney(ActionEvent event) {
         try {
            // 1. I-load ang cash_in.fxml mula sa resources folder mo
            // Base sa structure mo: /com/agosbank/fxml/cash_in.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/send_money.fxml"));
            Parent root = loader.load();

            // 2. Kunin ang kasalukuyang Stage (Window) gamit ang button na pinindot
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. I-set ang bagong Scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen(); // Para laging nasa gitna ang window
            stage.show();

            System.out.println("Navigating to Send Money... Success!");
            
        } catch (IOException e) {
            System.err.println("Error loading Send Money screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
