package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe unique de connexion a PostgreSQL.
 * Toutes les classes du projet utilisent DBConnection.getConnection().
 */
public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/smartpark";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Cause technique complete dans les logs, sans masquer l'echec.
            System.err.println("[DBConnection] echec de connexion a la base de donnees : "
                    + e.getMessage());
            for (Throwable t : e) {
                System.err.println("[DBConnection]   cause : " + t.getMessage());
            }
            return null;
        }
    }

    /** Vrai si une connexion a la base est etablie et close immediatement. */
    public static boolean estDisponible() {
        Connection con = getConnection();
        if (con == null) {
            return false;
        }
        try {
            con.close();
        } catch (SQLException ignore) {
            // fermeture best effort
        }
        return true;
    }
}
