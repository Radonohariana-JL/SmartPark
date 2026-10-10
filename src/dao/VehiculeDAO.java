package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Vehicule;
import util.DBConnection;

/**
 * Acces aux donnees de la table VEHICULE.
 */
public class VehiculeDAO {

    public List<Vehicule> lister() {
        return rechercher("");
    }

    public List<Vehicule> rechercher(String mot) {
        List<Vehicule> liste = new ArrayList<>();
        String sql = "SELECT v.id_vehicule, v.immatriculation, v.marque, v.couleur, "
                + "v.type_vehicule, v.id_proprietaire, p.nom || ' ' || p.prenom AS proprietaire "
                + "FROM vehicule v "
                + "JOIN proprietaire p ON p.id_proprietaire = v.id_proprietaire "
                + "WHERE v.immatriculation ILIKE ? OR v.marque ILIKE ? "
                + "OR p.nom ILIKE ? OR p.prenom ILIKE ? "
                + "ORDER BY v.id_vehicule";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String motCle = "%" + mot + "%";
            ps.setString(1, motCle);
            ps.setString(2, motCle);
            ps.setString(3, motCle);
            ps.setString(4, motCle);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setIdVehicule(rs.getInt("id_vehicule"));
                    v.setImmatriculation(rs.getString("immatriculation"));
                    v.setMarque(rs.getString("marque"));
                    v.setCouleur(rs.getString("couleur"));
                    v.setTypeVehicule(rs.getString("type_vehicule"));
                    v.setIdProprietaire(rs.getInt("id_proprietaire"));
                    v.setNomProprietaire(rs.getString("proprietaire"));
                    liste.add(v);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur VehiculeDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    public void ajouter(Vehicule v) {
        String sql = "INSERT INTO vehicule (immatriculation, marque, couleur, type_vehicule, id_proprietaire) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getImmatriculation());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getCouleur());
            ps.setString(4, v.getTypeVehicule());
            ps.setInt(5, v.getIdProprietaire());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur VehiculeDAO.ajouter : " + e.getMessage());
        }
    }

    public void modifier(Vehicule v) {
        String sql = "UPDATE vehicule SET immatriculation = ?, marque = ?, couleur = ?, "
                + "type_vehicule = ?, id_proprietaire = ? WHERE id_vehicule = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getImmatriculation());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getCouleur());
            ps.setString(4, v.getTypeVehicule());
            ps.setInt(5, v.getIdProprietaire());
            ps.setInt(6, v.getIdVehicule());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur VehiculeDAO.modifier : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM vehicule WHERE id_vehicule = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur VehiculeDAO.supprimer : " + e.getMessage());
        }
    }
}
