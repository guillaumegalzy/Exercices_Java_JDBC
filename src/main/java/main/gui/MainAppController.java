package main.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import main.gui.fournisseur.Fournisseur;
import java.net.URL;
import java.sql.*;
import java.util.*;

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
    public Label errorNom,errorAdresse,errorVille,errorCP,errorContact;
    public Map<TextInputControl,Label> inputErrorMap;
    public Boolean errorTot = true;  // Initialise à true les errors avant toute vérifications
    public List<Boolean> errorInput = new ArrayList<>();
    public TitledPane confirmMessage;
    public Button btnConfirm;

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

        //  Liste des inputs avec label d'erreur associés
            inputErrorMap = new HashMap<>() ;
            inputErrorMap.put(inputNom, errorNom);
            inputErrorMap.put(inputAdresse,errorAdresse);
            inputErrorMap.put(inputVille,errorVille);
            inputErrorMap.put(inputCP,errorCP);
            inputErrorMap.put(inputContact,errorContact);
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
                    break;

                default:
                    System.out.println(exception.getMessage());
                    break;
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

    /**
     * Fonction de vérification des champs du formulaire. Le résultat de chacune des vérifications est stocké dans la List errorInput
     */
    public void Verify(){
        // Vide la list suivant les erreurs recontrées sur les champs avant toute nouvelle vérification
            errorInput.clear();

        // Vérification de chacun des champs
            for (TextInputControl input : inputErrorMap.keySet()) {
                // Vérification spécifique à chaque input
                switch (input.idProperty().get()){
                    case "inputNom":
                        if(input.getText().matches("[\\D]{1,25}")){
                            this.errorInput.add(false);
                            inputErrorMap.get(input).setVisible(false);
                            input.setStyle("");
                        }else{
                            this.errorInput.add(true);
                            input.setStyle("-fx-border-color: red;-fx-border-width: 0.5pt");
                            inputErrorMap.get(input).setVisible(true);
                        }
                        break;

                    case "inputAdresse":
                        if(input.getText().matches("[\\d\\D]{1,50}")){
                            this.errorInput.add(false);
                            inputErrorMap.get(input).setVisible(false);
                            input.setStyle("");
                        }else{
                            this.errorInput.add(true);
                            input.setStyle("-fx-border-color: red;-fx-border-width: 0.5pt");
                            inputErrorMap.get(input).setVisible(true);
                        }
                        break;

                    case "inputCP":
                        if(input.getText().matches("[\\d]{5}")){
                            this.errorInput.add(false);
                            inputErrorMap.get(input).setVisible(false);
                            input.setStyle("");
                        }else{
                            this.errorInput.add(true);
                            input.setStyle("-fx-border-color: red;-fx-border-width: 0.5pt");
                            inputErrorMap.get(input).setVisible(true);
                        }
                        break;

                    case "inputVille":
                        if(input.getText().matches("[\\D]{1,30}")){
                            this.errorInput.add(false);
                            inputErrorMap.get(input).setVisible(false);
                            input.setStyle("");
                        }else{
                            this.errorInput.add(true);
                            input.setStyle("-fx-border-color: red;-fx-border-width: 0.5pt");
                            inputErrorMap.get(input).setVisible(true);
                        }
                        break;

                    case "inputContact":
                        if(input.getText().matches("[\\D]{1,15}")){
                            this.errorInput.add(false);
                            inputErrorMap.get(input).setVisible(false);
                            input.setStyle("");
                        }else{
                            this.errorInput.add(true);
                            input.setStyle("-fx-border-color: red;-fx-border-width: 0.5pt");
                            inputErrorMap.get(input).setVisible(true);
                        }
                        break;

                }
            }

        // Si aucune erreur rencontré sur les champs, alors OK pour requête ajout
            if (!errorInput.contains(true)){
                errorTot = false;
            }
    }

    public void Ajouter() throws SQLException {
        // Vérification des champs
            Verify();

        // Pas de requête s'il y a au moins une erreur
        if(!errorTot) {
            System.out.println("ok");
            // Récupère le dernier numéro de fournisseur
            Statement lastCodeStmt = con.createStatement();
            ResultSet indexLastCode = lastCodeStmt.executeQuery("Select MAX (numfou) from fournis");
            indexLastCode.first();

            // Crée une instance de la classe fournisseur
            Fournisseur newFourni = new Fournisseur(indexLastCode.getInt(1), this.inputNom.getText(), this.inputAdresse.getText(), this.inputCP.getText(), this.inputVille.getText(), this.inputContact.getText());

            // Insertion dans la BDD
            PreparedStatement insertOne = con.prepareStatement("INSERT INTO fournis (numfou,nomfou,ruefou,posfou,vilfou,confou) VALUES (?,?,?,?,?,?)");
            insertOne.setInt(1, newFourni.getNumfou() + 1); // Incrémentation du dernier numéro de fournisseur par 1
            insertOne.setString(2, newFourni.getNomfou());
            insertOne.setString(3, newFourni.getRuefou());
            insertOne.setString(4, newFourni.getPosfou());
            insertOne.setString(5, newFourni.getVilfou());
            insertOne.setString(6, newFourni.getContact());
            insertOne.execute();
            this.confirmMessage.setVisible(true);
        }

        errorTot = true; // Réinitialise le booleen d'erreur pour prochain ajout
    }

    public void Annuler() {
        // Vide tous les champs et réinitialise les mises en forme de vérification du formulaire
            for (TextInputControl input: inputErrorMap.keySet()) {
                input.clear();
                input.setStyle("");
                inputErrorMap.get(input).setVisible(false);
            }
    }

    public void hideConfirmMessage() {
        this.confirmMessage.setVisible(false);
        Annuler();
    }
}