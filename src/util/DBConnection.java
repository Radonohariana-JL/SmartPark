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
            System.out.println("Erreur de connexion : " + e.getMessage());
            return null;
        }
    }
}
