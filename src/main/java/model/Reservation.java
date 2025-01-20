package model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int tableId;
    private int userId;
    private LocalDateTime dateHeure;
    private int nombrePersonnes;
    private String statut;
    private String client;
    private String table;
    private int tableNumero;

    public Reservation() {}

    public Reservation(int tableId, int userId, LocalDateTime dateHeure, int nombrePersonnes, String statut) {
        this.tableId = tableId;
        this.userId = userId;
        this.dateHeure = dateHeure;
        this.nombrePersonnes = nombrePersonnes;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }



    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public int getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(int nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", tableId=" + tableId +
                ", userId=" + userId +
                ", dateHeure=" + dateHeure +
                ", nombrePersonnes=" + nombrePersonnes +
                ", statut='" + statut + '\'' +
                ", client='" + client + '\'' +
                ", table='" + table + '\'' +
                '}';
    }

    public int getTableNumero() {
        return tableNumero;
    }

    public void setTableNumber(int tableNumero) {
        this.tableNumero = tableNumero;
    }
}
