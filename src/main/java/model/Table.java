package model;

public class Table {
    private int id;
    private int numero;
    private int capacite;
    private String statut;

    public Table() {}

    public Table(int numero, int capacite, String statut) {
        this.numero = numero;
        this.capacite = capacite;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", numero=" + numero +
                ", capacite=" + capacite +
                ", statut='" + statut + '\'' +
                '}';
    }
}