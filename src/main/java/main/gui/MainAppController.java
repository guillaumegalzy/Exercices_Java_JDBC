package main.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.gui.fournisseur.Fournisseur;
import java.net.URL;
import java.sql.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {
    public Button BtnRecherche,btnAffiche,btnAfficheCmd;
    public TextField inputCodeFournis, inputAdresse, inputVille, inputCP, inputNom, inputContact;
    public Connection con;
    public PreparedStatement stmtSearch;
    public PreparedStatement stmtCombo;
    public ResultSet result,resultCombo;
    public Label message;
    public VBox Saisie_windows,Select_windows;
    public ComboBox<String> combo;
    public ObservableList<String> fournisseurListCombo = FXCollections.observableArrayList();
    public TextArea textCmd;
    public Button btnAjouter;
    public Button btnAnnuler;

//    public FontIcon icon;

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
//        this.icon.setIconColor(Color.RED);
    }

    public void btnRecherche() {

        try {
            this.message.setVisible(false);
//            this.icon.setVisible(false);
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
//                    this.icon.setVisible(true);
                    this.message.setText("Le code fournisseur accepte seulement de 1 \u00E0 10 caract\u00E8res num\u00E9riques.");
                    break;

                case "SQLDataException":
                    this.message.setVisible(true);
//                    this.icon.setVisible(true);
                    if (exception.getMessage().equals("Current position is after the last row")) {
                        this.message.setText("Il n'existe pas de fournisseur portant le code fournisseur : " + this.inputCodeFournis.getText());
                    }
            }
        }
    }

    public void ToggleAffich() throws SQLException {
        this.Saisie_windows.setVisible(!this.Saisie_windows.isVisible());
        this.Select_windows.setVisible(!this.Select_windows.isVisible());

        // Dans le cas où on arrive sur la fenêtre comportant la combobox
        if(this.Select_windows.isVisible()){
            // Réinitialisation des inputs, de la combobox et affichage
                this.textCmd.clear();
                this.combo.setValue(null);
                this.fournisseurListCombo.clear();

            // Création de la requête préparée alimentant la comboBox
                this.stmtCombo = con.prepareStatement("SELECT nomfou from fournis ORDER BY 1 ASC");

            // Exécution de la requête préparée alimentant la comboBox
                resultCombo = this.stmtCombo.executeQuery();

            // Ajout dans un premier temps la possibilité de tous sélectionner
                fournisseurListCombo.add("TOUS");

            // Récupération du contenu de la requête dans une ObservableList<String>
                while (resultCombo.next()){
                    fournisseurListCombo.addAll(resultCombo.getString(1));
                }

            // Ajout des éléments dans la comboBox à partir de l'OberservableList<String>
                combo.setItems(fournisseurListCombo);
        }
    }

    public void changeCombo() throws SQLException {

        // Clear la textArea
        this.textCmd.clear();

        // Si 'Tous' sélectionner, affiche toutes les commandes en cours
        if(this.combo.getValue().equals("TOUS")){
            Statement getAll = con.createStatement();
            ResultSet cmd = getAll.executeQuery("SELECT numcom,obscom,datcom,nomfou from entcom JOIN fournis f on entcom.numfou = f.numfou ORDER BY nomfou ASC ");
            while (cmd.next()){
                this.textCmd.appendText(
                        "Fournisseur : " + cmd.getString(4) + "\n" +
                        "Num\u00E9ro de commande : " + cmd.getString(1) + "\n" +
                        "Observation commande : " + cmd.getString(2)+ "\n" +
                        "Date de commande : " + cmd.getString(3)+ "\n" +
                        System.lineSeparator()
                );
            }
        }else{
            PreparedStatement getOne = con.prepareStatement("SELECT numcom,obscom,datcom,nomfou from entcom JOIN fournis f on entcom.numfou = f.numfou WHERE nomfou =?");
            getOne.setString(1,this.combo.getValue());
            ResultSet cmd = getOne.executeQuery();
            while (cmd.next()){
                this.textCmd.appendText(
                        "Fournisseur : " + cmd.getString(4) + "\n" +
                        "Num\u00E9ro de commande : " + cmd.getString(1) + "\n" +
                        "Observation commande : " + cmd.getString(2)+ "\n" +
                        "Date de commande : " + cmd.getString(3)+ "\n" +
                        System.lineSeparator()
                );
            }
        }
    }

    public void Ajouter() throws SQLException {
        // Récupère le dernier numéro de fournisseur
            Statement lastCodeStmt = con.createStatement();
            ResultSet indexLastCode = lastCodeStmt.executeQuery("Select MAX (numfou) from fournis");
            indexLastCode.first();

        // Crée une instance de la classe fournisseur
            Fournisseur newFourni = new Fournisseur(indexLastCode.getInt(1),this.inputNom.getText(),this.inputAdresse.getText(),this.inputCP.getText(),this.inputVille.getText(),this.inputContact.getText());

        // Insertion dans la BDD
            PreparedStatement insertOne = con.prepareStatement("INSERT INTO fournis (numfou,nomfou,ruefou,posfou,vilfou,confou) VALUES (?,?,?,?,?,?)");
            insertOne.setInt(1,newFourni.getNumfou()+1); // Incrémentation du dernier numéro de fournisseur par 1
            insertOne.setString(2,newFourni.getNomfou());
            insertOne.setString(3,newFourni.getRuefou());
            insertOne.setString(4,newFourni.getPosfou());
            insertOne.setString(5,newFourni.getVilfou());
            insertOne.setString(6,newFourni.getContact());
            insertOne.execute();
    }

    public void Annuler() {
        this.inputCP.clear();
        this.inputAdresse.clear();
        this.inputNom.clear();
        this.inputContact.clear();
        this.inputVille.clear();
    }
}