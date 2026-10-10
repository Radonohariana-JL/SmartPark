package model;

/**
 * Modele de la table PLACE (Merise).
 */
public class Place {

    public static final String LIBRE = "LIBRE";
    public static final String OCCUPEE = "OCCUPEE";

    private int idPlace;
    private String numeroPlace;
    private String etat;

    public Place() {
    }

    public Place(int idPlace, String numeroPlace, String etat) {
        this.idPlace = idPlace;
        this.numeroPlace = numeroPlace;
        this.etat = etat;
    }

    public int getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(int idPlace) {
        this.idPlace = idPlace;
    }

    public String getNumeroPlace() {
        return numeroPlace;
    }

    public void setNumeroPlace(String numeroPlace) {
        this.numeroPlace = numeroPlace;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    @Override
    public String toString() {
        return numeroPlace;
    }
}
