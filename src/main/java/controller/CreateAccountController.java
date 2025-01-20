package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.User;
import dao.UserDAO;
import util.ValidationUtil;

public class CreateAccountController {
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleCreateAccount() {
        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            showError("Invalid Password", "Password must be at least 8 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas", "Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showError("Cet email est déjà utilisé", "L'email que vous avez entré est déjà utilisé.");
                return;
            }

            User newUser = new User();
            newUser.setPrenom(prenom);
            newUser.setNom(nom);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole("USER");

            userDAO.create(newUser);
            showSuccess("Compte créé avec succès");
            navigateToLogin();

        } catch (Exception e) {
            showError("Erreur lors de la création du compte", "Une erreur est survenue lors de la création de votre compte.");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBack() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/restaurant/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
