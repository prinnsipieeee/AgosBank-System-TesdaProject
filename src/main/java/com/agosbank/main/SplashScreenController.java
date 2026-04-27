package com.agosbank.main;

import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class SplashScreenController {

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        // Gagawa tayo ng "Task" para mag-simulate ng loading
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    updateProgress(i, 100);
                    Thread.sleep(30); // 3 seconds total loading
                }
                return null;
            }
        };

        // I-bind ang progress bar sa task
        progressBar.progressProperty().bind(task.progressProperty());

        // Kapag tapos na ang loading, lilipat na sa Login Screen
        task.setOnSucceeded(e -> {
            try {
                // 1. I-load ang login.fxml
                Parent root = FXMLLoader.load(getClass().getResource("/com/agosbank/fxml/login.fxml"));
                
                // 2. Kunin ang kasalukuyang Stage (Window)
                Stage stage = (Stage) progressBar.getScene().getWindow();
                
                // 3. Palitan ang Scene ng Login Scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                
                // 4. I-center ang window dahil nag-iba tayo ng size
                stage.centerOnScreen();
                
            } catch (IOException ex) {
                System.err.println("Error: Hindi mahanap ang login.fxml! Check mo yung path.");
                ex.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}