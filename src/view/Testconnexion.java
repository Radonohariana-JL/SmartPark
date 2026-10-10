package view;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import util.DBConnection;

/**
 * Petit programme de test : verifie la connexion JDBC vers PostgreSQL.
 */
public class Testconnexion {

    public static void main(String[] args) {

        Connection con = DBConnection.getConnection();

        if (con == null) {
            System.out.println("Connexion echouee !");
            return;
        }

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM place");
            rs.next();
            System.out.println("Connexion reussie ! Places dans la base : " + rs.getInt(1));
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
