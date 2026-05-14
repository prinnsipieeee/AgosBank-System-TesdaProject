package com.agosbank.main;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.agosbank.models.Transaction;
import com.agosbank.services.TransactionService;
import com.agosbank.utils.SceneSwitcher;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    @FXML private Label greetingLabel;
    @FXML private Label balanceLabel;
    @FXML private ImageView toggleIcon; 
    @FXML private VBox dashboardHistoryContainer;

    private final TransactionService transactionService = new TransactionService();
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
        loadRecentHistory();
    }

    private void loadBalanceFromDB() {
        String accountId = UserSession.getInstance().getAccountId();
        
        if (accountId != null) {
            this.currentBalance = transactionService.getUserBalance(accountId);
        }
    }

    @FXML
    @SuppressWarnings("unused")
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

    @FXML
    @SuppressWarnings("unused")
    private void handleCashIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/cash_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigating to Cash In... Success!");
        } catch (IOException e) {
            System.err.println("Error loading Cash In screen: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleWithdraw(MouseEvent event) {
        try {
            String path = "/com/agosbank/fxml/withdraw.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {            
            System.err.println("Error: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSendMoney(ActionEvent event) {
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agosbank/fxml/send_money.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Navigating to Send Money... Success!");
        } catch (IOException e) {
            System.err.println("Error loading Send Money screen: " + e.getMessage());
        }
    }

    public void loadRecentHistory() {
        dashboardHistoryContainer.getChildren().clear();
        TransactionService service = new TransactionService();
        
        // Kunin ang "ALL" pero i-limit natin sa top 5 para sa Dashboard
        List<Transaction> history = service.getFilteredHistory(UserSession.getInstance().getAccountId(), "ALL");

        int count = 0;
        for (Transaction t : history) {
            if (count >= 5) break; 
            dashboardHistoryContainer.getChildren().add(createHistoryItem(t));
            count++;
        }
    }

    private Node createHistoryItem(Transaction transaction) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: #1e2d3e; -fx-background-radius: 12; -fx-margin: 5;");
        Label icon = new Label();
        if (transaction.getTransactionType().equals("CASH IN")) {
            icon.setText("↙"); // O kaya "➕"
            icon.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else {
            icon.setText("↗"); // O kaya "➖"
            icon.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 18px; -fx-font-weight: bold;");
        }
        VBox details = new VBox(2);
        Label typeLabel = new Label(transaction.getTransactionType());
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        String formattedDate = transaction.getDate().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a"));
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
        details.getChildren().addAll(typeLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

   
        Label amountLabel = new Label(String.format("₱%,.2f", transaction.getAmount()));
        amountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        item.getChildren().addAll(icon, details, spacer, amountLabel);
        return item;
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleNavigation(MouseEvent event) {
        VBox source = (VBox) event.getSource();
        String id = source.getId();

        switch (id) {
            case "navHome" -> System.out.println();
            case "navHistory" -> SceneSwitcher.switchScene(source, "history.fxml");
            case "navQR" -> SceneSwitcher.switchScene(source, "qrcode.fxml");
            case "navProfile" -> SceneSwitcher.switchScene(source, "account.fxml");
            default -> System.out.println();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void accountDetails(MouseEvent event) {
        try {
            String path = "/com/agosbank/fxml/account.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {
           System.err.println("Error Navigation account.fxml");
        }
    }
}
