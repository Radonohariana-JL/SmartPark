package util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalisation de SmartPark (architecture reutilisable).
 *
 * <p>Les textes sont resolus depuis des fichiers {@link ResourceBundle}
 * ({@code view/Messages_fr.properties}, {@code view/Messages_mg.properties} et
 * {@code view/Messages_en.properties}), encodes en UTF-8 (Java 9+). La langue
 * courante est conservee dans {@link PreferencesApp}.</p>
 *
 * <p>Convention d'appel : {@code I18n.t("clef", "Texte par defaut")}. Si la
 * clef est absente du bundle, le texte par defaut est renvoye : aucune chaine
 * n'est jamais vide et l'application reste utilisable.</p>
 */
public final class I18n {

    private static ResourceBundle bundle;
    private static String langue;

    static {
        charger(PreferencesApp.getLangue());
    }

    private I18n() {
    }

    /** Recharge le bundle pour la langue demandee ("fr", "mg" ou "en"). */
    public static void charger(String nouvelleLangue) {
        if (PreferencesApp.LANGUE_MG.equals(nouvelleLangue)) {
            langue = PreferencesApp.LANGUE_MG;
        } else if (PreferencesApp.LANGUE_EN.equals(nouvelleLangue)) {
            langue = PreferencesApp.LANGUE_EN;
        } else {
            langue = PreferencesApp.LANGUE_FR;
        }
        Locale locale;
        if (PreferencesApp.LANGUE_MG.equals(langue)) {
            locale = Locale.forLanguageTag("mg");
        } else if (PreferencesApp.LANGUE_EN.equals(langue)) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.FRENCH;
        }
        try {
            bundle = ResourceBundle.getBundle("view.Messages", locale);
        } catch (MissingResourceException e) {
            bundle = null;
            System.err.println("[I18n] bundle introuvable pour " + locale
                    + " : " + e.getMessage());
        }
    }

    public static String langue() {
        return langue;
    }

    /** Traduit une clef, ou renvoie le texte par defaut si absente. */
    public static String t(String clef, String defaut) {
        if (bundle != null && clef != null) {
            try {
                return bundle.getString(clef);
            } catch (MissingResourceException ignore) {
                // on retombe sur le texte par defaut
            }
        }
        return defaut;
    }

    /** Traduit une clef, avec la clef elle-meme comme repli. */
    public static String t(String clef) {
        return t(clef, clef);
    }
}
