package dao;

import model.Table;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    public void create(Table table) throws SQLException {
        String sql = "INSERT INTO tables (numero, capacite, statut) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, table.getNumero());
            pstmt.setInt(2, table.getCapacite());
            pstmt.setString(3, table.getStatut());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    table.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Table findById(int id) throws SQLException {
        String sql = "SELECT * FROM tables WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Table table = new Table();
                table.setId(rs.getInt("id"));
                table.setNumero(rs.getInt("numero"));
                table.setCapacite(rs.getInt("capacite"));
                table.setStatut(rs.getString("statut"));
                return table;
            }
        }
        return null;
    }

    public List<Table> findAll() throws SQLException {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Table table = new Table();
                table.setId(rs.getInt("id"));
                table.setNumero(rs.getInt("numero"));
                table.setCapacite(rs.getInt("capacite"));
                table.setStatut(rs.getString("statut"));
                tables.add(table);
            }
        }
        return tables;
    }

    public List<Table> findAvailable() throws SQLException {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE statut = 'DISPONIBLE' ORDER BY numero";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Table table = new Table();
                table.setId(rs.getInt("id"));
                table.setNumero(rs.getInt("numero"));
                table.setCapacite(rs.getInt("capacite"));
                table.setStatut(rs.getString("statut"));
                tables.add(table);
            }
        }
        return tables;
    }

    public void update(Table table) throws SQLException {
        String sql = "UPDATE tables SET numero = ?, capacite = ?, statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, table.getNumero());
            pstmt.setInt(2, table.getCapacite());
            pstmt.setString(3, table.getStatut());
            pstmt.setInt(4, table.getId());

            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tables WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public boolean isTableAvailable(int tableId, Timestamp dateHeure) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE table_id = ? " +
                "AND date_heure = ? AND statut != 'ANNULEE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableId);
            pstmt.setTimestamp(2, dateHeure);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return false;
    }
}
