package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formateur monetaire central de SmartPark.
 *
 * <p>Cette classe ne realise AUCUNE conversion de devise : elle ne fait que
 * mettre en forme le montant stocke en base selon la devise d'affichage
 * choisie. Changer le symbole ne convertit jamais la valeur financiere reelle
 * (aucun taux de change n'est applique).</p>
 *
 * <p>Trois devises sont proposees :</p>
 * <ul>
 *   <li>{@code MGA} : Ariary malgache, symbole {@code Ar}, aucun decimal ;</li>
 *   <li>{@code USD} : Dollar americain, symbole {@code $}, 2 decimales ;</li>
 *   <li>{@code GBP} : Livre sterling, symbole {@code £}, 2 decimales.</li>
 * </ul>
 */
public final class Devise {

    public static final String MGA = PreferencesApp.DEVISE_MGA; // Ariary malgache
    public static final String USD = PreferencesApp.DEVISE_USD; // Dollar americain
    public static final String GBP = PreferencesApp.DEVISE_GBP; // Livre sterling

    private Devise() {
    }

    /** Devise d'affichage courante (lue depuis les preferences). */
    public static String courante() {
        return PreferencesApp.getDevise();
    }

    /** Symbole d'affichage : "Ar" (MGA), "$" (USD) ou "£" (GBP). */
    public static String symbole() {
        String d = courante();
        if (USD.equals(d)) {
            return "$";
        }
        if (GBP.equals(d)) {
            return "\u00A3";
        }
        return "Ar";
    }

    /** Nombre de decimales : entier pour MGA, 2 pour USD et GBP. */
    public static int decimales() {
        return MGA.equals(courante()) ? 0 : 2;
    }

    /** Libelle long de la devise courante. */
    public static String libelle() {
        String d = courante();
        if (USD.equals(d)) {
            return "Dollar ($)";
        }
        if (GBP.equals(d)) {
            return "Livre sterling (\u00A3)";
        }
        return "Ariary (Ar)";
    }

    /**
     * Formate un montant en nombre groupe, suivi du symbole.
     * Exemples : {@code "3 000 Ar"}, {@code "1 234,56 $"}, {@code "£1,234.56"}.
     */
    public static String formater(double montant) {
        return formaterSansSymbole(montant) + " " + symbole();
    }

    /**
     * Formate un montant en nombre groupe, sans symbole.
     * Exemples : {@code "3 000"}, {@code "1 234,56"} ou {@code "1,234.56"}.
     */
    public static String formaterSansSymbole(double montant) {
        String d = courante();
        DecimalFormatSymbols symboles;
        if (MGA.equals(d)) {
            // Convention francaise : separateur de milliers = espace, virgule.
            symboles = new DecimalFormatSymbols(Locale.FRANCE);
            symboles.setGroupingSeparator(' ');
            symboles.setDecimalSeparator(',');
        } else {
            // Convention anglo-saxonne : separateur de milliers = virgule, point.
            symboles = new DecimalFormatSymbols(Locale.US);
            symboles.setGroupingSeparator(',');
            symboles.setDecimalSeparator('.');
        }
        String motif = decimales() == 0 ? "#,##0" : "#,##0.00";
        DecimalFormat format = new DecimalFormat(motif, symboles);
        return format.format(montant);
    }
}
