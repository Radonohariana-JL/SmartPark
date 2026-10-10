package smartpark;

import view.Login;

/**
 * Point d'entree de l'application SmartPark.
 * Affiche la fenetre de connexion.
 */
public class SmartPark {

    public static void main(String[] args) {
        Login login = new Login();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }
}