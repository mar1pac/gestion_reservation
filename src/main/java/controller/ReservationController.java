package controller;

import dao.ReservationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.Reservation;
import util.ValidationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private Spinner<Integer> nombrePersonnesSpinner;
    @FXML private ComboBox<String> tableComboBox;
    @FXML private TextArea specialRequestsArea;
    @FXML private Text statusMessage;
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> dateHeureCol;
    @FXML private TableColumn<Reservation, String> clientCol;
    @FXML private TableColumn<Reservation, String> tableCol;
    @FXML private TableColumn<Reservation, Integer> personnesCol;
    @FXML private TableColumn<Reservation, String> statutCol;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ReservationDAO reservationDAO;
    private ObservableList<String> heures;
    private ObservableList<String> tables;
    private Reservation selectedReservation;

    public ReservationController() {
        this.reservationDAO = new ReservationDAO();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFormControls();
        initializeTableColumns();
        setupListeners();
        loadReservations();

    }

    private void initializeFormControls() {
        setupTimeComboBox();
        setupTablesComboBox();
        setupDatePicker();
        setupSpinner();
    }

    private void initializeTableColumns() {
        dateHeureCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateHeure().format(DATE_TIME_FORMATTER)));
        clientCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClient()));
        tableCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTable()));
        personnesCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNombrePersonnes()).asObject());
        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut()));

        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> handleReservationSelection(newVal));
    }

    private void setupTimeComboBox() {
        List<String> timeSlots = new ArrayList<>();
        for (int hour = 11; hour <= 22; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
            timeSlots.add(String.format("%02d:30", hour));
        }
        heures = FXCollections.observableArrayList(timeSlots);
        heureComboBox.setItems(heures);
    }

    private void setupTablesComboBox() {
        tables = FXCollections.observableArrayList();
        tableComboBox.setItems(tables);
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        nombrePersonnesSpinner.setValueFactory(valueFactory);
        nombrePersonnesSpinner.setEditable(true);
    }

    private void setupListeners() {
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables());
        heureComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables());
        nombrePersonnesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables());
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationDAO.findAllToday();
            reservationsTable.setItems(FXCollections.observableArrayList(reservations));
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les réservations:\n" + e.getMessage());
        }
    }

    private void updateAvailableTables() {
        if (datePicker.getValue() == null || heureComboBox.getValue() == null) {
            return;
        }

        try {
            LocalDateTime selectedDateTime = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalTime.parse(heureComboBox.getValue())
            );

            int occupiedTables = reservationDAO.countOccupiedTables();
            int totalTables = 10;

            tables.clear();
            for (int i = 1; i <= totalTables - occupiedTables; i++) {
                tables.add("Table " + i);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de mettre à jour les tables disponibles:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmer() {
        if (!validateForm()) {
            return;
        }

        try {
            Reservation reservation = createReservationFromForm();
            if (selectedReservation != null) {
                reservation.setId(selectedReservation.getId());
                reservationDAO.update(reservation);
                showSuccess("Réservation mise à jour avec succès!");
                loadReservations();
            } else {
                reservationDAO.create(reservation);
                showSuccess("Réservation créée avec succès!");
            }
            clearForm();
            loadReservations();
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors de la sauvegarde de la réservation:\n" + e.getMessage());
        }
    }


    @FXML
    private void handleAnnuler() {
        selectedReservation = null;
        clearForm();
    }

    @FXML
    private void handleSupprimer() {
        if (selectedReservation == null) {
            showError("Erreur", "Veuillez sélectionner une réservation à supprimer");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                reservationDAO.delete(selectedReservation.getId());
                showSuccess("Réservation supprimée avec succès!");
                clearForm();
                loadReservations();
            } catch (SQLException e) {
                showError("Erreur", "Erreur lors de la suppression de la réservation:\n" + e.getMessage());
            }
        }
    }


    private void handleReservationSelection(Reservation reservation) {
        selectedReservation = reservation;
        if (reservation != null) {
            try {
                Reservation selectedReservationFromDB = reservationDAO.findById(reservation.getId());
                if (selectedReservationFromDB != null) {
                    populateForm(selectedReservationFromDB);
                } else {
                    showError("Erreur", "Réservation introuvable");
                }
            } catch (SQLException e) {
                showError("Erreur", "Impossible de charger la réservation:\n" + e.getMessage());
            }
        }
    }
    @FXML
    private void handleClose() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }


    private boolean validateForm() {
        if (datePicker.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une date");
            return false;
        }
        if (heureComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une heure");
            return false;
        }
        if (tableComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une table");
            return false;
        }

        LocalDateTime selectedDateTime = LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.parse(heureComboBox.getValue())
        );

        if (!ValidationUtil.isValidReservation(selectedDateTime)) {
            showError("Temps de réservation invalide", "L'heure de réservation doit être dans le futur.");
            return false;
        }

        return true;
    }


    private Reservation createReservationFromForm() {
        Reservation reservation = new Reservation();
        String tableStr = tableComboBox.getValue().replace("Table ", "");
        int tableId = Integer.parseInt(tableStr);

        LocalDateTime dateTime = LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.parse(heureComboBox.getValue())
        );

        reservation.setTableId(tableId);
        reservation.setUserId(1);
        reservation.setDateHeure(dateTime);
        reservation.setNombrePersonnes(nombrePersonnesSpinner.getValue());
        reservation.setStatut("CONFIRMEE");

        return reservation;
    }

    private void populateForm(Reservation reservation) {
        datePicker.setValue(reservation.getDateHeure().toLocalDate());
        heureComboBox.setValue(reservation.getDateHeure().toLocalTime().toString());
        nombrePersonnesSpinner.getValueFactory().setValue(reservation.getNombrePersonnes());
        tableComboBox.setValue("Table " + reservation.getTableId());
    }

    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        heureComboBox.setValue(null);
        nombrePersonnesSpinner.getValueFactory().setValue(2);
        tableComboBox.setValue(null);
        specialRequestsArea.clear();
        statusMessage.setVisible(false);
        selectedReservation = null;
    }

    private void showSuccess(String message) {
        statusMessage.setStyle("-fx-fill: green;");
        statusMessage.setText(message);
        statusMessage.setVisible(true);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}