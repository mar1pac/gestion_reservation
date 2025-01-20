package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import dao.TableDAO;
import model.Table;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionTablesController {
    @FXML private TableView<Table> tablesTable;
    @FXML private TableColumn<Table, Integer> numeroCol;
    @FXML private TableColumn<Table, Integer> capaciteCol;
    @FXML private TableColumn<Table, String> statutCol;
    @FXML private TableColumn<Table, Void> actionsCol;

    @FXML private TextField numeroField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final TableDAO tableDAO;
    private Table selectedTable;

    public GestionTablesController() {
        this.tableDAO = new TableDAO();
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        initializeFormControls();
        loadTables();

        tablesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedTable = newSelection;
                    if (newSelection != null) {
                        showTableDetails(newSelection);
                    }
                }
        );
    }

    private void initializeTableColumns() {
        numeroCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNumero()).asObject());
        capaciteCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacite()).asObject());
        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut()));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> {
                    Table table = getTableRow().getItem();
                    if (table != null) {
                        showTableDetails(table);
                    }
                });

                deleteButton.setOnAction(e -> {
                    Table table = getTableRow().getItem();
                    if (table != null) {
                        handleDeleteTable(table);
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

    private void initializeFormControls() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4);
        capaciteSpinner.setValueFactory(valueFactory);

        ObservableList<String> statuts = FXCollections.observableArrayList(
                "DISPONIBLE", "OCCUPEE", "RESERVEE", "HORS_SERVICE"
        );
        statutComboBox.setItems(statuts);

        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "TOUTES", "DISPONIBLES"
        );
        filterStatusComboBox.setItems(filterOptions);
        filterStatusComboBox.setValue("TOUTES");
        filterStatusComboBox.setOnAction(e -> handleFilterChange());

        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 11; hour <= 23; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
            timeSlots.add(String.format("%02d:30", hour));
        }
        timeComboBox.setItems(timeSlots);

        clearButton.setOnAction(e -> clearForm());
    }

    private void loadTables() {
        try {
            List<Table> tables = tableDAO.findAll();
            tablesTable.setItems(FXCollections.observableArrayList(tables));
        } catch (SQLException e) {
            showError("Erreur lors du chargement des tables", e);
        }
    }

    @FXML
    private void handleFilterChange() {
        try {
            if (filterStatusComboBox.getValue().equals("DISPONIBLES")) {
                List<Table> availableTables = tableDAO.findAvailable();
                tablesTable.setItems(FXCollections.observableArrayList(availableTables));
            } else {
                loadTables();
            }
        } catch (SQLException e) {
            showError("Erreur lors du filtrage des tables", e);
        }
    }

    private void showTableDetails(Table table) {
        if (table != null) {
            numeroField.setText(String.valueOf(table.getNumero()));
            capaciteSpinner.getValueFactory().setValue(table.getCapacite());
            statutComboBox.setValue(table.getStatut());
            saveButton.setText("Modifier");
        }
    }

    private void refreshTableData(int tableId) {
        try {
            Table refreshedTable = tableDAO.findById(tableId);
            if (refreshedTable != null) {
                ObservableList<Table> items = tablesTable.getItems();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getId() == tableId) {
                        items.set(i, refreshedTable);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du rafraîchissement des données de la table", e);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            Table table;
            if (selectedTable != null) {
                table = selectedTable;
            } else {
                table = new Table();
            }

            table.setNumero(Integer.parseInt(numeroField.getText()));
            table.setCapacite(capaciteSpinner.getValue());
            table.setStatut(statutComboBox.getValue());

            if (selectedTable != null) {
                tableDAO.update(table);
            } else {
                tableDAO.create(table);
            }

            loadTables();
            clearForm();
        } catch (SQLException e) {
            showError("Erreur lors de l'enregistrement de la table", e);
        }
    }

    private void handleDeleteTable(Table table) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Suppression de la table " + table.getNumero());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette table ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tableDAO.delete(table.getId());
                loadTables();
                clearForm();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression de la table", e);
            }
        }
    }

    @FXML
    private void checkTableAvailability() {
        if (selectedTable == null || datePicker.getValue() == null || timeComboBox.getValue() == null) {
            showError("Vérification impossible", "Veuillez sélectionner une table, une date et une heure");
            return;
        }

        try {
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeComboBox.getValue());
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            Timestamp timestamp = Timestamp.valueOf(dateTime);

            boolean isAvailable = tableDAO.isTableAvailable(selectedTable.getId(), timestamp);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Disponibilité de la table");
            alert.setHeaderText(null);
            alert.setContentText(isAvailable ?
                    "La table est disponible à cette date/heure" :
                    "La table n'est pas disponible à cette date/heure");
            alert.showAndWait();
        } catch (SQLException e) {
            showError("Erreur lors de la vérification de disponibilité", e);
        }
    }

    @FXML
    private void clearForm() {
        selectedTable = null;
        numeroField.clear();
        capaciteSpinner.getValueFactory().setValue(4);
        statutComboBox.setValue(null);
        datePicker.setValue(null);
        timeComboBox.setValue(null);
        saveButton.setText("Ajouter");
    }

    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        if (numeroField.getText().isEmpty() || !numeroField.getText().matches("\\d+")) {
            errorMessage.append("Le numéro de table doit être un nombre valide\n");
        }

        if (capaciteSpinner.getValue() == null || capaciteSpinner.getValue() < 1) {
            errorMessage.append("La capacité doit être supérieure à 0\n");
        }

        if (statutComboBox.getValue() == null) {
            errorMessage.append("Le statut est requis\n");
        }

        if (errorMessage.length() > 0) {
            showError("Erreurs de validation", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        showError("Erreur", message + "\n" + e.getMessage());
        e.printStackTrace();
    }
}