package util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Journal central des notifications metier de SmartPark.
 *
 * <p>Les entrees et sorties de vehicules confirmees y publient un evenement
 * reel. Le {@code Dashboard} s'y abonne pour afficher la pastille et la liste.
 * Le stockage est en memoire (aucune table SQL n'est necessaire) ; les
 * evenements sont donc perdus a la fermeture de l'application, ce qui est
 * coherent avec le fait qu'ils n'ont pas d'existence en base dediee.</p>
 *
 * <p>Aucun evenement n'est fabrique : une notification n'est creee qu'apres le
 * succes effectif d'une operation de DAO.</p>
 */
public final class NotificationCentre {

    /** Nombre maximal d'evenements conserves. */
    private static final int MAX = 50;

    /** Type d'evenement publie. */
    public enum Type {
        ENTREE,
        SORTIE
    }

    /** Evenement de notification (donnees reelles, structurees). */
    public static final class Evenement {

        private final Type type;
        private final String immatriculation;
        private final String place;
        private final double montant;
        private final LocalDateTime moment;
        private boolean lue;

        Evenement(Type type, String immatriculation, String place,
                double montant, LocalDateTime moment) {
            this.type = type;
            this.immatriculation = immatriculation;
            this.place = place;
            this.montant = montant;
            this.moment = moment;
        }

        public Type getType() {
            return type;
        }

        public String getImmatriculation() {
            return immatriculation;
        }

        public String getPlace() {
            return place;
        }

        public double getMontant() {
            return montant;
        }

        public LocalDateTime getMoment() {
            return moment;
        }

        public boolean isLue() {
            return lue;
        }
    }

    private static final List<Evenement> EVENEMENTS = new ArrayList<>();
    private static final List<Runnable> ECOUTEURS = new ArrayList<>();
    private static int nonLues;

    private NotificationCentre() {
    }

    /** Publie une entree de vehicule confirmee. */
    public static synchronized void entree(String immatriculation, String place) {
        publier(new Evenement(Type.ENTREE, immatriculation, place, 0,
                LocalDateTime.now()));
    }

    /** Publie une sortie de vehicule confirmee. */
    public static synchronized void sortie(String immatriculation, double montant) {
        publier(new Evenement(Type.SORTIE, immatriculation, null, montant,
                LocalDateTime.now()));
    }

    private static void publier(Evenement evenement) {
        EVENEMENTS.add(0, evenement);
        while (EVENEMENTS.size() > MAX) {
            EVENEMENTS.remove(EVENEMENTS.size() - 1);
        }
        nonLues++;
        notifier();
    }

    /** Copie des evenements, du plus recent au plus ancien. */
    public static synchronized List<Evenement> lister() {
        return Collections.unmodifiableList(new ArrayList<>(EVENEMENTS));
    }

    /** Nombre de notifications non lues. */
    public static synchronized int nonLues() {
        return nonLues;
    }

    /** Marque toutes les notifications comme lues. */
    public static synchronized void marquerToutesLues() {
        for (Evenement e : EVENEMENTS) {
            e.lue = true;
        }
        nonLues = 0;
        notifier();
    }

    /** Enregistre un ecouteur notifie a chaque changement. */
    public static void addEcouteur(Runnable ecouteur) {
        if (ecouteur == null) {
            return;
        }
        synchronized (ECOUTEURS) {
            if (!ECOUTEURS.contains(ecouteur)) {
                ECOUTEURS.add(ecouteur);
            }
        }
    }

    private static void notifier() {
        List<Runnable> copie;
        synchronized (ECOUTEURS) {
            copie = new ArrayList<>(ECOUTEURS);
        }
        for (Runnable ecouteur : copie) {
            try {
                ecouteur.run();
            } catch (RuntimeException ex) {
                System.err.println("[NotificationCentre] ecouteur en echec : "
                        + ex.getMessage());
            }
        }
    }
}
