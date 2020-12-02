package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // On crée le Parent qui contient le FXMLLoader "chargeur" de fxml avec l'adresse du fichier.
        Parent root = FXMLLoader.load(getClass().getResource("/main/gui/mainApp.fxml"));
        // On l'injecte dans la scene
        Scene scene = new Scene(root);
        // On assigne la scene au stage
        primaryStage.setScene(scene);
        // On nomme la scene
        primaryStage.setTitle( "Menu") ;
        // On affiche le stage
        primaryStage.show();
    }
}
