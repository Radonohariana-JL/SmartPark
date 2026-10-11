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
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import model.Tarif;

/**
 * Gestion des tarifs : liste des categories avec, pour chacune, un bouton
 * "Modifier" ouvrant une fenetre dediee (reproduction du modele de reference
 * {@code reference/Tarif_modell/code.html}, adapte au theme clair).
 * Seule la presentation change : la logique d'acces aux donnees reste inchangee.
 */
public class TarifForm extends JPanel {

    private static final int COL_ACTION = 3;

    private final DefaultTableModel modeleTable = new DefaultTableModel(
            new String[]{"#", "Catégorie de véhicule", "Prix par heure (Ar)", "Action"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Seule la colonne "Action" (bouton de ligne) accepte l'edition.
            return column == COL_ACTION;
        }
    };
    private final JTable table = new JTable(modeleTable);

    private final JLabel lblCompteur = new JLabel("0 catégorie", SwingConstants.RIGHT);

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
        this(true);
    }

    /**
     * @param afficherEntete false lorsqu'il est integre dans une page hote
     *                       (ex. onglet "Tarif" de Parametres) qui fournit
     *                       deja son propre titre.
     */
    public TarifForm(boolean afficherEntete) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        if (afficherEntete) {
            add(creerEntete(), BorderLayout.NORTH);
        }
        add(creerTable(), BorderLayout.CENTER);
        actualiser();
    }

    // ---------------------------------------------------------------------
    // EN-TETE DE PAGE
    // ---------------------------------------------------------------------
    private JPanel creerEntete() {
        JPanel enTete = new JPanel();
        enTete.setOpaque(false);
        enTete.setLayout(new BoxLayout(enTete, BoxLayout.Y_AXIS));

        JLabel titrePage = new JLabel("Configuration des tarifs");
        titrePage.setFont(police(Font.BOLD, 20));
        titrePage.setForeground(TEXTE);
        titrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(titrePage);

        JLabel sousTitrePage = new JLabel(
                "Définir le prix par heure pour chaque catégorie de véhicule.");
        sousTitrePage.setFont(police(Font.PLAIN, 12));
        sousTitrePage.setForeground(TEXTE_SECONDAIRE);
        sousTitrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(sousTitrePage);

        JPanel zoneEnTete = new JPanel(new BorderLayout());
        zoneEnTete.setOpaque(false);
        zoneEnTete.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        zoneEnTete.add(enTete, BorderLayout.NORTH);
        return zoneEnTete;
    }

    // ---------------------------------------------------------------------
    // TABLEAU (carte blanche + compteur + table stylee)
    // ---------------------------------------------------------------------
    private JPanel creerTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
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

        JLabel titre = new JLabel("Registre des tarifs applicables");
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
        table.setRowHeight(38);
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

        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        TableColumn colAction = table.getColumnModel().getColumn(COL_ACTION);
        colAction.setMinWidth(140);
        colAction.setPreferredWidth(140);
        colAction.setMaxWidth(140);
        colAction.setResizable(false);
        colAction.setCellRenderer(new ActionsRenderer());
        colAction.setCellEditor(new ActionsEditor());

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
        int rang = 1;
        for (Tarif t : liste) {
            modeleTable.addRow(new Object[]{rang++, t.getTypeVehicule(), t.getPrixHeure(), ""});
        }
        lblCompteur.setText(liste.size() + (liste.size() > 1 ? " catégories" : " catégorie"));
    }

    /** Ouvre la fenetre de modification du tarif de la ligne indiquee. */
    private void ouvrirModification(int ligne) {
        if (ligne < 0 || ligne >= table.getRowCount()) {
            return;
        }
        String type = String.valueOf(modeleTable.getValueAt(ligne, 1));
        Object valeur = modeleTable.getValueAt(ligne, 2);
        double prixActuel = valeur instanceof Number ? ((Number) valeur).doubleValue() : 0;

        Window parent = SwingUtilities.getWindowAncestor(this);
        FenetreTarif fenetre = new FenetreTarif(parent, type, prixActuel);
        fenetre.setLocationRelativeTo(this);
        fenetre.setVisible(true);

        if (fenetre.estValide()) {
            boolean ok = dao.modifier(new Tarif(type, fenetre.getPrix()));
            actualiser();
            if (ok) {
                JOptionPane.showMessageDialog(this, "Tarif modifié avec succès.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la modification du tarif (base indisponible).");
            }
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

    private static String formatPrix(double valeur) {
        DecimalFormat df = new DecimalFormat("###,##0");
        return df.format(valeur).replace(',', ' ').replace('\u00A0', ' ');
    }

    private static void styleChamp(JTextField champ) {
        champ.setOpaque(true);
        champ.setBackground(CARTE);
        champ.setForeground(TEXTE);
        champ.setCaretColor(PRIMAIRE);
        champ.setFont(police(Font.PLAIN, 13));
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
    // FENETRE DE MODIFICATION
    // ---------------------------------------------------------------------
    private static class FenetreTarif extends JDialog {

        private final JTextField champPrix = new JTextField();
        private final BoutonStyle btnAnnuler = new BoutonStyle("Annuler", BoutonStyle.Type.NEUTRE, false);
        private final BoutonStyle btnEnregistrer = new BoutonStyle("Enregistrer", BoutonStyle.Type.PRIMAIRE, false);

        private boolean valide;
        private double prix;

        FenetreTarif(Window parent, String type, double prixActuel) {
            super(parent, "Modifier le tarif", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setResizable(false);

            JPanel carte = new JPanel(new GridBagLayout());
            carte.setBackground(CARTE);
            carte.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST;
            g.weightx = 1;
            g.gridx = 0;
            g.gridwidth = 1;

            // Titre.
            g.gridy = 0;
            g.insets = new Insets(0, 0, 16, 0);
            JLabel titre = new JLabel("Modifier le tarif");
            titre.setFont(police(Font.BOLD, 16));
            titre.setForeground(TEXTE);
            carte.add(titre, g);

            // Type de vehicule (lecture seule).
            g.gridy = 1;
            g.insets = new Insets(0, 0, 0, 0);
            carte.add(libelle("Type de véhicule"), g);

            g.gridy = 2;
            g.insets = new Insets(6, 0, 0, 0);
            JLabel valeurType = new JLabel(type);
            valeurType.setFont(police(Font.BOLD, 14));
            valeurType.setForeground(TEXTE);
            JPanel zoneType = new JPanel(new BorderLayout());
            zoneType.setOpaque(true);
            zoneType.setBackground(FOND);
            zoneType.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDURE_CHAMP, 6, 1, new Insets(0, 0, 0, 0)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            zoneType.add(valeurType, BorderLayout.WEST);
            carte.add(zoneType, g);

            // Prix par heure.
            g.gridy = 3;
            g.insets = new Insets(16, 0, 0, 0);
            carte.add(libelle("Prix par heure (Ar)"), g);

            g.gridy = 4;
            g.insets = new Insets(6, 0, 0, 0);
            champPrix.setText(formatPrix(prixActuel));
            styleChamp(champPrix);
            champPrix.setPreferredSize(new Dimension(220, 36));
            JLabel suffixe = new JLabel("Ar / h");
            suffixe.setFont(police(Font.BOLD, 13));
            suffixe.setForeground(TEXTE_SECONDAIRE);
            suffixe.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            JPanel zonePrix = new JPanel();
            zonePrix.setOpaque(false);
            zonePrix.setLayout(new BoxLayout(zonePrix, BoxLayout.X_AXIS));
            zonePrix.add(champPrix);
            zonePrix.add(suffixe);
            zonePrix.add(Box.createHorizontalGlue());
            carte.add(zonePrix, g);

            // Aide.
            g.gridy = 5;
            g.insets = new Insets(16, 0, 0, 0);
            JLabel info = new JLabel(
                    "<html><body style='width:300px'>Le nouveau prix s'appliquera immédiatement "
                    + "au calcul du montant du stationnement.</body></html>");
            info.setFont(police(Font.PLAIN, 12));
            info.setForeground(TEXTE_SECONDAIRE);
            carte.add(info, g);

            // Boutons.
            g.gridy = 6;
            g.anchor = GridBagConstraints.EAST;
            g.fill = GridBagConstraints.NONE;
            g.insets = new Insets(20, 0, 0, 0);
            JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            barre.setOpaque(false);
            barre.add(btnAnnuler);
            barre.add(btnEnregistrer);
            carte.add(barre, g);

            btnAnnuler.addActionListener(e -> dispose());
            btnEnregistrer.addActionListener(e -> enregistrer());
            champPrix.addActionListener(e -> enregistrer());

            setContentPane(carte);
            pack();
        }

        private void enregistrer() {
            String texte = champPrix.getText().trim().replace(" ", "").replace("\u00A0", "");
            try {
                double valeur = Double.parseDouble(texte);
                if (valeur < 0) {
                    throw new NumberFormatException();
                }
                prix = valeur;
                valide = true;
                dispose();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Prix invalide. Utilisez un nombre positif.");
            }
        }

        boolean estValide() {
            return valide;
        }

        double getPrix() {
            return prix;
        }
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
            label.setHorizontalAlignment(c == COL_ACTION ? SwingConstants.RIGHT
                    : (c == 0 ? SwingConstants.CENTER : SwingConstants.LEFT));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDURE),
                    BorderFactory.createEmptyBorder(4, 12, 4, 8)));
            return label;
        }
    }

    private static class CelluleRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
            label.setBackground(sel ? SELECTION : fondLigne(sel, r));
            label.setForeground(TEXTE);
            switch (c) {
                case 0:
                    label.setFont(police(Font.BOLD, 12));
                    label.setForeground(sel ? TEXTE : TEXTE_SECONDAIRE);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    break;
                case 1:
                    label.setFont(police(Font.BOLD, 13));
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    break;
                case 2:
                    label.setFont(police(Font.PLAIN, 13));
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                    if (v instanceof Number) {
                        label.setText(formatPrix(((Number) v).doubleValue()));
                    }
                    break;
                default:
                    label.setFont(police(Font.PLAIN, 13));
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    break;
            }
            return label;
        }
    }

    private static Color fondLigne(boolean selection, int ligne) {
        if (selection) {
            return SELECTION;
        }
        return ligne % 2 == 1 ? LIGNE_ALTERNEE : CARTE;
    }

    /** Renderer de la colonne "Action" : bouton "Modifier" par ligne. */
    private static class ActionsRenderer implements TableCellRenderer {

        private final BoutonStyle bouton = new BoutonStyle("Modifier", BoutonStyle.Type.NEUTRE, true);

        ActionsRenderer() {
            bouton.setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            JPanel panneau = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
            panneau.setOpaque(true);
            panneau.setBackground(fondLigne(sel, r));
            panneau.add(bouton);
            return panneau;
        }
    }

    /** Editeur de la colonne "Action" : bouton actif ouvrant la fenetre. */
    private class ActionsEditor extends AbstractCellEditor implements TableCellEditor {

        private final BoutonStyle bouton = new BoutonStyle("Modifier", BoutonStyle.Type.PRIMAIRE, true);
        private final JPanel panneau = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        private int ligne;

        ActionsEditor() {
            bouton.setFocusable(false);
            panneau.setOpaque(true);
            panneau.add(bouton);
            bouton.addActionListener(e -> declencher());
        }

        private void declencher() {
            final int l = ligne;
            fireEditingStopped();
            SwingUtilities.invokeLater(() -> ouvrirModification(l));
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel,
                int r, int c) {
            ligne = r;
            panneau.setBackground(fondLigne(true, r));
            return panneau;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
