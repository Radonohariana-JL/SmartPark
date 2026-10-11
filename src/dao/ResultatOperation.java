package dao;

/**
 * Resultat d'une operation metier terminee par les DAO : succes/echec,
 * message explicite pour l'interface et, le cas echoant, valeurs calculees
 * (durée, montant) restituees a la vue.
 */
public class ResultatOperation {

    private final boolean succes;
    private final String message;
    private final double duree;
    private final double montant;

    private ResultatOperation(boolean succes, String message, double duree, double montant) {
        this.succes = succes;
        this.message = message;
        this.duree = duree;
        this.montant = montant;
    }

    public static ResultatOperation succes(String message) {
        return new ResultatOperation(true, message, 0, 0);
    }

    public static ResultatOperation succes(String message, double duree, double montant) {
        return new ResultatOperation(true, message, duree, montant);
    }

    public static ResultatOperation echec(String message) {
        return new ResultatOperation(false, message, 0, 0);
    }

    public boolean estSucces() {
        return succes;
    }

    public String getMessage() {
        return message;
    }

    public double getDuree() {
        return duree;
    }

    public double getMontant() {
        return montant;
    }
}