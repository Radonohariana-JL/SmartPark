package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Tarif;
import util.DBConnection;

/**
 * Acces aux donnees de la table TARIF.
 */
public class TarifDAO {

    public List<Tarif> lister() {
        List<Tarif> liste = new ArrayList<>();
        String sql = "SELECT id_tarif, type_vehicule, prix_heure FROM tarif ORDER BY id_tarif";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tarif t = new Tarif();
                t.setIdTarif(rs.getInt("id_tarif"));
                t.setTypeVehicule(rs.getString("type_vehicule"));
                t.setPrixHeure(rs.getDouble("prix_heure"));
                liste.add(t);
            }
        } catch (Exception e) {
            System.out.println("Erreur TarifDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    public void modifier(Tarif t) {
        String sql = "UPDATE tarif SET prix_heure = ? WHERE type_vehicule = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, t.getPrixHeure());
            ps.setString(2, t.getTypeVehicule());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur TarifDAO.modifier : " + e.getMessage());
        }
    }

    /** Retourne le prix par heure d'un type de vehicule, ou 0 si inexistant. */
    public double getPrixHeure(String typeVehicule) {
        String sql = "SELECT prix_heure FROM tarif WHERE type_vehicule = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, typeVehicule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("prix_heure");
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur TarifDAO.getPrixHeure : " + e.getMessage());
        }
        return 0;
    }
}
