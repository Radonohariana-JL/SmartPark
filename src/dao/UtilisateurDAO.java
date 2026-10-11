package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Utilisateur;
import util.DBConnection;
import util.MotDePasse;

/**
 * Acces aux donnees de la table UTILISATEUR (authentification).
 * Les mots de passe sont stockes sous forme de hachage SHA-256 hexadecimal.
 */
public class UtilisateurDAO {

    /**
     * Verifie l'email et le mot de passe (hachage SHA-256).
     * Retourne l'utilisateur connecte, ou null si l'identification echoue.
     * NB : une base indisponible renvoie aussi null ; distinguer avec
     * {@link DBConnection#estDisponible()}.
     */
    public Utilisateur verifier(String email, String motDePasse) {
        String sql = "SELECT id_utilisateur, nom, email, mot_de_passe, role "
                + "FROM utilisateur WHERE email = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    String stocke = rs.getString("mot_de_passe");
                    if (!correspondre(motDePasse, stocke)) {
                        return null;
                    }
                    Utilisateur u = new Utilisateur();
                    u.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    u.setNom(rs.getString("nom"));
                    u.setEmail(rs.getString("email"));
                    u.setMotDePasse(stocke);
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur UtilisateurDAO.verifier : " + e.getMessage());
        }
        return null;
    }

    public boolean modifierNom(int idUtilisateur, String nouveauNom) {
        String sql = "UPDATE utilisateur SET nom = ? WHERE id_utilisateur = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nouveauNom);
                ps.setInt(2, idUtilisateur);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur modifierNom : " + e.getMessage());
            return false;
        }
    }

    /**
     * Cree un utilisateur. Le mot de passe est hache en SHA-256 avant insertion :
     * aucune valeur en clair n'est conservee ni journalisee.
     */
    public boolean ajouter(String nom, String email, String motDePasse, String role) {
        String sql = "INSERT INTO utilisateur (nom, email, mot_de_passe, role) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nom);
                ps.setString(2, email);
                ps.setString(3, MotDePasse.hacher(motDePasse));
                ps.setString(4, role);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur UtilisateurDAO.ajouter : " + e.getMessage());
            return false;
        }
    }

    /**
     * Change le mot de passe apres verification de l'ancien (creation de hacha).
     */
    public boolean changerMotDePasse(int idUtilisateur, String ancienMotDePasse, String nouveauMotDePasse) {
        if (ancienMotDePasse == null || nouveauMotDePasse == null) {
            return false;
        }
        String lect = "SELECT mot_de_passe FROM utilisateur WHERE id_utilisateur = ?";
        String maj = "UPDATE utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            String stocke;
            try (PreparedStatement ps = con.prepareStatement(lect)) {
                ps.setInt(1, idUtilisateur);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    stocke = rs.getString("mot_de_passe");
                }
            }
            if (!correspondre(ancienMotDePasse, stocke)) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(maj)) {
                ps.setString(1, MotDePasse.hacher(nouveauMotDePasse));
                ps.setInt(2, idUtilisateur);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur changerMotDePasse : " + e.getMessage());
            return false;
        }
    }

    /**
     * Confronte un mot de passe en clair a la valeur stockee.
     * Compare au hachage SHA-256 ; pour les lignes pas encore migrees
     * (valeur non hachée), retombe sur la comparaison en clair afin de ne
     * jamais bloquer l'authentification pendant la migration.
     */
    private static boolean correspondre(String clair, String stocke) {
        if (clair == null || stocke == null) {
            return false;
        }
        if (MotDePasse.estHache(stocke)) {
            return MotDePasse.verifier(clair, stocke);
        }
        return stocke.equals(clair);
    }
}