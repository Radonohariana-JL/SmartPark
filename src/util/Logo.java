package util;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Charge le logo officiel de SmartPark embarque dans les ressources du projet
 * (src/resources/logo.png). L'image est ainsi disponible apres compilation et
 * quelle que soit la machine, sans chemin absolu.
 */
public final class Logo {

    /** Emplacement du logo dans le classpath (dossier de ressources du projet). */
    private static final String CHEMIN_RESSOURCE = "/resources/logo.png";

    private static Image image;
    private static boolean charge;

    private Logo() {
    }

    /**
     * Image originale du logo, mise en cache. Retourne {@code null} si la
     * ressource est introuvable ou illisible.
     */
    public static synchronized Image image() {
        if (!charge) {
            charge = true;
            try (InputStream flux = Logo.class.getResourceAsStream(CHEMIN_RESSOURCE)) {
                if (flux != null) {
                    image = ImageIO.read(flux);
                }
            } catch (IOException ex) {
                image = null;
            }
        }
        return image;
    }

    /**
     * Logo redimensionne en conservant ses proportions pour tenir dans un carre
     * de cote {@code cote} pixels. Retourne {@code null} si la ressource est
     * introuvable.
     */
    public static ImageIcon icone(int cote) {
        Image originale = image();
        if (originale == null) {
            return null;
        }
        int largeur = originale.getWidth(null);
        int hauteur = originale.getHeight(null);
        if (largeur <= 0 || hauteur <= 0) {
            return null;
        }
        double ratio = Math.min((double) cote / largeur, (double) cote / hauteur);
        int largeurCible = Math.max(1, (int) Math.round(largeur * ratio));
        int hauteurCible = Math.max(1, (int) Math.round(hauteur * ratio));
        return new ImageIcon(
                originale.getScaledInstance(largeurCible, hauteurCible, Image.SCALE_SMOOTH));
    }
}
