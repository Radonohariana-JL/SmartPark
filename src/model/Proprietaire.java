package model;

/**
 * Modele de la table PROPRIETAIRE (Merise).
 */
public class Proprietaire {

    private int idProprietaire;
    private String nom;
    private String prenom;
    private String telephone;

    public Proprietaire() {
    }

    public Proprietaire(String nom, String prenom, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    public int getIdProprietaire() {
        return idProprietaire;
    }

    public void setIdProprietaire(int idProprietaire) {
        this.idProprietaire = idProprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
