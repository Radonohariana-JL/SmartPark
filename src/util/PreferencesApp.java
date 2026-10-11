package util;

import java.util.prefs.Preferences;

/**
 * Preferences locales de SmartPark (theme, langue, devise).
 *
 * Le stockage s'appuie sur {@link java.util.prefs.Preferences} : aucune table
 * SQL ni modification du schema PostgreSQL n'est necessaire. Les preferences
 * sont conservees par utilisateur/systeme et survivent a la fermeture de
 * l'application.
 */
public final class PreferencesApp {

    /** Noeud de configuration SmartPark. */
    private static final Preferences PREFS =
            Preferences.userRoot().node("smartpark/settings");

    // --- Valeurs de theme ---
    public static final String THEME_CLAIR = "CLAIR";
    public static final String THEME_SOMBRE = "SOMBRE";

    // --- Valeurs de langue ---
    public static final String LANGUE_FR = "fr";
    public static final String LANGUE_MG = "mg";
    public static final String LANGUE_EN = "en";

    // --- Valeurs de devise ---
    public static final String DEVISE_MGA = "MGA";
    public static final String DEVISE_USD = "USD";
    public static final String DEVISE_GBP = "GBP";

    private PreferencesApp() {
    }

    public static String getTheme() {
        return PREFS.get("theme", THEME_CLAIR);
    }

    public static void setTheme(String theme) {
        enregistrer("theme", normaliser(theme, THEME_CLAIR,
                THEME_CLAIR, THEME_SOMBRE));
    }

    public static String getLangue() {
        return PREFS.get("langue", LANGUE_FR);
    }

    public static void setLangue(String langue) {
        enregistrer("langue", normaliser(langue, LANGUE_FR,
                LANGUE_FR, LANGUE_MG, LANGUE_EN));
    }

    public static String getDevise() {
        return PREFS.get("devise", DEVISE_MGA);
    }

    public static void setDevise(String devise) {
        enregistrer("devise", normaliser(devise, DEVISE_MGA,
                DEVISE_MGA, DEVISE_USD, DEVISE_GBP));
    }

    /**
     * Remet toutes les preferences aux valeurs par defaut. Utile pour les tests
     * et pour une eventuelle reinitialisation explicite.
     */
    public static void reinitialiser() {
        try {
            PREFS.clear();
            PREFS.flush();
        } catch (Exception e) {
            System.err.println("[PreferencesApp] reinitialisation impossible : "
                    + e.getMessage());
        }
    }

    private static void enregistrer(String cle, String valeur) {
        try {
            PREFS.put(cle, valeur);
            PREFS.flush();
        } catch (Exception e) {
            // Preference non bloquante : en cas d'echec, l'application continue
            // avec les valeurs par defaut plutot que de planter.
            System.err.println("[PreferencesApp] enregistrement '" + cle
                    + "' impossible : " + e.getMessage());
        }
    }

    /** Retourne valeur si elle figure parmi les choix, sinon le defaut. */
    private static String normaliser(String valeur, String defaut,
            String... choix) {
        if (valeur != null) {
            for (String c : choix) {
                if (c.equals(valeur)) {
                    return valeur;
                }
            }
        }
        return defaut;
    }
}
