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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        } catch (Exception e) {
            System.out.println("Erreur ProprietaireDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    public void ajouter(Proprietaire p) {
        String sql = "INSERT INTO proprietaire (nom, prenom, telephone) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getTelephone());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur ProprietaireDAO.ajouter : " + e.getMessage());
        }
    }

    public void modifier(Proprietaire p) {
        String sql = "UPDATE proprietaire SET nom = ?, prenom = ?, telephone = ? "
                + "WHERE id_proprietaire = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getPrenom());
            ps.setString(3, p.getTelephone());
            ps.setInt(4, p.getIdProprietaire());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur ProprietaireDAO.modifier : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM proprietaire WHERE id_proprietaire = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur ProprietaireDAO.supprimer : " + e.getMessage());
        }
    }
}
