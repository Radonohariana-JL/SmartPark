package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import model.Stationnement;
import util.DBConnection;

/**
 * Acces aux donnees de la table STATIONNEMENT.
 * Contient aussi la logique metier : entree, sortie, historique et statistiques.
 * Les operations de modification sont atomiques (transaction + rollback).
 */
public class StationnementDAO {

    private static final String COLONNES
            = "SELECT s.id_stationnement, s.date_entree, s.date_sortie, s.duree, s.montant, "
            + "s.statut, s.id_vehicule, s.id_place, "
            + "v.immatriculation, v.type_vehicule, "
            + "p.nom || ' ' || p.prenom AS proprietaire, "
            + "pl.numero_place "
            + "FROM stationnement s "
            + "JOIN vehicule v ON v.id_vehicule = s.id_vehicule "
            + "JOIN proprietaire p ON p.id_proprietaire = v.id_proprietaire "
            + "JOIN place pl ON pl.id_place = s.id_place ";

    // ---------------------------------------------------------------
    // ENTREE : creation d'un stationnement
    // ---------------------------------------------------------------
    /**
     * Enregistre une entree de maniere atomique.
     * Refuse si le vehicule est deja gare (EN_COURS) ou si la place n'est
     * pas LIBRE au moment de la validation (SELECT ... FOR UPDATE).
     */
    public ResultatOperation creerEntree(int idVehicule, int idPlace) {
        String sqlVerifVehicule = "SELECT 1 FROM stationnement "
                + "WHERE id_vehicule = ? AND statut = 'EN_COURS'";
        String sqlVerifPlace = "SELECT etat FROM place WHERE id_place = ? FOR UPDATE";
        String sql = "INSERT INTO stationnement (date_entree, statut, id_vehicule, id_place) "
                + "VALUES (?, 'EN_COURS', ?, ?)";
        String sqlPlace = "UPDATE place SET etat = 'OCCUPEE' WHERE id_place = ?";

        Connection con = DBConnection.getConnection();
        try {
            if (con == null) {
                return ResultatOperation.echec("Base de donnees indisponible.");
            }
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlVerifVehicule)) {
                ps.setInt(1, idVehicule);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        return ResultatOperation.echec(
                                "Ce vehicule possede deja un stationnement en cours.");
                    }
                }
            }

            String etatPlace;
            try (PreparedStatement ps = con.prepareStatement(sqlVerifPlace)) {
                ps.setInt(1, idPlace);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return ResultatOperation.echec("Place introuvable.");
                    }
                    etatPlace = rs.getString("etat");
                }
            }
            if (!"LIBRE".equals(etatPlace)) {
                con.rollback();
                return ResultatOperation.echec("Cette place est deja occupee.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, idVehicule);
                ps.setInt(3, idPlace);
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return ResultatOperation.echec("Echec de l'enregistrement de l'entree.");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlPlace)) {
                ps.setInt(1, idPlace);
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return ResultatOperation.echec("Echec de la mise a jour de la place.");
                }
            }

            con.commit();
            return ResultatOperation.succes("Entree enregistree.");
        } catch (Exception e) {
            annuler(con, "creerEntree", e);
            return ResultatOperation.echec("Erreur lors de l'enregistrement de l'entree.");
        } finally {
            fermer(con);
        }
    }

    // ---------------------------------------------------------------
    // SORTIE : fin d'un stationnement
    // ---------------------------------------------------------------
    /**
     * Cloture un stationnement de maniere atomique.
     * Refuse une double sortie (statut deja TERMINE) et un tarif manquant.
     * Calcule la duree et le montant dans la transaction a partir de la base.
     */
    public ResultatOperation terminer(Stationnement s, double prixHeure) {
        if (prixHeure <= 0) {
            return ResultatOperation.echec(
                    "Aucun tarif defini pour ce type de vehicule : cloture impossible.");
        }

        String sqlLect = "SELECT date_entree, id_place, statut "
                + "FROM stationnement WHERE id_stationnement = ? FOR UPDATE";
        String sql = "UPDATE stationnement SET date_sortie = ?, duree = ?, montant = ?, "
                + "statut = 'TERMINE' WHERE id_stationnement = ? AND statut = 'EN_COURS'";
        String sqlPlace = "UPDATE place SET etat = 'LIBRE' WHERE id_place = ?";

        Connection con = DBConnection.getConnection();
        try {
            if (con == null) {
                return ResultatOperation.echec("Base de donnees indisponible.");
            }
            con.setAutoCommit(false);

            LocalDateTime dateEntree;
            int idPlace;
            try (PreparedStatement ps = con.prepareStatement(sqlLect)) {
                ps.setInt(1, s.getIdStationnement());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return ResultatOperation.echec("Stationnement introuvable.");
                    }
                    Timestamp ts = rs.getTimestamp("date_entree");
                    dateEntree = ts == null ? LocalDateTime.now() : ts.toLocalDateTime();
                    idPlace = rs.getInt("id_place");
                    if (!"EN_COURS".equals(rs.getString("statut"))) {
                        con.rollback();
                        return ResultatOperation.echec(
                                "La sortie a deja ete enregistree pour ce stationnement.");
                    }
                }
            }

            LocalDateTime maintenant = LocalDateTime.now();
            double duree = ChronoUnit.MINUTES.between(dateEntree, maintenant) / 60.0;
            if (duree < 1) {
                duree = 1;
            }
            duree = Math.round(duree * 100.0) / 100.0;
            double montant = Math.round(duree * prixHeure * 100.0) / 100.0;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(maintenant));
                ps.setDouble(2, duree);
                ps.setDouble(3, montant);
                ps.setInt(4, s.getIdStationnement());
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return ResultatOperation.echec("Echec de la cloture du stationnement.");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlPlace)) {
                ps.setInt(1, idPlace);
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return ResultatOperation.echec("Echec de la liberation de la place.");
                }
            }

            con.commit();
            return ResultatOperation.succes("Sortie enregistree.", duree, montant);
        } catch (Exception e) {
            annuler(con, "terminer", e);
            return ResultatOperation.echec("Erreur lors de l'enregistrement de la sortie.");
        } finally {
            fermer(con);
        }
    }

    private static void fermer(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception ignore) {
                // fermeture best effort
            }
        }
    }

    private static void annuler(Connection con, String methode, Exception e) {
        System.err.println("Erreur StationnementDAO." + methode + " : " + e.getMessage());
        try {
            con.rollback();
        } catch (Exception ex) {
            System.err.println("StationnementDAO." + methode + " : rollback impossible : "
                    + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // LECTURES
    // ---------------------------------------------------------------
    /** Liste des vehicules actuellement garés (statut EN_COURS). */
    public List<Stationnement> listerEnCours() {
        List<Stationnement> liste = new ArrayList<>();
        String sql = COLONNES + "WHERE s.statut = 'EN_COURS' ORDER BY s.date_entree";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return liste;
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(remplir(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur StationnementDAO.listerEnCours : " + e.getMessage());
        }
        return liste;
    }

    /** Historique complet, filtre par immatriculation ou nom du proprietaire. */
    public List<Stationnement> listerHistorique(String mot) {
        List<Stationnement> liste = new ArrayList<>();
        String sql = COLONNES
                + "WHERE v.immatriculation ILIKE ? OR p.nom ILIKE ? OR p.prenom ILIKE ? "
                + "OR pl.numero_place ILIKE ? "
                + "ORDER BY s.date_entree DESC";

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
                        liste.add(remplir(rs));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur StationnementDAO.listerHistorique : " + e.getMessage());
        }
        return liste;
    }

    private Stationnement remplir(ResultSet rs) throws Exception {
        Stationnement s = new Stationnement();
        s.setIdStationnement(rs.getInt("id_stationnement"));

        Timestamp entree = rs.getTimestamp("date_entree");
        if (entree != null) {
            s.setDateEntree(entree.toLocalDateTime());
        }
        Timestamp sortie = rs.getTimestamp("date_sortie");
        if (sortie != null) {
            s.setDateSortie(sortie.toLocalDateTime());
        }
        s.setDuree(rs.getDouble("duree"));
        s.setMontant(rs.getDouble("montant"));
        s.setStatut(rs.getString("statut"));
        s.setIdVehicule(rs.getInt("id_vehicule"));
        s.setIdPlace(rs.getInt("id_place"));
        s.setImmatriculation(rs.getString("immatriculation"));
        s.setTypeVehicule(rs.getString("type_vehicule"));
        s.setNomProprietaire(rs.getString("proprietaire"));
        s.setNumeroPlace(rs.getString("numero_place"));
        return s;
    }

    // ---------------------------------------------------------------
    // STATISTIQUES
    // ---------------------------------------------------------------
    public int vehiculesActuellementGares() {
        return compter("SELECT COUNT(*) FROM stationnement WHERE statut = 'EN_COURS'");
    }

    public int stationnementsAujourdhui() {
        return compter("SELECT COUNT(*) FROM stationnement "
                + "WHERE date(date_entree) = CURRENT_DATE");
    }

    public double recettesAujourdhui() {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM stationnement "
                + "WHERE statut = 'TERMINE' AND date(date_sortie) = CURRENT_DATE";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return 0;
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur StationnementDAO.recettesAujourdhui : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Recettes des 7 derniers jours.
     * Retourne un tableau [n][2] : { "jj/mm", montant }.
     */
    public String[][] recettesParJour() {
        String sql = "SELECT to_char(d::date, 'DD/MM') AS jour, "
                + "COALESCE(SUM(s.montant), 0) AS total "
                + "FROM generate_series(CURRENT_DATE - 6, CURRENT_DATE, interval '1 day') AS d "
                + "LEFT JOIN stationnement s "
                + "ON s.statut = 'TERMINE' AND date(s.date_sortie) = d::date "
                + "GROUP BY d "
                + "ORDER BY d";

        List<String[]> lignes = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                return lignes.toArray(new String[0][]);
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lignes.add(new String[]{rs.getString("jour"), String.valueOf(rs.getDouble("total"))});
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur StationnementDAO.recettesParJour : " + e.getMessage());
        }
        return lignes.toArray(new String[0][]);
    }

    private int compter(String sql) {
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
            System.err.println("Erreur StationnementDAO.compter : " + e.getMessage());
        }
        return 0;
    }
}