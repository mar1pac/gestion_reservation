package dao;

import model.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public void create(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (table_id, user_id, date_heure, nombre_personnes, statut) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, reservation.getTableId());
            pstmt.setInt(2, reservation.getUserId());
            pstmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateHeure()));
            pstmt.setInt(4, reservation.getNombrePersonnes());
            pstmt.setString(5, reservation.getStatut());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }

            // Add success message here
            System.out.println("Reservation created successfully with ID: " + reservation.getId());
        }
    }





    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT r.*, u.nom || ' ' || u.prenom as client_name, t.numero as table_numero " +
                "FROM reservations r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN tables t ON r.table_id = t.id " +
                "WHERE r.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println(rs.next());
                return mapReservationFromResultSet(rs);
            }
        }
        return null;
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.nom || ' ' || u.prenom as client_name, t.numero as table_numero " +
                "FROM reservations r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN tables t ON r.table_id = t.id " +
                "ORDER BY date_heure DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reservations.add(mapReservationFromResultSet(rs));
            }
        }
        return reservations;
    }

    public List<Reservation> findAllToday() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.nom || ' ' || u.prenom as client_name, t.numero as table_numero " +
                "FROM reservations r " +
                "JOIN users u ON r.user_id = u.id " +
                "JOIN tables t ON r.table_id = t.id " +
                "WHERE DATE(date_heure) = CURRENT_DATE " +
                "ORDER BY date_heure";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.next());
                reservations.add(mapReservationFromResultSet(rs));
            }

        }
        return reservations;
    }

    public void update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations SET table_id = ?, user_id = ?, date_heure = ?, " +
                "nombre_personnes = ?, statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservation.getTableId());
            pstmt.setInt(2, reservation.getUserId());
            pstmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateHeure()));
            pstmt.setInt(4, reservation.getNombrePersonnes());
            pstmt.setString(5, reservation.getStatut());
            pstmt.setInt(6, reservation.getId());

            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int countOccupiedTables() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT table_id) FROM reservations " +
                "WHERE DATE(date_heure) = CURRENT_DATE AND statut = 'CONFIRMEE'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countReservationsByPeriod(String period) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE DATE(date_heure) = CURRENT_DATE " +
                "AND statut = 'CONFIRMEE' " +
                "AND CASE ? " +
                "    WHEN 'MATIN' THEN EXTRACT(HOUR FROM date_heure) < 12 " +
                "    WHEN 'MIDI' THEN EXTRACT(HOUR FROM date_heure) BETWEEN 12 AND 15 " +
                "    WHEN 'SOIR' THEN EXTRACT(HOUR FROM date_heure) > 15 " +
                "END";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, period);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getInt(1));
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Reservation mapReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
                rs.getInt("table_id"),
                rs.getInt("user_id"),
                rs.getTimestamp("date_heure").toLocalDateTime(),
                rs.getInt("nombre_personnes"),
                rs.getString("statut")
        );

        reservation.setId(rs.getInt("id"));
        reservation.setClient(rs.getString("client_name"));
        reservation.setTable("Table " + rs.getInt("table_numero"));

        return reservation;
    }
}