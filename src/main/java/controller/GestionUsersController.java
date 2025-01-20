package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.User;
import dao.UserDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Optional;

public class GestionUsersController implements Initializable {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> nomCol;
    @FXML private TableColumn<User, String> prenomCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> actionsCol;

    @FXML private TextField emailField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox statsBox;

    private UserDAO userDAO;
    private User selectedUser;
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userDAO = new UserDAO();

        roleComboBox.setItems(FXCollections.observableArrayList(
                "ADMIN", "USER"
        ));

        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        setupActionsColumn();

        loadUsers();

        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedUser = newSelection;
                        populateFields(newSelection);
                    }
                }
        );

        updateStats();
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        selectedUser = user;
                        populateFields(user);
                    }
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleDelete(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        try {
            User user = new User(
                    emailField.getText(),
                    passwordField.getText(),
                    roleComboBox.getValue(),
                    nomField.getText(),
                    prenomField.getText()
            );

            if (selectedUser != null) {
                user.setId(selectedUser.getId());
                userDAO.update(user);
            } else {
                if (userDAO.emailExists(user.getEmail())) {
                    showAlert("Erreur", "Cet email existe déjà.");
                    return;
                }
                userDAO.create(user);
            }

            clearForm();
            loadUsers();
            updateStats();
            showAlert("Succès", "Utilisateur sauvegardé avec succès.");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        emailField.clear();
        nomField.clear();
        prenomField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        selectedUser = null;
        usersTable.getSelectionModel().clearSelection();
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userDAO.delete(user.getId());
                loadUsers();
                updateStats();
                showAlert("Succès", "Utilisateur supprimé avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void loadUsers() {
        try {
            usersList.clear();
            usersList.addAll(userDAO.findAll());
            usersTable.setItems(usersList);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }

    private void updateStats() {
        try {
            statsBox.getChildren().clear();
            // Only show stats for USER and ADMIN roles
            String[] roles = {"ADMIN", "USER"};
            for (String role : roles) {
                int count = userDAO.countByRole(role);
                Label label = new Label(role + ": " + count + " utilisateur(s)");
                statsBox.getChildren().add(label);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour des statistiques: " + e.getMessage());
        }
    }

    private void populateFields(User user) {
        emailField.setText(user.getEmail());
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRole());
    }

    private boolean validateFields() {
        return !emailField.getText().isEmpty() &&
                !nomField.getText().isEmpty() &&
                !prenomField.getText().isEmpty() &&
                !passwordField.getText().isEmpty() &&
                roleComboBox.getValue() != null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}