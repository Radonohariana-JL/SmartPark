package model;

/**
 * Modele de la table VEHICULE (Merise).
 */
public class Vehicule {

    private int idVehicule;
    private String immatriculation;
    private String marque;
    private String couleur;
    private String typeVehicule;
    private int idProprietaire;

    // Champ non stocke en base : sert uniquement a l'affichage dans les tableaux.
    private String nomProprietaire;

    public Vehicule() {
    }

    public Vehicule(String immatriculation, String marque, String couleur,
                    String typeVehicule, int idProprietaire) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.couleur = couleur;
        this.typeVehicule = typeVehicule;
        this.idProprietaire = idProprietaire;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getTypeVehicule() {
        return typeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }

    public int getIdProprietaire() {
        return idProprietaire;
    }

    public void setIdProprietaire(int idProprietaire) {
        this.idProprietaire = idProprietaire;
    }

    public String getNomProprietaire() {
        return nomProprietaire;
    }

    public void setNomProprietaire(String nomProprietaire) {
        this.nomProprietaire = nomProprietaire;
    }

    @Override
    public String toString() {
        return immatriculation + " - " + marque;
    }
}
