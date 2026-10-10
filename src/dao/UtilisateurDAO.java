package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Utilisateur;
import util.DBConnection;

/**
 * Acces aux donnees de la table UTILISATEUR (authentification).
 */
public class UtilisateurDAO {

    /**
     * Verifie l'email et le mot de passe.
     * Retourne l'utilisateur connecte, ou null si l'identification echoue.
     */
    public Utilisateur verifier(String email, String motDePasse) {
        String sql = "SELECT id_utilisateur, nom, email, mot_de_passe, role "
                + "FROM utilisateur WHERE email = ? AND mot_de_passe = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, motDePasse);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur u = new Utilisateur();
                    u.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    u.setNom(rs.getString("nom"));
                    u.setEmail(rs.getString("email"));
                    u.setMotDePasse(rs.getString("mot_de_passe"));
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur UtilisateurDAO.verifier : " + e.getMessage());
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

    // NB : mot de passe stocké en clair (comme verifier()). Hachage à envisager plus tard.
    public boolean changerMotDePasse(int idUtilisateur, String ancienMotDePasse, String nouveauMotDePasse) {
        String verif = "SELECT 1 FROM utilisateur WHERE id_utilisateur = ? AND mot_de_passe = ?";
        String maj = "UPDATE utilisateur SET mot_de_passe = ? WHERE id_utilisateur = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(verif)) {
                ps.setInt(1, idUtilisateur);
                ps.setString(2, ancienMotDePasse);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(maj)) {
                ps.setString(1, nouveauMotDePasse);
                ps.setInt(2, idUtilisateur);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur changerMotDePasse : " + e.getMessage());
            return false;
        }
    }
}
