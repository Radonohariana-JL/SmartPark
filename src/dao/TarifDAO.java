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

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return liste;
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Tarif t = new Tarif();
                    t.setIdTarif(rs.getInt("id_tarif"));
                    t.setTypeVehicule(rs.getString("type_vehicule"));
                    t.setPrixHeure(rs.getDouble("prix_heure"));
                    liste.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur TarifDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    /** Vrai si la modification de tarif a touche exactement une ligne. */
    public boolean modifier(Tarif t) {
        String sql = "UPDATE tarif SET prix_heure = ? WHERE type_vehicule = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, t.getPrixHeure());
                ps.setString(2, t.getTypeVehicule());
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur TarifDAO.modifier : " + e.getMessage());
            return false;
        }
    }

    /** Retourne le prix par heure d'un type de vehicule, ou 0 si inexistant. */
    public double getPrixHeure(String typeVehicule) {
        String sql = "SELECT prix_heure FROM tarif WHERE type_vehicule = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return 0;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, typeVehicule);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("prix_heure");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur TarifDAO.getPrixHeure : " + e.getMessage());
        }
        return 0;
    }
}