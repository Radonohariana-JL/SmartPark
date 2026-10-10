package model;

/**
 * Modele de la table TARIF (Merise).
 */
public class Tarif {

    private int idTarif;
    private String typeVehicule;
    private double prixHeure;

    public Tarif() {
    }

    public Tarif(String typeVehicule, double prixHeure) {
        this.typeVehicule = typeVehicule;
        this.prixHeure = prixHeure;
    }

    public int getIdTarif() {
        return idTarif;
    }

    public void setIdTarif(int idTarif) {
        this.idTarif = idTarif;
    }

    public String getTypeVehicule() {
        return typeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }

    public double getPrixHeure() {
        return prixHeure;
    }

    public void setPrixHeure(double prixHeure) {
        this.prixHeure = prixHeure;
    }

    @Override
    public String toString() {
        return typeVehicule + " : " + prixHeure;
    }
}
