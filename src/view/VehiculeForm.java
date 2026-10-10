package view;

import dao.ProprietaireDAO;
import dao.VehiculeDAO;
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
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import model.Proprietaire;
import model.Vehicule;

/**
 * Gestion des vehicules : ajout, modification, suppression, recherche.
 * Seul le design est personnalise : la logique CRUD est inchangee.
 */
public class VehiculeForm extends JPanel {

    private static final int COL_ACTIONS = 6;

    private final JTextField txtImmatriculation = new JTextField(12);
    private final JTextField txtMarque = new JTextField(12);
    private final JTextField txtCouleur = new JTextField(10);

    private final JComboBox<String> comboType = new JComboBox<>(
            new String[]{"Moto", "Voiture", "Camion"});
    private final JComboBox<Proprietaire> comboProprietaire = new JComboBox<>();

    private final JTextField txtRecherche = new JTextField(15);

    private final JButton btnAjouter = new BoutonStyle("Ajouter", BoutonStyle.Type.PRIMAIRE, false);
    // btnModifier est reutilise comme « Enregistrer les modifications » (mode edition).
    private final JButton btnModifier = new BoutonStyle("Enregistrer les modifications", BoutonStyle.Type.PRIMAIRE, false);
    private final JButton btnAnnuler = new BoutonStyle("Annuler", BoutonStyle.Type.NEUTRE, false);
    // btnSupprimer reste declare mais n'est plus affiche (action dans la colonne Actions).
    private final JButton btnSupprimer = new BoutonStyle("Supprimer", BoutonStyle.Type.DESTRUCTIF, false);
    private final JButton btnActualiser = new BoutonStyle("Actualiser", BoutonStyle.Type.NEUTRE, false);
    private final JButton btnRechercher = new BoutonStyle("Rechercher", BoutonStyle.Type.NEUTRE, false);

    private final DefaultTableModel modeleTable = new DefaultTableModel(
            new String[]{"ID", "Immatriculation", "Marque", "Couleur", "Type", "Proprietaire", "Actions"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == COL_ACTIONS;
        }
    };
    private final JTable table = new JTable(modeleTable);

    private final JLabel lblCompteur = new JLabel("0 resultat(s)", SwingConstants.RIGHT);
    private final JLabel lblBandeau = new JLabel("Modification du véhicule sélectionné");

    private final VehiculeDAO dao = new VehiculeDAO();

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

    public VehiculeForm() {
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

        JLabel titrePage = new JLabel("Véhicules");
        titrePage.setFont(police(Font.BOLD, 20));
        titrePage.setForeground(TEXTE);
        titrePage.setAlignmentX(Component.LEFT_ALIGNMENT);
        enTete.add(titrePage);

        JLabel sousTitrePage = new JLabel(
                "Gérer les véhicules : ajout, modification, suppression et recherche.");
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

    /** Carte blanche contenant les champs de saisie et les boutons. */
    private JPanel creerCarteFormulaire() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        styleChamp(txtImmatriculation);
        styleChamp(txtMarque);
        styleChamp(txtCouleur);
        styleChamp(txtRecherche);
        styleCombo(comboType);
        styleCombo(comboProprietaire);

        // Bandeau du mode edition (cache par defaut).
        lblBandeau.setFont(police(Font.BOLD, 12));
        lblBandeau.setForeground(PRIMAIRE);
        lblBandeau.setOpaque(true);
        lblBandeau.setBackground(SELECTION);
        lblBandeau.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        lblBandeau.setVisible(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;

        // Ligne 0 : bandeau d'edition.
        g.gridy = 0;
        g.gridx = 0;
        g.gridwidth = 3;
        g.insets = new Insets(0, 0, 16, 0);
        carte.add(lblBandeau, g);

        // Ligne 1 : libelles (Immatriculation, Marque, Couleur).
        g.gridy = 1;
        g.gridwidth = 1;
        g.insets = new Insets(0, 0, 0, 16);
        g.gridx = 0;
        carte.add(libelle("Immatriculation"), g);
        g.gridx = 1;
        carte.add(libelle("Marque"), g);
        g.gridx = 2;
        g.insets = new Insets(0, 0, 0, 0);
        carte.add(libelle("Couleur"), g);

        // Ligne 2 : champs.
        g.gridy = 2;
        g.insets = new Insets(8, 0, 0, 16);
        g.gridx = 0;
        carte.add(txtImmatriculation, g);
        g.gridx = 1;
        carte.add(txtMarque, g);
        g.gridx = 2;
        g.insets = new Insets(8, 0, 0, 0);
        carte.add(txtCouleur, g);

        // Ligne 3 : libelles (Type, Proprietaire).
        g.gridy = 3;
        g.gridx = 0;
        g.insets = new Insets(16, 0, 0, 16);
        carte.add(libelle("Type"), g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.insets = new Insets(16, 0, 0, 0);
        carte.add(libelle("Propriétaire"), g);

        // Ligne 4 : combos.
        g.gridy = 4;
        g.gridx = 0;
        g.gridwidth = 1;
        g.insets = new Insets(8, 0, 0, 16);
        carte.add(comboType, g);
        g.gridx = 1;
        g.gridwidth = 2;
        g.insets = new Insets(8, 0, 0, 0);
        carte.add(comboProprietaire, g);

        // Ligne 5 : separateur.
        g.gridy = 5;
        g.gridx = 0;
        g.gridwidth = 3;
        g.insets = new Insets(16, 0, 16, 0);
        JPanel separateur = new JPanel();
        separateur.setOpaque(true);
        separateur.setBackground(BORDURE);
        separateur.setPreferredSize(new Dimension(1, 1));
        carte.add(separateur, g);

        // Ligne 6 : barre de recherche.
        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        carte.add(creerBarreRecherche(), g);

        // Ligne 7 : boutons d'action alignes a droite.
        g.gridy = 7;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(16, 0, 0, 0);
        carte.add(creerBarreBoutons(), g);

        // Evenements.
        btnAjouter.addActionListener(e -> ajouter());
        btnModifier.addActionListener(e -> {
            modifier();
            if (table.getSelectedRow() < 0) {
                activerModeEdition(false);
            }
        });
        btnAnnuler.addActionListener(e -> {
            viderChamps();
            table.clearSelection();
            activerModeEdition(false);
        });
        btnActualiser.addActionListener(e -> {
            viderChamps();
            actualiser();
            activerModeEdition(false);
        });
        btnRechercher.addActionListener(e -> rechercher());
        txtRecherche.addActionListener(e -> rechercher());

        activerModeEdition(false);
        return carte;
    }

    private JPanel creerBarreRecherche() {
        JPanel barre = new JPanel(new BorderLayout(8, 0));
        barre.setOpaque(false);
        barre.add(libelle("Rechercher"), BorderLayout.WEST);
        barre.add(txtRecherche, BorderLayout.CENTER);
        barre.add(btnRechercher, BorderLayout.EAST);
        return barre;
    }

    private JPanel creerBarreBoutons() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barre.setOpaque(false);
        barre.add(btnAnnuler);
        barre.add(btnModifier);
        barre.add(btnActualiser);
        barre.add(btnAjouter);
        return barre;
    }

    private void activerModeEdition(boolean actif) {
        lblBandeau.setVisible(actif);
        btnModifier.setVisible(actif);
        btnAnnuler.setVisible(actif);
        btnAjouter.setEnabled(!actif);
    }

    // ---------------------------------------------------------------------
    // TABLEAU (carte blanche + compteur + table stylee)
    // ---------------------------------------------------------------------
    private JPanel creerTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(e -> remplirFormulaire());
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

        JLabel titre = new JLabel("Liste des véhicules");
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

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(90);

        javax.swing.table.TableColumn colActions = table.getColumnModel().getColumn(COL_ACTIONS);
        colActions.setMinWidth(190);
        colActions.setPreferredWidth(190);
        colActions.setMaxWidth(190);
        colActions.setResizable(false);
        colActions.setCellRenderer(new ActionsRenderer());
        colActions.setCellEditor(new ActionsEditor());

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new EnteteRenderer());

        table.setDefaultRenderer(Object.class, new CelluleRenderer());
    }

    // ---------------------------------------------------------------
    // Operations CRUD (inchangees)
    // ---------------------------------------------------------------
    private void ajouter() {
        if (!champsValides()) {
            return;
        }
        Vehicule v = new Vehicule(
                txtImmatriculation.getText().trim().toUpperCase(),
                txtMarque.getText().trim(),
                txtCouleur.getText().trim(),
                (String) comboType.getSelectedItem(),
                ((Proprietaire) comboProprietaire.getSelectedItem()).getIdProprietaire());
        dao.ajouter(v);
        JOptionPane.showMessageDialog(this, "Vehicule ajoute avec succes.");
        viderChamps();
        actualiser();
    }

    private void modifier() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un vehicule dans la table.");
            return;
        }
        if (!champsValides()) {
            return;
        }
        Vehicule v = new Vehicule(
                txtImmatriculation.getText().trim().toUpperCase(),
                txtMarque.getText().trim(),
                txtCouleur.getText().trim(),
                (String) comboType.getSelectedItem(),
                ((Proprietaire) comboProprietaire.getSelectedItem()).getIdProprietaire());
        v.setIdVehicule((int) modeleTable.getValueAt(ligne, 0));
        dao.modifier(v);
        JOptionPane.showMessageDialog(this, "Vehicule modifie avec succes.");
        actualiser();
    }

    private void supprimer() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un vehicule dans la table.");
            return;
        }
        int id = (int) modeleTable.getValueAt(ligne, 0);
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer ce vehicule ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            dao.supprimer(id);
            viderChamps();
            actualiser();
        }
    }

    private void rechercher() {
        chargerDonnees(dao.rechercher(txtRecherche.getText().trim()));
    }

    private void actualiser() {
        chargerProprietaires();
        chargerDonnees(dao.lister());
    }

    private void chargerProprietaires() {
        comboProprietaire.removeAllItems();
        List<Proprietaire> liste = new ProprietaireDAO().lister();
        for (Proprietaire p : liste) {
            comboProprietaire.addItem(p);
        }
    }

    private void chargerDonnees(List<Vehicule> liste) {
        modeleTable.setRowCount(0);
        for (Vehicule v : liste) {
            modeleTable.addRow(new Object[]{
                v.getIdVehicule(), v.getImmatriculation(), v.getMarque(),
                v.getCouleur(), v.getTypeVehicule(), v.getNomProprietaire(), ""
            });
        }
        lblCompteur.setText(liste.size() + " resultat(s)");
    }

    // ---------------------------------------------------------------
    // Divers (inchanges)
    // ---------------------------------------------------------------
    private boolean champsValides() {
        if (comboProprietaire.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Aucun proprietaire. Ajoutez d'abord un proprietaire.");
            return false;
        }
        if (txtImmatriculation.getText().trim().isEmpty()
                || txtMarque.getText().trim().isEmpty()
                || txtCouleur.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs !");
            return false;
        }
        return true;
    }

    private void remplirFormulaire() {
        int ligne = table.getSelectedRow();
        if (ligne >= 0) {
            txtImmatriculation.setText(String.valueOf(modeleTable.getValueAt(ligne, 1)));
            txtMarque.setText(String.valueOf(modeleTable.getValueAt(ligne, 2)));
            txtCouleur.setText(String.valueOf(modeleTable.getValueAt(ligne, 3)));
            comboType.setSelectedItem(String.valueOf(modeleTable.getValueAt(ligne, 4)));
            String proprietaire = String.valueOf(modeleTable.getValueAt(ligne, 5));
            for (int i = 0; i < comboProprietaire.getItemCount(); i++) {
                if (comboProprietaire.getItemAt(i).toString().equals(proprietaire)) {
                    comboProprietaire.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void viderChamps() {
        txtImmatriculation.setText("");
        txtMarque.setText("");
        txtCouleur.setText("");
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

    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setOpaque(true);
        combo.setBackground(CARTE);
        combo.setForeground(TEXTE);
        combo.setFont(police(Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(120, 36));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setBorder(BorderFactory.createLineBorder(BORDURE_CHAMP));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new ComboRenderer());
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

    private static class ComboRenderer implements ListCellRenderer<Object> {
        private final javax.swing.DefaultListCellRenderer base = new javax.swing.DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) base.getListCellRendererComponent(
                    list, value, index, selected, focus);
            label.setText(value == null ? "Sélectionnez un propriétaire" : value.toString());
            label.setFont(police(Font.PLAIN, 13));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            label.setBackground(selected ? SELECTION : CARTE);
            label.setForeground(value == null ? TEXTE_SECONDAIRE : TEXTE);
            return label;
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
            label.setHorizontalAlignment(c == COL_ACTIONS ? SwingConstants.CENTER : SwingConstants.LEFT);
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
            label.setFont(police(Font.PLAIN, 13));
            label.setHorizontalAlignment(SwingConstants.LEFT);
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

    private static JPanel creerPanneauActions(JButton modifier, JButton supprimer) {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setOpaque(true);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 4, 0, 4);
        g.gridy = 0;
        g.gridx = 0;
        panneau.add(modifier, g);
        g.gridx = 1;
        panneau.add(supprimer, g);
        return panneau;
    }

    private static class ActionsRenderer implements TableCellRenderer {
        private final JPanel panneau = creerPanneauActions(
                new BoutonStyle("Modifier", BoutonStyle.Type.SECONDAIRE, true),
                new BoutonStyle("Supprimer", BoutonStyle.Type.DESTRUCTIF, true));

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            panneau.setBackground(fondLigne(sel, r));
            return panneau;
        }
    }

    private class ActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final BoutonStyle btnEdit = new BoutonStyle("Modifier", BoutonStyle.Type.SECONDAIRE, true);
        private final BoutonStyle btnSuppr = new BoutonStyle("Supprimer", BoutonStyle.Type.DESTRUCTIF, true);
        private final JPanel panneau = creerPanneauActions(btnEdit, btnSuppr);
        private int ligneCourante = -1;

        ActionsEditor() {
            btnEdit.addActionListener(e -> declencher(true));
            btnSuppr.addActionListener(e -> declencher(false));
        }

        private void declencher(boolean modification) {
            final int ligne = ligneCourante;
            stopCellEditing();
            SwingUtilities.invokeLater(() -> {
                if (ligne < 0 || ligne >= table.getRowCount()) {
                    return;
                }
                table.setRowSelectionInterval(ligne, ligne);
                if (modification) {
                    activerModeEdition(true);
                } else {
                    supprimer();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel,
                int r, int c) {
            ligneCourante = r;
            panneau.setBackground(fondLigne(true, r));
            return panneau;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}