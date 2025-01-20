package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import dao.ReservationDAO;
import model.Reservation;

import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {
    @FXML private Label tablesOccupeesLabel;
    @FXML private Label reservationsJourLabel;
    @FXML private TableView<Reservation> prochainesReservationsTable;
    @FXML private TableColumn<Reservation, String> dateHeureCol;
    @FXML private TableColumn<Reservation, String> clientCol;
    @FXML private TableColumn<Reservation, String> tableCol;
    @FXML private TableColumn<Reservation, Integer> personnesCol;
    @FXML private TableColumn<Reservation, String> statutCol;

    private final ReservationDAO reservationDAO;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminDashboardController() {
        this.reservationDAO = new ReservationDAO();
    }

    @FXML
    public void initialize() {
        initializeTableColumns();
        loadDashboardData();
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
    }

    private void loadDashboardData() {
        try {
            List<Reservation> reservations = reservationDAO.findAllToday();
            prochainesReservationsTable.setItems(FXCollections.observableArrayList(reservations));

            tablesOccupeesLabel.setText(String.valueOf(reservationDAO.countOccupiedTables()));
            reservationsJourLabel.setText(String.valueOf(reservations.size()));
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les données du tableau de bord:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleGestionTables() {
        openWindow("/org/example/restaurant/gestion_tables.fxml", "Gestion des Tables");
    }

    @FXML
    private void handleGestionUtilisateurs() {
        openWindow("/org/example/restaurant/gestion_users.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    private void handleGestionReservations() {
        openWindow("/org/example/restaurant/reservation.fxml", "Gestion des Réservations");
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre " + title + ":\n" + e.getMessage());
        }
    }

    @FXML
    private void handleRapportJour() {
        try {
            List<Reservation> reservationsJour = reservationDAO.findAllToday();
            afficherRapportJour(reservationsJour);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de générer le rapport journalier:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleStatistiques() {
        try {
            Stage statsStage = new Stage();
            VBox statsContainer = new VBox(10);
            statsContainer.setPadding(new Insets(10));

            PieChart pieChart = createReservationsPieChart();
            statsContainer.getChildren().add(pieChart);

            TextArea statsArea = new TextArea();
            statsArea.setEditable(false);
            statsArea.setPrefRowCount(5);
            statsArea.setText(generateStatsText());
            statsContainer.getChildren().add(statsArea);

            Scene scene = new Scene(statsContainer, 600, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            statsStage.setTitle("Statistiques");
            statsStage.setScene(scene);
            statsStage.show();
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'afficher les statistiques:\n" + e.getMessage());
        }
    }

    private String generateStatsText() throws SQLException {
        int totalReservations = reservationDAO.findAllToday().size();
        int tablesOccupees = reservationDAO.countOccupiedTables();
        int reservationsMatin = reservationDAO.countReservationsByPeriod("MATIN");
        int reservationsMidi = reservationDAO.countReservationsByPeriod("MIDI");
        int reservationsSoir = reservationDAO.countReservationsByPeriod("SOIR");

        return String.format("""
            Statistiques du %s
            Total des réservations: %d
            Tables occupées: %d
            Réservations du matin: %d
            Réservations du midi: %d
            Réservations du soir: %d
            """,
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                totalReservations, tablesOccupees,
                reservationsMatin, reservationsMidi, reservationsSoir
        );
    }

    @FXML
    private void handleExportReservations() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les réservations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Date,Client,Table,Personnes,Statut");

                List<Reservation> reservations = reservationDAO.findAll();
                for (Reservation reservation : reservations) {
                    writer.printf("%s,%s,%s,%d,%s%n",
                            reservation.getDateHeure().format(DATE_TIME_FORMATTER),
                            reservation.getClient().replace(",", ";"),
                            reservation.getTable(),
                            reservation.getNombrePersonnes(),
                            reservation.getStatut()
                    );
                }
                showInfo("Export réussi", "Les réservations ont été exportées avec succès.");
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de l'exportation des réservations:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/restaurant/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage currentStage = (Stage) tablesOccupeesLabel.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la déconnexion:\n" + e.getMessage());
        }
    }

    private PieChart createReservationsPieChart() throws SQLException {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition des réservations");

        int matin = reservationDAO.countReservationsByPeriod("MATIN");
        int midi = reservationDAO.countReservationsByPeriod("MIDI");
        int soir = reservationDAO.countReservationsByPeriod("SOIR");

        pieChart.getData().addAll(
                new PieChart.Data("Matin", matin),
                new PieChart.Data("Midi", midi),
                new PieChart.Data("Soir", soir)
        );

        return pieChart;
    }

    private void afficherRapportJour(List<Reservation> reservations) throws SQLException {
        Stage rapportStage = new Stage();
        VBox rapportContainer = new VBox(10);
        rapportContainer.setPadding(new Insets(10));

        Label titreLabel = new Label("Rapport des réservations du " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        titreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label statsLabel = new Label(String.format("""
            Nombre total de réservations : %d
            Tables occupées : %d
            Nombre total de couverts : %d""",
                reservations.size(),
                reservationDAO.countOccupiedTables(),
                reservations.stream().mapToInt(Reservation::getNombrePersonnes).sum()
        ));

        TableView<Reservation> tableView = createReservationsTableView();
        tableView.setItems(FXCollections.observableArrayList(reservations));

        rapportContainer.getChildren().addAll(titreLabel, statsLabel, tableView);

        Scene scene = new Scene(rapportContainer, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        rapportStage.setTitle("Rapport Journalier");
        rapportStage.setScene(scene);
        rapportStage.show();
    }

    private TableView<Reservation> createReservationsTableView() {
        TableView<Reservation> tableView = new TableView<>();

        TableColumn<Reservation, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateHeure().format(DATE_TIME_FORMATTER)));

        TableColumn<Reservation, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClient()));

        TableColumn<Reservation, String> tableCol = new TableColumn<>("Table");
        tableCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTable()));

        TableColumn<Reservation, Integer> personnesCol = new TableColumn<>("Personnes");
        personnesCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNombrePersonnes()).asObject());

        TableColumn<Reservation, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut()));

        tableView.getColumns().addAll(dateCol, clientCol, tableCol, personnesCol, statutCol);
        return tableView;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}