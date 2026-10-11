package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Proprietaire;
import util.DBConnection;

/**
 * Acces aux donnees de la table PROPRIETAIRE.
 */
public class ProprietaireDAO {

    public List<Proprietaire> lister() {
        return rechercher("");
    }

    public List<Proprietaire> rechercher(String mot) {
        List<Proprietaire> liste = new ArrayList<>();
        String sql = "SELECT id_proprietaire, nom, prenom, telephone FROM proprietaire "
                + "WHERE nom ILIKE ? OR prenom ILIKE ? OR telephone ILIKE ? "
                + "ORDER BY id_proprietaire";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return liste;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String motCle = "%" + mot + "%";
                ps.setString(1, motCle);
                ps.setString(2, motCle);
                ps.setString(3, motCle);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Proprietaire p = new Proprietaire();
                        p.setIdProprietaire(rs.getInt("id_proprietaire"));
                        p.setNom(rs.getString("nom"));
                        p.setPrenom(rs.getString("prenom"));
                        p.setTelephone(rs.getString("telephone"));
                        liste.add(p);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.rechercher : " + e.getMessage());
        }
        return liste;
    }

    /** Vrai si l'insertion a cree exactement une ligne. */
    public boolean ajouter(Proprietaire p) {
        String sql = "INSERT INTO proprietaire (nom, prenom, telephone) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getNom());
                ps.setString(2, p.getPrenom());
                ps.setString(3, p.getTelephone());
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.ajouter : " + e.getMessage());
            return false;
        }
    }

    /**
     * Insere un proprietaire et retourne l'identifiant genere (ou -1 en cas
     * d'echec). Utilise par la creation rapide depuis la page Entree afin de
     * selectionner immediatement le proprietaire cree.
     */
    public int ajouterAvecId(Proprietaire p) {
        String sql = "INSERT INTO proprietaire (nom, prenom, telephone) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return -1;
            }
            try (PreparedStatement ps = con.prepareStatement(sql,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNom());
                ps.setString(2, p.getPrenom());
                ps.setString(3, p.getTelephone());
                if (ps.executeUpdate() != 1) {
                    return -1;
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        p.setIdProprietaire(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.ajouterAvecId : " + e.getMessage());
        }
        return -1;
    }

    /** Vrai si la modification a touche exactement une ligne. */
    public boolean modifier(Proprietaire p) {
        String sql = "UPDATE proprietaire SET nom = ?, prenom = ?, telephone = ? "
                + "WHERE id_proprietaire = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getNom());
                ps.setString(2, p.getPrenom());
                ps.setString(3, p.getTelephone());
                ps.setInt(4, p.getIdProprietaire());
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.modifier : " + e.getMessage());
            return false;
        }
    }

    /** Vrai si la suppression a supprime exactement une ligne. */
    public boolean supprimer(int id) {
        String sql = "DELETE FROM proprietaire WHERE id_proprietaire = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.supprimer : " + e.getMessage());
            return false;
        }
    }

    /**
     * Nombre de vehicules rattaches a un proprietaire. Permet aux vues de
     * refuser la suppression tant que des relations existent en base.
     */
    public int compterVehicules(int idProprietaire) {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE id_proprietaire = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return -1;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idProprietaire);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur ProprietaireDAO.compterVehicules : " + e.getMessage());
        }
        return -1;
    }
}