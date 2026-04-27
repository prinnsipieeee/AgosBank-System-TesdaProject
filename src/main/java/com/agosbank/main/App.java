package com.agosbank.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // LOGIN na agad ang load natin dito
        Parent root = FXMLLoader.load(getClass().getResource("/com/agosbank/fxml/login.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setTitle("AgosBank - Login");
        stage.setScene(scene);
        stage.setResizable(false); // Para hindi ma-stretch ang mobile design natin
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}