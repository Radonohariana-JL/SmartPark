package util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Gestionnaire de theme (clair / sombre) de SmartPark.
 *
 * <p>Principe : la palette claire est la palette d'origine de l'application.
 * En mode clair, le gestionnaire ne touche a RIEN (le rendu reste strictement
 * identique a l'original). En mode sombre, il recolorise les composants Swing
 * standard (fonds et textes) et laisse les composants a dessin personnalise
 * intacts. Les composants a dessin personnalise qui lisent la palette mutable
 * exposee ici ({@link #FOND}, {@link #CARTE}, {@link #TEXTE}...) se mettent a
 * jour d'eux-memes, car les champs sont re-evalues a chaque repeint.</p>
 *
 * <p>Le remappage est reversible : les couleurs d'origine de chaque composant
 * sont memorisees (proprietes clientes) avant la premiere modification, puis
 * restaurees au retour en mode clair.</p>
 *
 * <p>Aucune donnee metier ni aucun calcul financier n'est modifie.</p>
 */
public final class Theme {

    public static final String CLAIR = PreferencesApp.THEME_CLAIR;
    public static final String SOMBRE = PreferencesApp.THEME_SOMBRE;

    // --- Palette mutable : clair (valeurs d'origine) / sombre ---
    public static Color FOND = new Color(0xF1F5F9);
    public static Color CARTE = new Color(0xFFFFFF);
    public static Color BANDE = new Color(0xF8FAFC);
    public static Color CARTE_ENTETE = new Color(0xF8FAFC);
    public static Color BORDURE = new Color(0xE2E8F0);
    public static Color TEXTE = new Color(0x0F172A);
    public static Color TEXTE_SECONDAIRE = new Color(0x64748B);
    public static Color LABEL = new Color(0x334155);
    public static Color BORDURE_CHAMP = new Color(0xCBD5E1);
    public static Color SELECTION = new Color(0xDBEAFE);
    public static Color CHIP_FOND = new Color(0xE2E8F0);
    public static Color LIGNE_ALTERNEE = new Color(0xF8FAFC);
    public static Color BLEU_FOND = new Color(0xEFF6FF);
    public static Color BLEU_BORDURE = new Color(0xBFDBFE);
    public static Color VERT_FOND = new Color(0xDCFCE7);
    public static Color PRIMAIRE = new Color(0x2563EB);
    public static Color PRIMAIRE_FONCE = new Color(0x1E40AF);
    public static Color PRIMAIRE_HOVER = new Color(0x1D4ED8);
    public static Color PRIMAIRE_PRESSED = new Color(0x1E40AF);
    public static Color SUCCES = new Color(0x16A34A);
    public static Color SUCCES_FOND = new Color(0xDCFCE7);
    public static Color SUCCES_BORD = new Color(0xBBF7D0);
    public static Color AMBRE = new Color(0xD97706);
    public static Color AMBRE_FOND = new Color(0xFEF3C7);
    public static Color AMBRE_BORD = new Color(0xFDE68A);
    public static Color ERREUR = new Color(0xDC2626);
    public static Color NEUTRE = new Color(0x94A3B8);
    public static Color OUTLINE = new Color(0x94A3B8);
    // Accents secondaires / destructifs / survols (recharges avec le theme).
    public static Color SECONDAIRE = new Color(0x64748B);
    public static Color SECONDAIRE_HOVER = new Color(0x475569);
    public static Color SECONDAIRE_PRESSED = new Color(0x334155);
    public static Color DESTRUCTIF = new Color(0xDC2626);
    public static Color DESTRUCTIF_HOVER = new Color(0xB91C1C);
    public static Color DESTRUCTIF_PRESSED = new Color(0x7F1D1D);
    public static Color SURVOL_LIGNE = new Color(0xEEF2F7);
    public static Color SURVOL_MODIFIER = new Color(0xDBEAFE);
    public static Color SURVOL_SUPPRIMER = new Color(0xFEE2E2);

    private static final String CLE_BG = "smartpark.theme.bg";
    private static final String CLE_FG = "smartpark.theme.fg";

    private static boolean sombre;
    private static final List<Runnable> ECOUTEURS = new ArrayList<>();

    static {
        // Toute fenetre ouverte recoit immediatement la palette active.
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                    if (event instanceof WindowEvent
                            && event.getID() == WindowEvent.WINDOW_OPENED) {
                        Window fenetre = ((WindowEvent) event).getWindow();
                        SwingUtilities.invokeLater(() -> {
                            remapper(fenetre);
                            fenetre.repaint();
                        });
                    }
                }, AWTEvent.WINDOW_EVENT_MASK);
            } catch (Throwable ignore) {
                // Environnement sans toolkit graphique : on continue sans listener.
            }
        }
    }

    private Theme() {
    }

    public static boolean estSombre() {
        return sombre;
    }

    public static String mode() {
        return sombre ? SOMBRE : CLAIR;
    }

    /** Enregistre un ecouteur notifie (sur l'EDT) a chaque changement de theme. */
    public static void addEcouteur(Runnable ecouteur) {
        if (ecouteur != null && !ECOUTEURS.contains(ecouteur)) {
            ECOUTEURS.add(ecouteur);
        }
    }

    /** Lit le theme enregistre et l'applique (a appeler au demarrage). */
    public static void appliquerAuDemarrage() {
        appliquer(PreferencesApp.getTheme());
    }

    /**
     * Applique immediatement le theme demande et l'enregistre comme preference.
     */
    public static void appliquerEtEnregistrer(String mode) {
        PreferencesApp.setTheme(mode);
        appliquer(mode);
    }

    /** Applique le theme sans le persister (utilise par le demarrage/tests). */
    public static void appliquer(String mode) {
        boolean cible = SOMBRE.equals(mode);
        boolean change = (cible != sombre);
        sombre = cible;
        majPalette();
        appliquerUIManager();
        if (change) {
            // 1) les vues rechargent d'abord leur palette locale,
            // 2) puis on recolorise et on repeint les fenetres.
            notifierEcouteurs();
            rafraichirFenetres();
        }
    }

    private static void majPalette() {
        if (sombre) {
            FOND = new Color(0x0F172A);
            CARTE = new Color(0x1E293B);
            BANDE = new Color(0x172033);
            CARTE_ENTETE = new Color(0x172033);
            BORDURE = new Color(0x334155);
            TEXTE = new Color(0xF1F5F9);
            TEXTE_SECONDAIRE = new Color(0x94A3B8);
            LABEL = new Color(0xE2E8F0);
            BORDURE_CHAMP = new Color(0x4B5563);
            SELECTION = new Color(0x1E3A8A);
            CHIP_FOND = new Color(0x334155);
            LIGNE_ALTERNEE = new Color(0x172033);
            BLEU_FOND = new Color(0x1B2436);
            BLEU_BORDURE = new Color(0x2A4365);
            VERT_FOND = new Color(0x14351F);
            PRIMAIRE = new Color(0x3B82F6);
            PRIMAIRE_FONCE = new Color(0x93C5FD);
            PRIMAIRE_HOVER = new Color(0x2563EB);
            PRIMAIRE_PRESSED = new Color(0x1D4ED8);
            SUCCES = new Color(0x34D399);
            SUCCES_FOND = new Color(0x14351F);
            SUCCES_BORD = new Color(0x1E4A2A);
            AMBRE = new Color(0xFBBF24);
            AMBRE_FOND = new Color(0x3A2E0A);
            AMBRE_BORD = new Color(0x5A4410);
            ERREUR = new Color(0xF87171);
            NEUTRE = new Color(0x94A3B8);
            OUTLINE = new Color(0x94A3B8);
            SECONDAIRE = new Color(0x94A3B8);
            SECONDAIRE_HOVER = new Color(0x8494A6);
            SECONDAIRE_PRESSED = new Color(0x64748B);
            DESTRUCTIF = new Color(0xF87171);
            DESTRUCTIF_HOVER = new Color(0xEF4444);
            DESTRUCTIF_PRESSED = new Color(0xDC2626);
            SURVOL_LIGNE = new Color(0x243044);
            SURVOL_MODIFIER = new Color(0x1E3A8A);
            SURVOL_SUPPRIMER = new Color(0x3A1515);
        } else {
            FOND = new Color(0xF1F5F9);
            CARTE = new Color(0xFFFFFF);
            BANDE = new Color(0xF8FAFC);
            CARTE_ENTETE = new Color(0xF8FAFC);
            BORDURE = new Color(0xE2E8F0);
            TEXTE = new Color(0x0F172A);
            TEXTE_SECONDAIRE = new Color(0x64748B);
            LABEL = new Color(0x334155);
            BORDURE_CHAMP = new Color(0xCBD5E1);
            SELECTION = new Color(0xDBEAFE);
            CHIP_FOND = new Color(0xE2E8F0);
            LIGNE_ALTERNEE = new Color(0xF8FAFC);
            BLEU_FOND = new Color(0xEFF6FF);
            BLEU_BORDURE = new Color(0xBFDBFE);
            VERT_FOND = new Color(0xDCFCE7);
            PRIMAIRE = new Color(0x2563EB);
            PRIMAIRE_FONCE = new Color(0x1E40AF);
            PRIMAIRE_HOVER = new Color(0x1D4ED8);
            PRIMAIRE_PRESSED = new Color(0x1E40AF);
            SUCCES = new Color(0x16A34A);
            SUCCES_FOND = new Color(0xDCFCE7);
            SUCCES_BORD = new Color(0xBBF7D0);
            AMBRE = new Color(0xD97706);
            AMBRE_FOND = new Color(0xFEF3C7);
            AMBRE_BORD = new Color(0xFDE68A);
            ERREUR = new Color(0xDC2626);
            NEUTRE = new Color(0x94A3B8);
            OUTLINE = new Color(0x94A3B8);
            SECONDAIRE = new Color(0x64748B);
            SECONDAIRE_HOVER = new Color(0x475569);
            SECONDAIRE_PRESSED = new Color(0x334155);
            DESTRUCTIF = new Color(0xDC2626);
            DESTRUCTIF_HOVER = new Color(0xB91C1C);
            DESTRUCTIF_PRESSED = new Color(0x7F1D1D);
            SURVOL_LIGNE = new Color(0xEEF2F7);
            SURVOL_MODIFIER = new Color(0xDBEAFE);
            SURVOL_SUPPRIMER = new Color(0xFEE2E2);
        }
    }

    // ------------------------------------------------------------------
    // UIManager : composants Swing standard et boites de dialogue
    // ------------------------------------------------------------------
    private static void appliquerUIManager() {
        Color fond = sombre ? new Color(0x1E293B) : new Color(0xFFFFFF);
        Color surface = sombre ? new Color(0x0F172A) : new Color(0xF1F5F9);
        Color texte = sombre ? new Color(0xF1F5F9) : new Color(0x0F172A);
        Color bordure = sombre ? new Color(0x334155) : new Color(0xE2E8F0);
        Color selection = sombre ? new Color(0x2563EB) : new Color(0xDBEAFE);
        Color texteSelection = sombre ? new Color(0xFFFFFF) : new Color(0x0F172A);

        set("Panel.background", surface);
        set("Panel.foreground", texte);
        set("control", surface);
        set("info", fond);
        set("Label.background", surface);
        set("Label.foreground", texte);
        set("OptionPane.background", fond);
        set("OptionPane.foreground", texte);
        set("OptionPane.messageForeground", texte);
        set("TextField.background", fond);
        set("TextField.foreground", texte);
        set("TextField.caretForeground", texte);
        set("TextField.selectionBackground", selection);
        set("TextField.selectionForeground", texteSelection);
        set("PasswordField.background", fond);
        set("PasswordField.foreground", texte);
        set("TextArea.background", fond);
        set("TextArea.foreground", texte);
        set("TextPane.background", fond);
        set("TextPane.foreground", texte);
        set("ComboBox.background", fond);
        set("ComboBox.foreground", texte);
        set("ComboBox.selectionBackground", selection);
        set("ComboBox.selectionForeground", texteSelection);
        set("List.background", fond);
        set("List.foreground", texte);
        set("List.selectionBackground", selection);
        set("List.selectionForeground", texteSelection);
        set("Table.background", fond);
        set("Table.foreground", texte);
        set("Table.gridColor", bordure);
        set("Table.selectionBackground", selection);
        set("Table.selectionForeground", texteSelection);
        set("TableHeader.background", sombre ? new Color(0x172033) : new Color(0xF8FAFC));
        set("TableHeader.foreground", texte);
        set("ScrollPane.background", surface);
        set("Viewport.background", fond);
        set("ScrollBar.background", surface);
        set("ScrollBar.thumb", sombre ? new Color(0x475569) : new Color(0xCBD5E1));
        set("ScrollBar.track", surface);
        set("ToolTip.background", sombre ? new Color(0x334155) : new Color(0xFFFFFF));
        set("ToolTip.foreground", texte);
        set("Button.background", fond);
        set("Button.foreground", texte);
        set("ToggleButton.background", fond);
        set("ToggleButton.foreground", texte);
        set("CheckBox.background", surface);
        set("CheckBox.foreground", texte);
        set("RadioButton.background", surface);
        set("RadioButton.foreground", texte);
        set("MenuBar.background", surface);
        set("MenuBar.foreground", texte);
        set("Menu.background", fond);
        set("Menu.foreground", texte);
        set("MenuItem.background", fond);
        set("MenuItem.foreground", texte);
        set("PopupMenu.background", fond);
        set("PopupMenu.foreground", texte);
        set("Separator.foreground", bordure);
        set("TabbedPane.background", surface);
        set("TabbedPane.foreground", texte);
        set("TabbedPane.selected", fond);
    }

    private static void set(String cle, Color valeur) {
        UIManager.put(cle, valeur);
    }

    /** Reapplique le theme a toutes les fenetres ouvertes, immediatement. */
    public static void rafraichirFenetres() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(Theme::rafraichirFenetres);
            return;
        }
        for (Window fenetre : Window.getWindows()) {
            remapper(fenetre);
            fenetre.repaint();
        }
    }

    /**
     * Parcours recursif des composants.
     *
     * <p>En mode clair, restaure les couleurs d'origine (aucune modification si
     * le composant n'en a jamais eu). En mode sombre, recolorise les fonds et
     * textes standard ; les sous-arbres a dessin personnalise sont ignores.</p>
     */
    public static void remapper(Component racine) {
        if (racine == null) {
            return;
        }
        if (!sombre) {
            restaurer(racine);
            return;
        }
        if (!(racine instanceof JComponent)) {
            if (racine instanceof Container) {
                for (Component enfant : ((Container) racine).getComponents()) {
                    remapper(enfant);
                }
            }
            return;
        }

        JComponent j = (JComponent) racine;
        // Un composant a rendu vectoriel conserve son propre dessin, mais ses
        // enfants (labels, champs) doivent tout de meme etre recolorises.
        boolean peintPersonnalise = estPeintPersonnalise(j);

        boolean surfaceSombre = false;
        if (!peintPersonnalise && j.isOpaque()) {
            Color bg = j.getBackground();
            if (bg != null) {
                Integer mappe = mapperBg(bg);
                if (mappe != null) {
                    memoriser(j, CLE_BG, bg);
                    j.setBackground(new Color(mappe));
                } else if (luminance(bg) < 0.45) {
                    // Surface sombre non geree (barre laterale) : ne pas toucher.
                    surfaceSombre = true;
                }
            }
        }

        if (!peintPersonnalise && !surfaceSombre) {
            Color fg = j.getForeground();
            Integer mappe = mapperFg(fg);
            if (mappe != null) {
                memoriser(j, CLE_FG, fg);
                j.setForeground(new Color(mappe));
            }
        }

        if (surfaceSombre) {
            return;
        }
        if (racine instanceof Container) {
            for (Component enfant : ((Container) racine).getComponents()) {
                remapper(enfant);
            }
        }
    }

    /** Restaure fond/texte d'origine memorises (mode clair). */
    private static void restaurer(Component racine) {
        if (racine instanceof JComponent) {
            JComponent j = (JComponent) racine;
            restaurer(j, CLE_BG, j.getBackground(), j::setBackground);
            restaurer(j, CLE_FG, j.getForeground(), j::setForeground);
        }
        if (racine instanceof Container) {
            for (Component enfant : ((Container) racine).getComponents()) {
                restaurer(enfant);
            }
        }
    }

    private static void restaurer(JComponent j, String cle, Color ignore,
            java.util.function.Consumer<Color> setter) {
        Object memorise = j.getClientProperty(cle);
        if (memorise instanceof Color) {
            setter.accept((Color) memorise);
            j.putClientProperty(cle, null);
        }
    }

    private static void memoriser(JComponent j, String cle, Color valeur) {
        if (j.getClientProperty(cle) == null) {
            j.putClientProperty(cle, valeur);
        }
    }

    /** Vrai si la classe declare son propre paintComponent (dessin vectoriel). */
    private static boolean estPeintPersonnalise(JComponent composant) {
        try {
            Method m = composant.getClass().getMethod("paintComponent", Graphics.class);
            Class<?> declarant = m.getDeclaringClass();
            if (declarant == JComponent.class
                    || declarant == JPanel.class
                    || declarant == JLabel.class) {
                return false;
            }
            // Les composants Swing/JDK recolorables (JTable, JComboBox, JList...)
            // ne sont pas consideres comme "dessin personnalise" : leur fond doit
            // etre remappe. Seuls les composants du projet (package view.*) le sont.
            String nom = declarant.getName();
            return !nom.startsWith("javax.swing.") && !nom.startsWith("java.awt.");
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Tables de correspondance clair -> sombre (sens unique)
    // ------------------------------------------------------------------
    private static Integer mapperBg(Color couleur) {
        if (couleur == null) {
            return null;
        }
        int rgb = couleur.getRGB() & 0xFFFFFF;
        for (int[] paire : FONDS) {
            if (rgb == paire[0]) {
                return paire[1];
            }
        }
        return null;
    }

    private static Integer mapperFg(Color couleur) {
        if (couleur == null) {
            return null;
        }
        int rgb = couleur.getRGB() & 0xFFFFFF;
        for (int[] paire : TEXTES) {
            if (rgb == paire[0]) {
                return paire[1];
            }
        }
        return null;
    }

    /** Fonds : {clair, sombre}. */
    private static final int[][] FONDS = {
        {0xF1F5F9, 0x0F172A}, // fond de page
        {0xFFFFFF, 0x1E293B}, // carte / fond de champ
        {0xF8FAFC, 0x172033}, // bande / entete de carte
        {0xF4F6FA, 0x243044}, // carte claire secondaire
        {0xE2E8F0, 0x334155}, // bordure / chip
        {0xEFF6FF, 0x1B2436}, // bandeau info bleu
        {0xBFDBFE, 0x2A4365}, // bordure info bleu
        {0xDCFCE7, 0x14351F}, // fond vert
        {0xBBF7D0, 0x1E4A2A}, // bordure verte
        {0xFEF3C7, 0x3A2E0A}, // fond ambre
        {0xFDE68A, 0x5A4410}, // bordure ambre
        {0xDBEAFE, 0x1E3A8A}, // selection
        {0xEEF2F7, 0x243044}, // survol de ligne
        {0xFEE2E2, 0x3A1515}, // survol supprimer
        {0xCBD5E1, 0x4B5563}, // bordure de champ
    };

    /** Textes : {clair, sombre}. */
    private static final int[][] TEXTES = {
        {0x0F172A, 0xF1F5F9}, // texte principal
        {0x334155, 0xE2E8F0}, // label
        {0x64748B, 0x94A3B8}, // texte secondaire
        {0x94A3B8, 0x94A3B8}, // texte discret / outline
        {0x475569, 0x8494A6}, // survol secondaire
        {0x1E40AF, 0x93C5FD}, // bleu fonce
        {0x1D4ED8, 0x60A5FA}, // bleu survol
        {0xDC2626, 0xF87171}, // rouge
        {0xD97706, 0xFBBF24}, // ambre
        {0x16A34A, 0x34D399}, // vert
        {0x2563EB, 0x60A5FA}, // bleu primaire
    };

    private static double luminance(Color c) {
        return (0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue())
                / 255.0;
    }

    /**
     * Adapte une couleur de FOND capturee par un composant a dessin personnalise
     * (les champs {@code final} initialises a la construction ne se mettent pas a
     * jour tout seuls). La table clair/sombre est appliquee dans les deux sens :
     * une couleur d'origine claire devient sombre en mode sombre, et inversement.
     * Les couleurs hors palette (accents specifiques) sont renvoyees inchangees.
     */
    public static Color adapteFond(Color couleur) {
        return adapter(couleur, FONDS);
    }

    /** Idem {@link #adapteFond} pour une couleur de TEXTE / trait / icone. */
    public static Color adapteTexte(Color couleur) {
        return adapter(couleur, TEXTES);
    }

    /**
     * Adapte une couleur dont la nature (fond ou texte) n'est pas connue a
     * l'avance : les tables de fonds puis de textes sont essayees.
     */
    public static Color adapte(Color couleur) {
        Color c = adapter(couleur, FONDS);
        if (c != couleur) {
            return c;
        }
        return adapter(couleur, TEXTES);
    }

    private static Color adapter(Color couleur, int[][] table) {
        if (couleur == null) {
            return null;
        }
        int rgb = couleur.getRGB() & 0xFFFFFF;
        for (int[] paire : table) {
            if (rgb == paire[0] || rgb == paire[1]) {
                return new Color(sombre ? paire[1] : paire[0]);
            }
        }
        return couleur;
    }

    private static void notifierEcouteurs() {
        Runnable action = () -> {
            for (Runnable ecouteur : new ArrayList<>(ECOUTEURS)) {
                try {
                    ecouteur.run();
                } catch (RuntimeException ex) {
                    System.err.println("[Theme] ecouteur en echec : " + ex.getMessage());
                }
            }
            recolonerBoutons();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private static void recolonerBoutons() {
        for (Window fenetre : Window.getWindows()) {
            fenetre.repaint();
        }
    }
}
