package com.agosbank.utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {
    public static void switchScene(Node node, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/com/agosbank/fxml/" + fxmlPath));
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Navigation Error: " + e.getMessage());
        }
    }
}
