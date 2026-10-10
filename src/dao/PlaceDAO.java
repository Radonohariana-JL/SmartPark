package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Place;
import util.DBConnection;

/**
 * Acces aux donnees de la table PLACE.
 */
public class PlaceDAO {

    public List<Place> listerToutes() {
        return lister("");
    }

    public List<Place> listerLibres() {
        return lister("LIBRE");
    }

    private List<Place> lister(String etat) {
        List<Place> liste = new ArrayList<>();
        String sql = "SELECT id_place, numero_place, etat FROM place ";
        if (!etat.isEmpty()) {
            sql += "WHERE etat = ? ";
        }
        sql += "ORDER BY numero_place";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!etat.isEmpty()) {
                ps.setString(1, etat);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Place p = new Place();
                    p.setIdPlace(rs.getInt("id_place"));
                    p.setNumeroPlace(rs.getString("numero_place"));
                    p.setEtat(rs.getString("etat"));
                    liste.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur PlaceDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    public void changerEtat(int idPlace, String etat) {
        String sql = "UPDATE place SET etat = ? WHERE id_place = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, etat);
            ps.setInt(2, idPlace);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur PlaceDAO.changerEtat : " + e.getMessage());
        }
    }

    public int compterLibres() {
        String sql = "SELECT COUNT(*) FROM place WHERE etat = 'LIBRE'";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Erreur PlaceDAO.compterLibres : " + e.getMessage());
        }
        return 0;
    }
}
