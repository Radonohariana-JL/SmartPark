package smartpark;

import javax.swing.SwingUtilities;
import util.I18n;
import util.Theme;
import view.Login;

/**
 * Point d'entree de l'application SmartPark.
 * Applique le theme et la langue enregistres avant d'afficher la connexion.
 */
public class SmartPark {

    public static void main(String[] args) {
        // Preferences chargees/validees avant toute construction d'interface.
        Theme.appliquerAuDemarrage();
        I18n.charger(util.PreferencesApp.getLangue());
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}