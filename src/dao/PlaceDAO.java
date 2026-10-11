package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return liste;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
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
            }
        } catch (Exception e) {
            System.err.println("Erreur PlaceDAO.lister : " + e.getMessage());
        }
        return liste;
    }

    /** Vrai si le changement d'etat a touche exactement une ligne. */
    public boolean changerEtat(int idPlace, String etat) {
        String sql = "UPDATE place SET etat = ? WHERE id_place = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, etat);
                ps.setInt(2, idPlace);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur PlaceDAO.changerEtat : " + e.getMessage());
            return false;
        }
    }

    public int compterLibres() {
        String sql = "SELECT COUNT(*) FROM place WHERE etat = 'LIBRE'";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return 0;
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur PlaceDAO.compterLibres : " + e.getMessage());
        }
        return 0;
    }
}