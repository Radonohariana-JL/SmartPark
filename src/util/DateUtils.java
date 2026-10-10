package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Petites methodes utilitaires pour les dates.
 */
public class DateUtils {

    private static final DateTimeFormatter FORMAT_DATE_HEURE
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String format(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(FORMAT_DATE_HEURE);
    }

    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}