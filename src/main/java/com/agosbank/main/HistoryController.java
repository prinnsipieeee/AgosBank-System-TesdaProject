package com.agosbank.main;

import java.io.IOException;
import java.util.List;

import com.agosbank.models.Transaction;
import com.agosbank.services.TransactionService;
import com.agosbank.utils.SceneSwitcher;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HistoryController {

    @FXML private VBox fullHistoryContainer;
    @FXML private ToggleGroup dateFilterGroup;
    @FXML private Pane detailsOverlay;
    @FXML private Label lblDetType, lblDetAmount, lblDetRef, lblDetName, lblDetDate;

    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        // Default: Ipakita lahat ng history pag-load ng page
        loadHistory("ALL");

        // Listener para sa ToggleGroup - para automatic mag-filter pagka-click
        dateFilterGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selectedBtn = (ToggleButton) newToggle;
                loadHistory(selectedBtn.getText().toUpperCase());
            }
        });
    }

    private void loadHistory(String filter) {
        fullHistoryContainer.getChildren().clear();
        String currentAcc = UserSession.getInstance().getAccountId();
        
        List<Transaction> logs = transactionService.getFilteredHistory(currentAcc, filter);

        if (logs.isEmpty()) {
            System.out.println("No transactions for filter: " + filter);
            return;
        }

        for (Transaction t : logs) {
            fullHistoryContainer.getChildren().add(createHistoryItem(t));
        }
    }

     private Node createHistoryItem(Transaction transaction) {
        HBox item = new HBox(15);

        item.setOnMouseClicked(event -> showTransactionDetails(transaction));
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

        // 2. Transaction Details (Type + Date)
        VBox details = new VBox(2);
        Label typeLabel = new Label(transaction.getTransactionType());
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Formatting the date para mas malinis
        String formattedDate = transaction.getDate().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a"));
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
        details.getChildren().addAll(typeLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Amount
        Label amountLabel = new Label(String.format("₱%,.2f", transaction.getAmount()));
        amountLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        item.getChildren().addAll(icon, details, spacer, amountLabel);
        return item;
    }

    private void showTransactionDetails(Transaction transaction){
        lblDetType.setText(transaction.getTransactionType());
        lblDetAmount.setText(String.format("₱%,.2f", transaction.getAmount()));
        lblDetRef.setText(transaction.getAccountId());
        
        // Check kung sino ang papangalanan (Sender o Receiver)
        String displayName = transaction.getTransactionType().equals("CASH IN") ? transaction.getSenderName() : transaction.getReceiverName();
        lblDetName.setText(displayName);
        
        lblDetDate.setText(transaction.getDate().toString());

        // 2. Show the overlay
        detailsOverlay.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), detailsOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    @FXML
    private void closeDetails() {
        detailsOverlay.setVisible(false);
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
    private void handleNavigation(MouseEvent event) {
        // Kuhanin natin kung anong VBox ang pinindot
        VBox source = (VBox) event.getSource();
        String id = source.getId();

        switch (id) {
            case "navHome" -> SceneSwitcher.switchScene(source, "dashboard.fxml");
            case "navHistory" -> System.out.println("Already In History");
            case "navQR" -> SceneSwitcher.switchScene(source, "qrcode.fxml");
            case "navProfile" -> SceneSwitcher.switchScene(source, "account.fxml");
            default -> System.out.println();
        }
    }
}