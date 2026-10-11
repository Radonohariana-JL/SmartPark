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

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return liste;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
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
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.rechercher : " + e.getMessage());
        }
        return liste;
    }

    /** Vrai si l'insertion a cree exactement une ligne. */
    public boolean ajouter(Vehicule v) {
        String sql = "INSERT INTO vehicule (immatriculation, marque, couleur, type_vehicule, id_proprietaire) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, v.getImmatriculation());
                ps.setString(2, v.getMarque());
                ps.setString(3, v.getCouleur());
                ps.setString(4, v.getTypeVehicule());
                ps.setInt(5, v.getIdProprietaire());
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.ajouter : " + e.getMessage());
            return false;
        }
    }

    /**
     * Insere un vehicule et retourne l'identifiant genere (ou -1 en cas
     * d'echec). Utilise par la creation rapide depuis la page Entree afin de
     * selectionner immediatement le vehicule cree.
     */
    public int ajouterAvecId(Vehicule v) {
        String sql = "INSERT INTO vehicule (immatriculation, marque, couleur, type_vehicule, id_proprietaire) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return -1;
            }
            try (PreparedStatement ps = con.prepareStatement(sql,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, v.getImmatriculation());
                ps.setString(2, v.getMarque());
                ps.setString(3, v.getCouleur());
                ps.setString(4, v.getTypeVehicule());
                ps.setInt(5, v.getIdProprietaire());
                if (ps.executeUpdate() != 1) {
                    return -1;
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        v.setIdVehicule(id);
                        return id;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.ajouterAvecId : " + e.getMessage());
        }
        return -1;
    }

    /** Vrai si la modification a touche exactement une ligne. */
    public boolean modifier(Vehicule v) {
        String sql = "UPDATE vehicule SET immatriculation = ?, marque = ?, couleur = ?, "
                + "type_vehicule = ?, id_proprietaire = ? WHERE id_vehicule = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, v.getImmatriculation());
                ps.setString(2, v.getMarque());
                ps.setString(3, v.getCouleur());
                ps.setString(4, v.getTypeVehicule());
                ps.setInt(5, v.getIdProprietaire());
                ps.setInt(6, v.getIdVehicule());
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.modifier : " + e.getMessage());
            return false;
        }
    }

    /** Vrai si la suppression a supprime exactement une ligne. */
    public boolean supprimer(int id) {
        String sql = "DELETE FROM vehicule WHERE id_vehicule = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() == 1;
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.supprimer : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vrai si l'immatriculation est deja utilisee par une autre ligne.
     * La contrainte UNIQUE en base reste la garantie finale.
     */
    public boolean estImmatriculationUtilisee(String immatriculation, Integer idExclu) {
        String sql = "SELECT 1 FROM vehicule WHERE immatriculation = ?";
        if (idExclu != null) {
            sql += " AND id_vehicule <> " + idExclu;
        }
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, immatriculation);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.estImmatriculationUtilisee : " + e.getMessage());
            return false;
        }
    }

    /**
     * Nombre de stationnements lies a un vehicule. Permet aux vues de refuser
     * la suppression tant que des historiques existent en base (FK sans CASCADE).
     */
    public int compterStationnements(int idVehicule) {
        String sql = "SELECT COUNT(*) FROM stationnement WHERE id_vehicule = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return -1;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idVehicule);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur VehiculeDAO.compterStationnements : " + e.getMessage());
        }
        return -1;
    }
}