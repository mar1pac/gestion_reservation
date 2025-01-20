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

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private UserDAO userDAO = new UserDAO();

    public void handleCreateAccount() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/restaurant/create_account.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Créer un compte");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (!ValidationUtil.isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            showError("Invalid Password", "Password must be at least 8 characters.");
            return;
        }

        try {
            User user = userDAO.findByEmail(email);
            if (user != null && password.equals(user.getPassword())) {
                String fxmlFile = user.getRole().equals("ADMIN") ?
                        "/org/example/restaurant/admin_dashboard.fxml" : "/org/example/restaurant/reservation.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
            } else {
                showError("Identifiants invalides", "Incorrect email or password. Please try again.");
            }

        } catch (Exception e) {
            showError("Erreur de connexion", "An unexpected error occurred. Please try again later.");
            e.printStackTrace();
        }
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}