package main.gui;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {
    public Button BtnRecherche;
    public TextField inputCodeFournis, inputAdresse, inputVille, inputCP, inputNom, inputContact;
    public Connection con;
    public PreparedStatement stmtSearch;
    public ResultSet result;
    public Label message;
    public FontIcon icon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String urlMariaDB = "jdbc:mariadb://localhost:3306/papyrus"; // https://mariadb.com/kb/en/java-connector-using-gradle/
        try {
            // Création connexion à la base de données/
            this.con = DriverManager.getConnection(urlMariaDB, "root", null); //create connection for a server installed in localhost, with a user "root" with no password

            // Création de la requête préparée de recherche du fournisseur
            this.stmtSearch = con.prepareStatement("SELECT nomfou, confou,ruefou,vilfou,posfou FROM fournis WHERE numfou=?");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // Paramétrage format du message d'erreur et de l'icône
        this.message.setStyle("-fx-text-fill: red;-fx-font-weight: bold");
        this.icon.setIconColor(Color.RED);
    }

    public void btnRecherche() {

        try {
            this.message.setVisible(false);
            this.icon.setVisible(false);
            // Définit le critère de recherche pour la requête préparée
            stmtSearch.setInt(1, Integer.parseInt(this.inputCodeFournis.getText()));

            // Exécute la requête et récupération du résultat
            result = stmtSearch.executeQuery();

            // Exploitation des résultats
            result.first();
            this.inputNom.setText(result.getString(1));
            this.inputContact.setText(result.getString(2));
            this.inputAdresse.setText(result.getString(3));
            this.inputVille.setText(result.getString(4).toUpperCase(Locale.ROOT));
            this.inputCP.setText(result.getString(5));

            // Vide le jeu de résultat
            result.close();

        } catch (Exception exception) {
            switch (exception.getClass().getSimpleName()) {
                case "NumberFormatException":
                    this.message.setVisible(true);
                    this.icon.setVisible(true);
                    this.message.setText("Le code fournisseur accepte seulement de 1 \u00E0 10 caract\u00E8res num\u00E9riques.");
                    break;

                case "SQLDataException":
                    this.message.setVisible(true);
                    this.icon.setVisible(true);
                    if (exception.getMessage().equals("Current position is after the last row")) {
                        this.message.setText("Il n'existe pas de fournisseur portant le code fournisseur : " + this.inputCodeFournis.getText());
                    }
            }
        }
    }
}