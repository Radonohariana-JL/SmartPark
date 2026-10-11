package view;

import dao.StationnementDAO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.Stationnement;
import util.DateUtils;

/**
 * Historique des stationnements, avec recherche.
 * Seul le design est personnalise : la logique est inchangee.
 */
public class HistoriqueForm extends JPanel {

    private static final int COL_STATUT = 8;

    private final JTextField txtRecherche = new JTextField(20);
    private final JButton btnRechercher = new BoutonStyle("Rechercher", BoutonStyle.Type.PRIMAIRE, false);
    private final JButton btnActualiser = new BoutonStyle("Actualiser", BoutonStyle.Type.NEUTRE, false);

    /** Filtre d'affichage par statut (applique cote client, sans requete). */
    private static final String FILTRE_TOUS = "Tous les statuts";
    private static final String FILTRE_EN_COURS = "En cours";
    private static final String FILTRE_TERMINE = "Terminé";
    private final JComboBox<String> comboStatut = new JComboBox<>(
            new String[]{FILTRE_TOUS, FILTRE_EN_COURS, FILTRE_TERMINE});

    /** Derniere liste chargee (avant filtre de statut). */
    private List<Stationnement> donnees = new ArrayList<>();

    private final DefaultTableModel modeleTable = new DefaultTableModel(
            new String[]{"ID", "Vehicule", "Proprietaire", "Place",
                "Date entree", "Date sortie", "Duree (h)", "Montant (" + util.Devise.symbole() + ")", "Statut"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(modeleTable);

    private final JLabel lblCompteur = new JLabel("0 resultat(s)", SwingConstants.RIGHT);

    private final StationnementDAO dao = new StationnementDAO();

    // ---------------------------------------------------------------------
    // PALETTE DE COULEURS
    // ---------------------------------------------------------------------
    private static final Color FOND               = new Color(0xF1F5F9);
    private static final Color CARTE              = new Color(0xFFFFFF);
    private static final Color BORDURE            = new Color(0xE2E8F0);
    private static final Color PRIMAIRE           = new Color(0x2563EB);
    private static final Color PRIMAIRE_HOVER     = new Color(0x1D4ED8);
    private static final Color PRIMAIRE_PRESSED   = new Color(0x1E40AF);
    private static final Color SECONDAIRE         = new Color(0x64748B);
    private static final Color SECONDAIRE_HOVER   = new Color(0x475569);
    private static final Color SECONDAIRE_PRESSED = new Color(0x334155);
    private static final Color DESTRUCTIF         = new Color(0xDC2626);
    private static final Color DESTRUCTIF_HOVER   = new Color(0xB91C1C);
    private static final Color DESTRUCTIF_PRESSED = new Color(0x7F1D1D);
    private static final Color TEXTE              = new Color(0x0F172A);
    private static final Color TEXTE_SECONDAIRE   = new Color(0x64748B);
    private static final Color LABEL              = new Color(0x334155);
    private static final Color BORDURE_CHAMP      = new Color(0xCBD5E1);
    private static final Color ENTETE_TABLE       = new Color(0xF8FAFC);
    private static final Color LIGNE_ALTERNEE     = new Color(0xF8FAFC);
    private static final Color SELECTION          = new Color(0xDBEAFE);

    private static final Color PASTILLE_EN_COURS_FOND    = new Color(0xDBEAFE);
    private static final Color PASTILLE_EN_COURS_TEXTE   = new Color(0x1D4ED8);
    private static final Color PASTILLE_TERMINE_FOND     = new Color(0xDCFCE7);
    private static final Color PASTILLE_TERMINE_TEXTE    = new Color(0x15803D);

    public HistoriqueForm() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        add(creerBarreRecherche(), BorderLayout.NORTH);
        add(creerTable(), BorderLayout.CENTER);
        actualiser();
    }

    // ---------------------------------------------------------------------
    // EN-TETE DE PAGE + CARTE DE RECHERCHE
    // ---------------------------------------------------------------------
    private JPanel creerBarreRecherche() {
        JPanel enTete = new JPanel();
        enTete.setOpaque(false);
        enTete.setLayout(new BoxLayout(enTete, BoxLayout.Y_AXIS));

        JLabel titrePage = new JLabel("Historique");
        titrePage.setFont(police(Font.BOLD, 20));
        titrePage.setForeground(TEXTE);
        titrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(titrePage);

        JLabel sousTitrePage = new JLabel(
                "Consulter tous les stationnements, avec recherche.");
        sousTitrePage.setFont(police(Font.PLAIN, 12));
        sousTitrePage.setForeground(TEXTE_SECONDAIRE);
        sousTitrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(sousTitrePage);

        JPanel zoneEnTete = new JPanel(new BorderLayout());
        zoneEnTete.setOpaque(false);
        zoneEnTete.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        zoneEnTete.add(enTete, BorderLayout.NORTH);

        JPanel haut = new JPanel(new BorderLayout());
        haut.setOpaque(false);
        haut.add(zoneEnTete, BorderLayout.NORTH);
        haut.add(creerCarteRecherche(), BorderLayout.CENTER);
        return haut;
    }

    /** Carte blanche contenant le champ de recherche et les boutons. */
    private JPanel creerCarteRecherche() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        styleChamp(txtRecherche);
        styleCombo(comboStatut);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;
        g.gridx = 0;
        g.gridwidth = 1;

        // Ligne 0 : libelle.
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 0);
        carte.add(libelle("Rechercher"), g);

        // Ligne 1 : champ qui s'etire + boutons.
        g.gridy = 1;
        g.insets = new Insets(8, 0, 0, 0);
        JPanel barre = new JPanel(new BorderLayout(8, 0));
        barre.setOpaque(false);
        barre.add(txtRecherche, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(libelle("Statut"));
        actions.add(comboStatut);
        actions.add(btnActualiser);
        actions.add(btnRechercher);
        barre.add(actions, BorderLayout.EAST);
        carte.add(barre, g);

        // Evenements.
        btnRechercher.addActionListener(e -> rechercher());
        btnActualiser.addActionListener(e -> {
            txtRecherche.setText("");
            comboStatut.setSelectedItem(FILTRE_TOUS);
            actualiser();
        });
        txtRecherche.addActionListener(e -> rechercher());
        comboStatut.addActionListener(e -> afficher());
        return carte;
    }

    // ---------------------------------------------------------------------
    // TABLEAU (carte blanche + compteur + table stylee)
    // ---------------------------------------------------------------------
    private JPanel creerTable() {
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        styleTable();

        JScrollPane defilement = new JScrollPane(table);
        defilement.setBorder(BorderFactory.createLineBorder(BORDURE));
        defilement.getViewport().setBackground(CARTE);

        JPanel carte = new JPanel(new BorderLayout(0, 12));
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JPanel enTete = new JPanel(new BorderLayout());
        enTete.setOpaque(false);

        JLabel titre = new JLabel("Liste des stationnements");
        titre.setFont(police(Font.BOLD, 14));
        titre.setForeground(TEXTE);
        enTete.add(titre, BorderLayout.WEST);

        lblCompteur.setFont(police(Font.PLAIN, 12));
        lblCompteur.setForeground(TEXTE_SECONDAIRE);
        enTete.add(lblCompteur, BorderLayout.EAST);

        carte.add(enTete, BorderLayout.NORTH);
        carte.add(defilement, BorderLayout.CENTER);
        return carte;
    }

    private void styleTable() {
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDURE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXTE);
        table.setFont(police(Font.PLAIN, 13));
        table.setFillsViewportHeight(true);

        TableColumn colId = table.getColumnModel().getColumn(0);
        colId.setPreferredWidth(60);
        colId.setMaxWidth(80);

        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        TableColumn colDuree = table.getColumnModel().getColumn(6);
        colDuree.setPreferredWidth(90);
        colDuree.setCellRenderer(new CelluleRenderer(SwingConstants.RIGHT));

        TableColumn colMontant = table.getColumnModel().getColumn(7);
        colMontant.setPreferredWidth(120);
        colMontant.setCellRenderer(new CelluleRenderer(SwingConstants.RIGHT));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new EnteteRenderer());

        table.setDefaultRenderer(Object.class, new CelluleRenderer());
    }

    // ---------------------------------------------------------------
    // Operations (inchangees)
    // ---------------------------------------------------------------
    private void rechercher() {
        chargerDonnees(dao.listerHistorique(txtRecherche.getText().trim()));
    }

    private void actualiser() {
        chargerDonnees(dao.listerHistorique(""));
    }

    private void chargerDonnees(List<Stationnement> liste) {
        donnees = liste;
        afficher();
    }

    /** Remplit la table en appliquant le filtre de statut courant. */
    private void afficher() {
        String statut = statutFiltre();
        modeleTable.setRowCount(0);
        int compte = 0;
        for (Stationnement s : donnees) {
            if (statut != null && !statut.equals(s.getStatut())) {
                continue;
            }
            modeleTable.addRow(new Object[]{
                s.getIdStationnement(),
                s.getImmatriculation(),
                s.getNomProprietaire(),
                s.getNumeroPlace(),
                DateUtils.format(s.getDateEntree()),
                DateUtils.format(s.getDateSortie()),
                s.getDuree(),
                util.Devise.formater(s.getMontant()),
                s.getStatut()
            });
            compte++;
        }
        lblCompteur.setText(compte + " resultat(s)");
    }

    /** Statut attendu selon le filtre, ou {@code null} pour "tous". */
    private String statutFiltre() {
        Object valeur = comboStatut.getSelectedItem();
        if (FILTRE_EN_COURS.equals(valeur)) {
            return "EN_COURS";
        }
        if (FILTRE_TERMINE.equals(valeur)) {
            return "TERMINE";
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // OUTILS DE STYLE
    // ---------------------------------------------------------------------
    private static final boolean UTILISER_SEGOE = contientPolice("Segoe UI");

    private static boolean contientPolice(String nom) {
        for (String famille : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()) {
            if (famille.equalsIgnoreCase(nom)) {
                return true;
            }
        }
        return false;
    }

    private static Font police(int style, int taille) {
        return new Font(UTILISER_SEGOE ? "Segoe UI" : "SansSerif", style, taille);
    }

    private static JLabel libelle(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 12));
        label.setForeground(LABEL);
        return label;
    }

    private void styleChamp(JTextField champ) {
        champ.setOpaque(true);
        champ.setBackground(CARTE);
        champ.setForeground(TEXTE);
        champ.setCaretColor(PRIMAIRE);
        champ.setFont(police(Font.PLAIN, 13));
        champ.setPreferredSize(new Dimension(120, 36));
        champ.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        champ.setBorder(bordureChamp(1, BORDURE_CHAMP));

        champ.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                champ.setBorder(bordureChamp(2, PRIMAIRE));
            }

            @Override
            public void focusLost(FocusEvent e) {
                champ.setBorder(bordureChamp(1, BORDURE_CHAMP));
            }
        });
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setOpaque(true);
        combo.setBackground(CARTE);
        combo.setForeground(TEXTE);
        combo.setFont(police(Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(150, 36));
        combo.setMaximumSize(new Dimension(150, 36));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static Border bordureChamp(int epaisseur, Color couleur) {
        int p = 6 - (epaisseur - 1);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, epaisseur),
                BorderFactory.createEmptyBorder(p, 10, p, 10));
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS DE DESSIN
    // ---------------------------------------------------------------------
    private static class RoundedBorder extends AbstractBorder {
        private final Color couleur;
        private final int rayon;
        private final int epaisseur;
        private final Insets espacement;

        RoundedBorder(Color couleur, int rayon, int epaisseur, Insets espacement) {
            this.couleur = couleur;
            this.rayon = rayon;
            this.epaisseur = epaisseur;
            this.espacement = espacement;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(epaisseur));
            g2.drawRoundRect(x + epaisseur / 2, y + epaisseur / 2,
                    w - epaisseur - 1, h - epaisseur - 1, rayon * 2, rayon * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return espacement != null ? espacement : new Insets(0, 0, 0, 0);
        }
    }

    private static class BoutonStyle extends JButton {

        enum Type { PRIMAIRE, SECONDAIRE, DESTRUCTIF, NEUTRE }

        private final Type type;
        private final int taillePolice;

        BoutonStyle(String texte, Type type, boolean compact) {
            super(texte);
            this.type = type;
            this.taillePolice = compact ? 12 : 13;
            int hauteur = compact ? 26 : 36;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, taillePolice));

            FontMetrics fm = getFontMetrics(getFont());
            int largeur = fm.stringWidth(texte) + (compact ? 24 : 48);
            setPreferredSize(new Dimension(largeur, hauteur));
            setMinimumSize(new Dimension(largeur, hauteur));
            setMaximumSize(new Dimension(largeur, hauteur));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            ButtonModel modele = getModel();

            Color fond;
            Color texte;
            if (!modele.isEnabled()) {
                fond = new Color(0xE2E8F0);
                texte = new Color(0x94A3B8);
            } else if (modele.isPressed() || modele.isArmed()) {
                fond = couleurEnfoncee();
                texte = couleurTexte();
            } else if (modele.isRollover()) {
                fond = couleurSurvol();
                texte = couleurTexte();
            } else {
                fond = couleurBase();
                texte = couleurTexte();
            }

            g2.setColor(fond);
            g2.fillRoundRect(0, 0, w, h, 8, 8);

            if (type == Type.NEUTRE) {
                g2.setColor(BORDURE_CHAMP);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            }

            g2.setFont(police(Font.BOLD, taillePolice));
            FontMetrics fm = g2.getFontMetrics();
            int yBase = (h + fm.getAscent() - fm.getDescent()) / 2;
            int x = (w - fm.stringWidth(getText())) / 2;
            g2.setColor(texte);
            g2.drawString(getText(), x, yBase);
            g2.dispose();
        }

        private Color couleurBase() {
            switch (type) {
                case PRIMAIRE:    return PRIMAIRE;
                case SECONDAIRE:  return SECONDAIRE;
                case DESTRUCTIF:  return DESTRUCTIF;
                default:          return CARTE;
            }
        }

        private Color couleurSurvol() {
            switch (type) {
                case PRIMAIRE:    return PRIMAIRE_HOVER;
                case SECONDAIRE:  return SECONDAIRE_HOVER;
                case DESTRUCTIF:  return DESTRUCTIF_HOVER;
                default:          return FOND;
            }
        }

        private Color couleurEnfoncee() {
            switch (type) {
                case PRIMAIRE:    return PRIMAIRE_PRESSED;
                case SECONDAIRE:  return SECONDAIRE_PRESSED;
                case DESTRUCTIF:  return DESTRUCTIF_PRESSED;
                default:          return BORDURE;
            }
        }

        private Color couleurTexte() {
            return type == Type.NEUTRE ? LABEL : Color.WHITE;
        }
    }

    private static class EnteteRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setOpaque(true);
            label.setBackground(ENTETE_TABLE);
            label.setForeground(LABEL);
            label.setFont(police(Font.BOLD, 13));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDURE),
                    BorderFactory.createEmptyBorder(4, 12, 4, 8)));
            return label;
        }
    }

    private static class CelluleRenderer extends DefaultTableCellRenderer {

        private final int alignement;
        private final PastilleLabel labelStatut = new PastilleLabel();

        CelluleRenderer() {
            this(SwingConstants.LEFT);
        }

        CelluleRenderer(int alignement) {
            this.alignement = alignement;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            if (c == COL_STATUT) {
                labelStatut.configurer(
                        v == null ? "" : String.valueOf(v), fondLigne(sel, r));
                return labelStatut;
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setOpaque(true);
            label.setFont(police(Font.PLAIN, 13));
            label.setHorizontalAlignment(alignement);
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));
            label.setBackground(fondLigne(sel, r));
            label.setForeground(TEXTE);
            return label;
        }
    }

    /** Etiquette de cellule dessinant une pastille coloree pour le statut. */
    private static class PastilleLabel extends JLabel {

        private Color fondLigne = CARTE;
        private Color pastille = null;

        PastilleLabel() {
            setOpaque(false);
            setFont(police(Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        void configurer(String statut, Color fondLigne) {
            this.fondLigne = fondLigne;
            setText(statut);
            if ("EN_COURS".equals(statut)) {
                pastille = PASTILLE_EN_COURS_FOND;
                setForeground(PASTILLE_EN_COURS_TEXTE);
            } else if ("TERMINE".equals(statut)) {
                pastille = PASTILLE_TERMINE_FOND;
                setForeground(PASTILLE_TERMINE_TEXTE);
            } else {
                pastille = null;
                setForeground(TEXTE);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(fondLigne);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (pastille != null) {
                FontMetrics fm = g2.getFontMetrics(getFont());
                int largeurTexte = fm.stringWidth(getText()) + 24;
                int hauteur = 22;
                int x = getInsets().left;
                int y = (getHeight() - hauteur) / 2;
                g2.setColor(pastille);
                g2.fillRoundRect(x, y, largeurTexte, hauteur, hauteur, hauteur);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static Color fondLigne(boolean selection, int ligne) {
        if (selection) {
            return SELECTION;
        }
        return ligne % 2 == 1 ? LIGNE_ALTERNEE : CARTE;
    }
}
