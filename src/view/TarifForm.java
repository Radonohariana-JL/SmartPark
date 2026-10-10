package view;

import dao.TarifDAO;
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
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import model.Tarif;

/**
 * Gestion des tarifs : affichage et modification du prix par heure.
 * Seul le design est personnalise : la logique est inchangee.
 */
public class TarifForm extends JPanel {

    private final JTextField txtPrix = new JTextField(10);

    private final JButton btnModifier = new BoutonStyle("Modifier", BoutonStyle.Type.PRIMAIRE, false);
    private final JButton btnActualiser = new BoutonStyle("Actualiser", BoutonStyle.Type.NEUTRE, false);

    private final DefaultTableModel modeleTable = new DefaultTableModel(
            new String[]{"Type de vehicule", "Prix par heure (Ar)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(modeleTable);

    private final JLabel lblCompteur = new JLabel("0 resultat(s)", SwingConstants.RIGHT);

    private final TarifDAO dao = new TarifDAO();

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

    public TarifForm() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        add(creerFormulaire(), BorderLayout.NORTH);
        add(creerTable(), BorderLayout.CENTER);
        actualiser();
    }

    // ---------------------------------------------------------------------
    // FORMULAIRE (en-tete de page + carte de saisie)
    // ---------------------------------------------------------------------
    private JPanel creerFormulaire() {
        JPanel enTete = new JPanel();
        enTete.setOpaque(false);
        enTete.setLayout(new BoxLayout(enTete, BoxLayout.Y_AXIS));

        JLabel titrePage = new JLabel("Tarifs");
        titrePage.setFont(police(Font.BOLD, 20));
        titrePage.setForeground(TEXTE);
        titrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(titrePage);

        JLabel sousTitrePage = new JLabel(
                "Définir le prix par heure pour chaque type de véhicule.");
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
        haut.add(creerCarteFormulaire(), BorderLayout.CENTER);
        return haut;
    }

    /** Carte blanche contenant le champ de prix et les boutons. */
    private JPanel creerCarteFormulaire() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        styleChamp(txtPrix);
        txtPrix.setPreferredSize(new Dimension(320, 36));
        txtPrix.setMaximumSize(new Dimension(320, 36));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;
        g.gridx = 0;
        g.gridwidth = 1;

        // Ligne 0 : libelle.
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 0);
        carte.add(libelle("Prix par heure (Ar)"), g);

        // Ligne 1 : champ (pleine largeur de carte, plafonne a 320 px).
        g.gridy = 1;
        g.insets = new Insets(8, 0, 0, 0);
        JPanel zoneChamp = new JPanel();
        zoneChamp.setOpaque(false);
        zoneChamp.setLayout(new BoxLayout(zoneChamp, BoxLayout.X_AXIS));
        zoneChamp.add(txtPrix);
        zoneChamp.add(Box.createHorizontalGlue());
        carte.add(zoneChamp, g);

        // Ligne 2 : texte d'aide.
        g.gridy = 2;
        g.insets = new Insets(8, 0, 0, 0);
        JLabel aide = new JLabel(
                "Sélectionnez un type dans la liste pour modifier son prix.");
        aide.setFont(police(Font.PLAIN, 12));
        aide.setForeground(TEXTE_SECONDAIRE);
        carte.add(aide, g);

        // Ligne 3 : boutons d'action alignes a droite.
        g.gridy = 3;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(16, 0, 0, 0);
        carte.add(creerBarreBoutons(), g);

        // Evenements.
        btnModifier.addActionListener(e -> modifier());
        btnActualiser.addActionListener(e -> actualiser());
        txtPrix.addActionListener(e -> modifier());
        return carte;
    }

    private JPanel creerBarreBoutons() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barre.setOpaque(false);
        barre.add(btnActualiser);
        barre.add(btnModifier);
        return barre;
    }

    // ---------------------------------------------------------------------
    // TABLEAU (carte blanche + compteur + table stylee)
    // ---------------------------------------------------------------------
    private JPanel creerTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(e -> remplirPrix());
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

        JLabel titre = new JLabel("Tarifs : Moto, Voiture, Camion");
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

        table.getColumnModel().getColumn(0).setPreferredWidth(220);

        TableColumn colPrix = table.getColumnModel().getColumn(1);
        colPrix.setPreferredWidth(180);
        colPrix.setCellRenderer(new CelluleRenderer(SwingConstants.RIGHT));

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new EnteteRenderer());

        table.setDefaultRenderer(Object.class, new CelluleRenderer());
    }

    // ---------------------------------------------------------------
    // Operations (inchangees)
    // ---------------------------------------------------------------
    private void actualiser() {
        List<Tarif> liste = dao.lister();
        modeleTable.setRowCount(0);
        for (Tarif t : liste) {
            modeleTable.addRow(new Object[]{t.getTypeVehicule(), t.getPrixHeure()});
        }
        lblCompteur.setText(liste.size() + " resultat(s)");
    }

    private void modifier() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un tarif dans la table.");
            return;
        }
        String prixTexte = txtPrix.getText().trim();
        if (prixTexte.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un prix.");
            return;
        }

        try {
            double prix = Double.parseDouble(prixTexte);
            if (prix < 0) {
                throw new NumberFormatException();
            }
            Tarif t = new Tarif(String.valueOf(modeleTable.getValueAt(ligne, 0)), prix);
            dao.modifier(t);
            JOptionPane.showMessageDialog(this, "Tarif modifie avec succes.");
            actualiser();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Prix invalide. Utilisez un nombre positif.");
        }
    }

    private void remplirPrix() {
        int ligne = table.getSelectedRow();
        if (ligne >= 0) {
            txtPrix.setText(String.valueOf(modeleTable.getValueAt(ligne, 1)));
        }
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

        CelluleRenderer() {
            this(SwingConstants.LEFT);
        }

        CelluleRenderer(int alignement) {
            this.alignement = alignement;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
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

    private static Color fondLigne(boolean selection, int ligne) {
        if (selection) {
            return SELECTION;
        }
        return ligne % 2 == 1 ? LIGNE_ALTERNEE : CARTE;
    }
}
