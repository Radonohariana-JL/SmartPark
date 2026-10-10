package view;

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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import dao.ProprietaireDAO;
import model.Proprietaire;

/**
 * Gestion des proprietaires : ajout, modification, suppression, recherche.
 * Reproduction du modele de reference {@code reference/Proprietaire_model/code.html}
 * (dimensions exactes : titre display-md 24px, sous-titre headline-sm 14px,
 * outil de recherche 380px, en-tete de tableau h-40 label-sm majuscule,
 * lignes py-3.5 (46px), colonnes 80/220/220/220/140, compteur en pastille
 * "N proprietaire(s)" avec point vert). Le formulaire reste une carte MASQUEE
 * (modele sans formulaire) ouverte par le bouton "Ajouter un proprietaire".
 * La logique CRUD et les objets JTextField/JTable sont inchanges.
 */
public class ProprietaireFormPanel extends JPanel {

    private final JTextField txtNom = new JTextField(15);
    private final JTextField txtPrenom = new JTextField(15);
    private final JTextField txtTelephone = new JTextField(15);
    private final JTextField txtRecherche = new JTextField(15);

    private final JButton btnOuvrirAjout = new BoutonStyle("Ajouter un propriétaire",
            BoutonStyle.Type.PRIMAIRE, 14, 36, Icone.Type.AJOUTER);
    private final JButton btnAjouter = new BoutonStyle("Ajouter", BoutonStyle.Type.PRIMAIRE);
    private final JButton btnActualiser = new BoutonStyle("Actualiser", BoutonStyle.Type.NEUTRE);
    private final JButton btnRechercher = new BoutonStyle("Rechercher", BoutonStyle.Type.NEUTRE);
    private final JButton btnEnregistrer = new BoutonStyle("Enregistrer les modifications",
            BoutonStyle.Type.PRIMAIRE, 13, 36, Icone.Type.COCHE);
    private final JButton btnAnnuler = new BoutonStyle("Annuler", BoutonStyle.Type.NEUTRE);

    private final DefaultTableModel modeleTable = new DefaultTableModel(
            new String[]{"ID", "NOM", "PRÉNOM", "TÉLÉPHONE", "ACTIONS"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Seule la colonne "Actions" (boutons de ligne) accepte l'edition.
            return column == COL_ACTIONS;
        }
    };
    private final JTable table = new JTable(modeleTable);

    private final JLabel lblCompteur = new JLabel("0 propriétaire", SwingConstants.LEFT);
    private final JLabel lblBandeau = new JLabel();

    /** Conteneur de la carte formulaire : masque par defaut. */
    private final JPanel zoneFormulaire = new JPanel(new BorderLayout());

    private final JPanel pied = new JPanel();
    private final JPanel separateur = new JPanel();
    private final JPanel bandeauEdition = new JPanel(new BorderLayout(8, 0));
    private final JPanel barreAjout;
    private final JPanel barreEdition;

    private final Timer timerRecherche = new Timer(250, e -> rechercher());

    private boolean modeEdition;
    private boolean carteOuverte;

    private final ProprietaireDAO dao = new ProprietaireDAO();

    // ---------------------------------------------------------------------
    // PALETTE DE COULEURS (version claire)
    // ---------------------------------------------------------------------
    private static final Color FOND              = new Color(0xF1F5F9);
    private static final Color CARTE             = new Color(0xFFFFFF);
    private static final Color BORDURE           = new Color(0xE2E8F0);
    private static final Color PRIMAIRE          = new Color(0x2563EB);
    private static final Color PRIMAIRE_HOVER    = new Color(0x1D4ED8);
    private static final Color PRIMAIRE_PRESSED  = new Color(0x1E40AF);
    private static final Color SECONDAIRE        = new Color(0x64748B);
    private static final Color SECONDAIRE_HOVER  = new Color(0x475569);
    private static final Color SECONDAIRE_PRESSED = new Color(0x334155);
    private static final Color DESTRUCTIF        = new Color(0xDC2626);
    private static final Color DESTRUCTIF_HOVER  = new Color(0xB91C1C);
    private static final Color DESTRUCTIF_PRESSED = new Color(0x7F1D1D);
    private static final Color TEXTE             = new Color(0x0F172A);
    private static final Color TEXTE_SECONDAIRE  = new Color(0x64748B);
    private static final Color LABEL             = new Color(0x334155);
    private static final Color BORDURE_CHAMP     = new Color(0xCBD5E1);
    private static final Color LIGNE_ALTERNEE    = new Color(0xF8FAFC);
    private static final Color SURVOL_LIGNE      = new Color(0xEEF2F7);
    private static final Color SELECTION         = new Color(0xDBEAFE);
    private static final Color VERT              = new Color(0x16A34A);
    private static final Color BANDEAU_BORDURE   = new Color(0xBFDBFE);
    private static final Color SURVOL_MODIFIER   = new Color(0xDBEAFE);
    private static final Color SURVOL_SUPPRIMER  = new Color(0xFEE2E2);

    // Colonne "Actions" : largeur fixe, boutons Modifier / Supprimer.
    private static final int COL_ACTIONS = 4;
    private static final int COTE_ACTION = 30;
    private static final int ECART_ACTIONS = 6;
    private static final int MARGE_DROITE_ACTIONS = 8;
    private static final int GRANDE_LARGEUR = 4000;

    /** Ligne survolee (fond gris clair) ; -1 si aucune. */
    private int ligneSurvol = -1;

    public ProprietaireFormPanel() {
        barreAjout = creerBarreAjout();
        barreEdition = creerBarreEdition();
        timerRecherche.setRepeats(false);

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        add(creerEntete(), BorderLayout.NORTH);
        add(creerCorps(), BorderLayout.CENTER);
        actualiser();
    }

    // ---------------------------------------------------------------------
    // EN-TETE DE PAGE + OUTILS
    // ---------------------------------------------------------------------
    /** Titre (display-md 24px) + sous-titre (headline-sm 14px) a gauche,
     *  bouton "Ajouter un proprietaire" en haut a droite (seul). */
    private JPanel creerEntete() {
        JPanel nord = new JPanel();
        nord.setOpaque(false);
        nord.setLayout(new BoxLayout(nord, BoxLayout.Y_AXIS));

        JPanel ligneTitre = new JPanel(new BorderLayout(16, 0));
        ligneTitre.setOpaque(false);
        ligneTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        ligneTitre.setMaximumSize(new Dimension(GRANDE_LARGEUR, 64));

        JPanel bloc = new JPanel();
        bloc.setOpaque(false);
        bloc.setLayout(new BoxLayout(bloc, BoxLayout.Y_AXIS));
        JLabel titre = new JLabel("Gestion des propriétaires");
        titre.setFont(police(Font.BOLD, 24));
        titre.setForeground(TEXTE);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sousTitre = new JLabel(
                "Gérez les informations des propriétaires de véhicules enregistrés dans le parking.");
        sousTitre.setFont(police(Font.PLAIN, 14));
        sousTitre.setForeground(TEXTE_SECONDAIRE);
        sousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        sousTitre.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        bloc.add(titre);
        bloc.add(sousTitre);
        ligneTitre.add(bloc, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        droite.setOpaque(false);
        droite.add(btnOuvrirAjout);
        ligneTitre.add(droite, BorderLayout.EAST);

        nord.add(ligneTitre);
        nord.add(Box.createVerticalStrut(4));

        nord.add(creerBarreOutils());
        nord.add(Box.createVerticalStrut(12));

        JPanel enveloppe = new JPanel(new BorderLayout());
        enveloppe.setOpaque(false);
        enveloppe.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enveloppe.add(nord, BorderLayout.NORTH);
        return enveloppe;
    }

    /** Barre d'outils : recherche (380px) a gauche, compteur-pastille a droite. */
    private JPanel creerBarreOutils() {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.setAlignmentX(Component.LEFT_ALIGNMENT);
        ligne.setMaximumSize(new Dimension(GRANDE_LARGEUR, 36));
        ligne.setPreferredSize(new Dimension(100, 36));

        JPanel boite = new JPanel(new BorderLayout());
        boite.setOpaque(true);
        boite.setBackground(CARTE);
        boite.setBorder(rechercheBorder(BORDURE_CHAMP, 1));

        JLabel loupe = new JLabel(new Icone(Icone.Type.RECHERCHE, 18, TEXTE_SECONDAIRE));
        loupe.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        boite.add(loupe, BorderLayout.WEST);

        txtRecherche.setOpaque(false);
        txtRecherche.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        txtRecherche.setFont(police(Font.PLAIN, 13));
        txtRecherche.setForeground(TEXTE);
        txtRecherche.setCaretColor(PRIMAIRE);
        boite.add(txtRecherche, BorderLayout.CENTER);
        boite.setPreferredSize(new Dimension(380, 36));
        boite.setMaximumSize(new Dimension(380, 36));

        txtRecherche.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                boite.setBorder(rechercheBorder(PRIMAIRE, 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                boite.setBorder(rechercheBorder(BORDURE_CHAMP, 1));
            }
        });

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gauche.setOpaque(false);
        gauche.add(boite);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        droite.setOpaque(false);
        droite.add(creerCompteur());

        ligne.add(gauche, BorderLayout.WEST);
        ligne.add(droite, BorderLayout.EAST);
        return ligne;
    }

    /** Pastille de comptage "N proprietaire(s)" (bulle pleine + point vert). */
    private JPanel creerCompteur() {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pill.setOpaque(true);
        pill.setBackground(LIGNE_ALTERNEE);
        pill.setBorder(new RoundedBorder(BORDURE, 12, 1, new Insets(0, 0, 0, 0)));
        pill.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 12, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));
        pill.add(new PastilleDot(VERT, 8));
        lblCompteur.setFont(police(Font.BOLD, 12));
        lblCompteur.setForeground(TEXTE_SECONDAIRE);
        pill.add(lblCompteur);
        return pill;
    }

    /** Bordure arrondie de la zone de recherche (rayon xl 8px). */
    private static Border rechercheBorder(Color couleur, int epaisseur) {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(couleur, 8, epaisseur, new Insets(1, 1, 1, 1)),
                BorderFactory.createEmptyBorder(0, 12, 0, 0));
    }

    // ---------------------------------------------------------------------
    // CORPS : carte formulaire (masquee) + tableau
    // ---------------------------------------------------------------------
    private JPanel creerCorps() {
        JPanel corps = new JPanel(new BorderLayout());
        corps.setOpaque(false);

        zoneFormulaire.setOpaque(false);
        zoneFormulaire.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        zoneFormulaire.add(creerCarteFormulaire(), BorderLayout.CENTER);
        zoneFormulaire.setVisible(false);

        corps.add(zoneFormulaire, BorderLayout.NORTH);
        corps.add(creerTable(), BorderLayout.CENTER);
        return corps;
    }

    /** Carte blanche (masquee par defaut) contenant les champs et les boutons. */
    private JPanel creerCarteFormulaire() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        styleChamp(txtNom);
        styleChamp(txtPrenom);
        styleChamp(txtTelephone);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 10, 12);
        carte.add(libelle("Nom"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 10, 0);
        carte.add(txtNom, g);

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 10, 12);
        carte.add(libelle("Prénom"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 10, 0);
        carte.add(txtPrenom, g);

        g.gridx = 0;
        g.gridy = 2;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 0, 12);
        carte.add(libelle("Téléphone"), g);
        g.gridx = 1;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 0, 0);
        carte.add(txtTelephone, g);

        preparerPied();

        g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(18, 0, 0, 0);
        carte.add(pied, g);

        // Evenements (noms et comportements inchanges).
        btnOuvrirAjout.addActionListener(e -> ouvrirAjout());
        btnAjouter.addActionListener(e -> ajouter());
        btnActualiser.addActionListener(e -> {
            viderChamps();
            actualiser();
        });
        btnRechercher.addActionListener(e -> rechercher());
        txtRecherche.addActionListener(e -> rechercher());
        txtRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timerRecherche.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timerRecherche.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                timerRecherche.restart();
            }
        });
        btnEnregistrer.addActionListener(e -> enregistrerModification());
        btnAnnuler.addActionListener(e -> annulerEdition());

        return carte;
    }

    /** Prepare le bas de carte (separateur + bandeau edition + barres de boutons). */
    private void preparerPied() {
        separateur.setOpaque(true);
        separateur.setBackground(BORDURE);
        separateur.setPreferredSize(new Dimension(1, 1));
        separateur.setMaximumSize(new Dimension(GRANDE_LARGEUR, 1));
        separateur.setMinimumSize(new Dimension(0, 1));
        separateur.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblBandeau.setText("Modification du propriétaire sélectionné");
        lblBandeau.setFont(police(Font.BOLD, 12));
        lblBandeau.setForeground(PRIMAIRE_PRESSED);
        lblBandeau.setIcon(new Icone(Icone.Type.UTILISATEUR, 16, PRIMAIRE_PRESSED));
        lblBandeau.setIconTextGap(8);

        bandeauEdition.setOpaque(true);
        bandeauEdition.setBackground(SELECTION);
        bandeauEdition.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BANDEAU_BORDURE, 8, 1, new Insets(1, 1, 1, 1)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        bandeauEdition.add(lblBandeau, BorderLayout.CENTER);
        bandeauEdition.setAlignmentX(Component.LEFT_ALIGNMENT);
        bandeauEdition.setPreferredSize(new Dimension(100, 38));
        bandeauEdition.setMaximumSize(new Dimension(GRANDE_LARGEUR, 38));

        barreAjout.setAlignmentX(Component.LEFT_ALIGNMENT);
        barreAjout.setPreferredSize(new Dimension(100, 36));
        barreAjout.setMaximumSize(new Dimension(GRANDE_LARGEUR, 36));
        barreEdition.setAlignmentX(Component.LEFT_ALIGNMENT);
        barreEdition.setPreferredSize(new Dimension(100, 36));
        barreEdition.setMaximumSize(new Dimension(GRANDE_LARGEUR, 36));

        pied.setLayout(new BoxLayout(pied, BoxLayout.Y_AXIS));
        pied.setOpaque(false);
        reconstruirePied();
    }

    /** Reconstruit le pied selon l'etat (ajout ou edition). */
    private void reconstruirePied() {
        pied.removeAll();
        if (carteOuverte) {
            pied.add(separateur);
            pied.add(Box.createVerticalStrut(14));
            if (modeEdition) {
                pied.add(bandeauEdition);
                pied.add(Box.createVerticalStrut(14));
                pied.add(barreEdition);
            } else {
                pied.add(barreAjout);
            }
        }
        pied.revalidate();
        pied.repaint();
    }

    /** Rangee de boutons en mode ajout (Annuler puis Ajouter). */
    private JPanel creerBarreAjout() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barre.setOpaque(false);
        barre.add(btnAnnuler);
        barre.add(btnAjouter);
        return barre;
    }

    /** Rangee de boutons en mode edition (Annuler puis Enregistrer). */
    private JPanel creerBarreEdition() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barre.setOpaque(false);
        barre.add(btnAnnuler);
        barre.add(btnEnregistrer);
        return barre;
    }

    // ---------------------------------------------------------------------
    // TABLEAU
    // ---------------------------------------------------------------------
    private JPanel creerTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(e -> remplirFormulaire());

        // Survol : ligne entiere + boutons de la colonne "Actions".
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mettreAJourSurvol(e);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                reinitialiserSurvol();
            }
        });

        styleTable();

        JScrollPane defilement = new JScrollPane(table);
        defilement.setBorder(new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)));
        defilement.setOpaque(true);
        defilement.setBackground(CARTE);
        defilement.getViewport().setBackground(CARTE);
        defilement.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        defilement.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel carte = new JPanel(new BorderLayout());
        carte.setOpaque(false);
        carte.add(defilement, BorderLayout.CENTER);
        return carte;
    }

    /** Applique le style visuel du tableau (en-tete h-10 40px, lignes 46px). */
    private void styleTable() {
        table.setRowHeight(46);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXTE);
        table.setFont(police(Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Largeurs de colonnes du modele : 80 / 220 / 220 / 220 / 140.
        int[] largeurs = {80, 220, 220, 220, 140};
        for (int i = 0; i < largeurs.length; i++) {
            TableColumn colonne = table.getColumnModel().getColumn(i);
            colonne.setPreferredWidth(largeurs[i]);
            colonne.setMinWidth(largeurs[i]);
        }

        // Colonne "Actions" : largeur fixe, non redimensionnable, boutons de ligne.
        TableColumn colonneActions = table.getColumnModel().getColumn(COL_ACTIONS);
        colonneActions.setMaxWidth(140);
        colonneActions.setResizable(false);
        colonneActions.setCellRenderer(new ActionsRenderer());
        colonneActions.setCellEditor(new ActionsEditor());

        // En-tete personnalise : hauteur 40px, label-sm (10px) en majuscules.
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setDefaultRenderer(new EnteteRenderer());

        // Cellules : lignes alternées + survol + sélection.
        table.setDefaultRenderer(Object.class, new CelluleRenderer());
    }

    // ---------------------------------------------------------------------
    // SURVOL DES BOUTONS DE LA COLONNE "ACTIONS"
    // ---------------------------------------------------------------------
    private int ligneSurvolee = -1;
    private boolean survolModifier;
    private boolean survolSupprimer;

    /** Memorise la ligne survolee et le bouton survole dans la colonne "Actions". */
    private void mettreAJourSurvol(MouseEvent e) {
        int ligne = table.rowAtPoint(e.getPoint());
        int colonne = table.columnAtPoint(e.getPoint());
        boolean nouveauModifier = false;
        boolean nouveauSupprimer = false;
        int nouvelleLigne = -1;

        if (ligne >= 0) {
            ligneSurvol = ligne;
        }

        if (ligne >= 0 && colonne == COL_ACTIONS) {
            Rectangle cellule = table.getCellRect(ligne, colonne, true);
            int x = e.getX() - cellule.x;
            int supprimerDroite = cellule.width - MARGE_DROITE_ACTIONS;
            int supprimerGauche = supprimerDroite - COTE_ACTION;
            int modifierDroite = supprimerGauche - ECART_ACTIONS;
            int modifierGauche = modifierDroite - COTE_ACTION;
            if (x >= supprimerGauche && x < supprimerDroite) {
                nouveauSupprimer = true;
                nouvelleLigne = ligne;
            } else if (x >= modifierGauche && x < modifierDroite) {
                nouveauModifier = true;
                nouvelleLigne = ligne;
            }
        }

        if (nouvelleLigne != ligneSurvolee
                || nouveauModifier != survolModifier
                || nouveauSupprimer != survolSupprimer) {
            ligneSurvolee = nouvelleLigne;
            survolModifier = nouveauModifier;
            survolSupprimer = nouveauSupprimer;
            table.repaint();
        }
    }

    private void reinitialiserSurvol() {
        if (ligneSurvolee != -1 || survolModifier || survolSupprimer || ligneSurvol != -1) {
            ligneSurvolee = -1;
            survolModifier = false;
            survolSupprimer = false;
            ligneSurvol = -1;
            table.repaint();
        }
    }

    // ---------------------------------------------------------------------
    // OUVERTURE / FERMETURE DE LA CARTE FORMULAIRE
    // ---------------------------------------------------------------------
    /** Ouvre la carte en mode ajout (champs vides, bouton "Ajouter"). */
    private void ouvrirAjout() {
        modeEdition = false;
        carteOuverte = true;
        viderChamps();
        table.clearSelection();
        zoneFormulaire.setVisible(true);
        reconstruirePied();
        zoneFormulaire.revalidate();
        zoneFormulaire.repaint();
        txtNom.requestFocusInWindow();
    }

    /** Active le mode edition apres selection d'une ligne. */
    private void entrerModeEdition() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            return;
        }
        modeEdition = true;
        carteOuverte = true;
        zoneFormulaire.setVisible(true);
        reconstruirePied();
        zoneFormulaire.revalidate();
        zoneFormulaire.repaint();
    }

    /** Ferme la carte et vide le formulaire. */
    private void fermerCarte() {
        modeEdition = false;
        carteOuverte = false;
        viderChamps();
        table.clearSelection();
        zoneFormulaire.setVisible(false);
        reconstruirePied();
        zoneFormulaire.revalidate();
        zoneFormulaire.repaint();
    }

    /** Quitte le mode edition et ferme la carte (bouton "Annuler"). */
    private void annulerEdition() {
        fermerCarte();
    }

    /** Enregistre la modification puis ferme la carte en cas de succes. */
    private void enregistrerModification() {
        boolean valide = table.getSelectedRow() >= 0
                && !txtNom.getText().trim().isEmpty()
                && !txtPrenom.getText().trim().isEmpty()
                && !txtTelephone.getText().trim().isEmpty();
        modifier();
        if (valide) {
            fermerCarte();
        }
    }

    // ---------------------------------------------------------------------
    // Operations CRUD (inchangées)
    // ---------------------------------------------------------------------
    private void ajouter() {
        if (!champsValides()) {
            return;
        }
        Proprietaire p = new Proprietaire(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                txtTelephone.getText().trim());
        dao.ajouter(p);
        JOptionPane.showMessageDialog(this, "Proprietaire ajoute avec succes.");
        viderChamps();
        actualiser();
        fermerCarte();
    }

    private void modifier() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un proprietaire dans la table.");
            return;
        }
        if (!champsValides()) {
            return;
        }
        Proprietaire p = new Proprietaire(
                txtNom.getText().trim(),
                txtPrenom.getText().trim(),
                txtTelephone.getText().trim());
        p.setIdProprietaire((int) modeleTable.getValueAt(ligne, 0));
        dao.modifier(p);
        JOptionPane.showMessageDialog(this, "Proprietaire modifie avec succes.");
        actualiser();
    }

    private void supprimer() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Selectionnez un proprietaire dans la table.");
            return;
        }
        int id = (int) modeleTable.getValueAt(ligne, 0);
        int choix = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer ce proprietaire ?",
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
        chargerDonnees(dao.lister());
    }

    private void chargerDonnees(List<Proprietaire> liste) {
        modeleTable.setRowCount(0);
        for (Proprietaire p : liste) {
            // 5e valeur vide : la colonne "Actions" est dessinee par le renderer.
            modeleTable.addRow(new Object[]{
                p.getIdProprietaire(), p.getNom(), p.getPrenom(), p.getTelephone(), ""
            });
        }
        // Compteur du modele : "N proprietaire(s)" (singulier/pluriel).
        lblCompteur.setText(liste.size() + (liste.size() > 1 ? " propriétaires" : " propriétaire"));
    }

    // ---------------------------------------------------------------------
    // Divers (inchangés)
    // ---------------------------------------------------------------------
    private boolean champsValides() {
        if (txtNom.getText().trim().isEmpty()
                || txtPrenom.getText().trim().isEmpty()
                || txtTelephone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs !");
            return false;
        }
        return true;
    }

    private void remplirFormulaire() {
        int ligne = table.getSelectedRow();
        if (ligne >= 0) {
            txtNom.setText(String.valueOf(modeleTable.getValueAt(ligne, 1)));
            txtPrenom.setText(String.valueOf(modeleTable.getValueAt(ligne, 2)));
            txtTelephone.setText(String.valueOf(modeleTable.getValueAt(ligne, 3)));
        }
    }

    private void viderChamps() {
        txtNom.setText("");
        txtPrenom.setText("");
        txtTelephone.setText("");
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

    /** Police utilisee partout : Segoe UI si disponible, sinon SansSerif. */
    private static Font police(int style, int taille) {
        return new Font(UTILISER_SEGOE ? "Segoe UI" : "SansSerif", style, taille);
    }

    /** Libelle de champ : gras, couleur sobre. */
    private static JLabel libelle(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 12));
        label.setForeground(LABEL);
        return label;
    }

    /** Style commun des champs de saisie (hauteur 32px, rayon 4px, focus). */
    private void styleChamp(JTextField champ) {
        champ.setOpaque(true);
        champ.setBackground(CARTE);
        champ.setForeground(TEXTE);
        champ.setCaretColor(PRIMAIRE);
        champ.setFont(police(Font.PLAIN, 13));
        champ.setPreferredSize(new Dimension(120, 32));
        champ.setBorder(bordureChamp(1, BORDURE_CHAMP));

        champ.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Epaisseur compensee pour eviter le saut de mise en page.
                champ.setBorder(bordureChamp(2, PRIMAIRE));
            }

            @Override
            public void focusLost(FocusEvent e) {
                champ.setBorder(bordureChamp(1, BORDURE_CHAMP));
            }
        });
    }

    /** Bordure du champ : ligne arrondie (4px) + marge interieure constante. */
    private static Border bordureChamp(int epaisseur, Color couleur) {
        int p = 6 - (epaisseur - 1);
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(couleur, 4, epaisseur, new Insets(1, 1, 1, 1)),
                BorderFactory.createEmptyBorder(p, 10, p, 10));
    }

    /** Bouton carre a icone pour la colonne "Actions". */
    private static BoutonStyle boutonAction(String texte, BoutonStyle.Type type) {
        boolean supprimer = texte != null && texte.toLowerCase().startsWith("supp");
        Icone.Type icone = supprimer ? Icone.Type.SUPPRIMER : Icone.Type.MODIFIER;
        Color teinte = supprimer ? DESTRUCTIF : PRIMAIRE;
        Color survol = supprimer ? SURVOL_SUPPRIMER : SURVOL_MODIFIER;
        return new BoutonStyle(icone, teinte, survol);
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS DE DESSIN
    // ---------------------------------------------------------------------
    /** Bordure arrondie et coloree, dessinee avec antialiasing. */
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
            g2.setStroke(new java.awt.BasicStroke(epaisseur));
            g2.drawRoundRect(x + epaisseur / 2, y + epaisseur / 2,
                    w - epaisseur - 1, h - epaisseur - 1, rayon * 2, rayon * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return espacement != null ? espacement : new Insets(0, 0, 0, 0);
        }
    }

    /** Petite pastille ronde coloree (point du compteur). */
    private static class PastilleDot extends JComponent {
        private final Color couleur;
        private final int diametre;

        PastilleDot(Color couleur, int diametre) {
            this.couleur = couleur;
            this.diametre = diametre;
            setPreferredSize(new Dimension(diametre, diametre));
            setMinimumSize(new Dimension(diametre, diametre));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(couleur);
            g2.fillOval(0, 0, diametre, diametre);
            g2.dispose();
        }
    }

    /** Bouton plat arrondi (texte, texte+icone ou icone seule). */
    private static class BoutonStyle extends JButton {

        enum Type { PRIMAIRE, SECONDAIRE, DESTRUCTIF, NEUTRE }

        private final Type type;
        private final Icone icone;
        private final boolean boutonIcone;
        private final Color survolIcone;

        BoutonStyle(String texte, Type type) {
            this(texte, type, 13, 36);
        }

        BoutonStyle(String texte, Type type, int taillePolice, int hauteur) {
            this(texte, type, taillePolice, hauteur, null);
        }

        BoutonStyle(String texte, Type type, int taillePolice, int hauteur,
                Icone.Type typeIcone) {
            super(texte);
            this.type = type;
            this.boutonIcone = false;
            this.survolIcone = null;
            this.icone = typeIcone != null ? new Icone(typeIcone, 18, Color.WHITE) : null;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, taillePolice));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            Dimension d = dimensionCompacte(texte, taillePolice, hauteur);
            if (icone != null) {
                d = new Dimension(d.width + icone.getIconWidth() + 8, hauteur);
            }
            setPreferredSize(d);
            setMaximumSize(d);
        }

        /** Bouton carre a icone seule (colonne "Actions"). */
        BoutonStyle(Icone.Type typeIcone, Color couleurIcone, Color couleurSurvol) {
            super("");
            this.type = Type.NEUTRE;
            this.boutonIcone = true;
            this.survolIcone = couleurSurvol;
            this.icone = new Icone(typeIcone, 18, couleurIcone);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            Dimension d = new Dimension(COTE_ACTION, COTE_ACTION);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        /** Dimension d'un bouton compact (marge horizontale de 12 px). */
        private static Dimension dimensionCompacte(String texte, int taillePolice, int hauteur) {
            Font font = police(Font.BOLD, taillePolice);
            Graphics2D g2 = new java.awt.image.BufferedImage(1, 1,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
            int largeur = g2.getFontMetrics(font).stringWidth(texte) + 24;
            g2.dispose();
            return new Dimension(largeur, hauteur);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            ButtonModel modele = getModel();

            if (boutonIcone) {
                // Boutons de ligne : rayon lg (4px) comme le modele.
                if (modele.isRollover() && modele.isEnabled()) {
                    g2.setColor(survolIcone);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                } else if ((modele.isPressed() || modele.isArmed()) && modele.isEnabled()) {
                    g2.setColor(BORDURE);
                    g2.fillRoundRect(0, 0, w, h, 8, 8);
                }
                icone.paintIcon(this, g2, (w - icone.getIconWidth()) / 2,
                        (h - icone.getIconHeight()) / 2);
                g2.dispose();
                return;
            }

            // Couleurs selon l'etat.
            Color fond;
            Color texte;
            if (!modele.isEnabled()) {
                fond = BORDURE;
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

            // Contour pour le style neutre.
            if (type == Type.NEUTRE) {
                g2.setColor(BORDURE_CHAMP);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            }

            // Icone optionnelle + texte.
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int largeurIcone = icone != null ? icone.getIconWidth() + 8 : 0;
            int total = largeurIcone + fm.stringWidth(getText());
            int x = (w - total) / 2;
            int yBase = (h + fm.getAscent() - fm.getDescent()) / 2;
            if (icone != null) {
                icone.paintIcon(this, g2, x, (h - icone.getIconHeight()) / 2);
                x += largeurIcone;
            }
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

    /** En-tete de tableau : hauteur 40px, label-sm (10px) majuscules, fond clair. */
    private static class EnteteRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setOpaque(true);
            label.setBackground(LIGNE_ALTERNEE);
            label.setForeground(TEXTE_SECONDAIRE);
            label.setFont(police(Font.BOLD, 10));
            label.setHorizontalAlignment(
                    c == COL_ACTIONS ? SwingConstants.RIGHT : SwingConstants.LEFT);
            String texte = label.getText();
            if (texte != null) {
                label.setText(texte.toUpperCase(Locale.FRENCH));
            }
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDURE),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)));
            return label;
        }
    }

    /** Cellule : colonnes typees, lignes alternees, survol de ligne, selection. */
    private class CelluleRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            // Typographies du modele : ID label-md, Nom headline-sm, telephone label-md.
            switch (c) {
                case 0:
                    label.setFont(police(Font.BOLD, 12));
                    label.setForeground(TEXTE_SECONDAIRE);
                    break;
                case 1:
                    label.setFont(police(Font.BOLD, 14));
                    label.setForeground(TEXTE);
                    break;
                case 3:
                    label.setFont(police(Font.BOLD, 12));
                    label.setForeground(TEXTE_SECONDAIRE);
                    break;
                default:
                    label.setFont(police(Font.PLAIN, 13));
                    label.setForeground(TEXTE);
                    break;
            }

            if (sel) {
                label.setBackground(SELECTION);
                label.setForeground(TEXTE);
            } else if (r == ligneSurvol) {
                label.setBackground(SURVOL_LIGNE);
                label.setForeground(TEXTE);
            } else if (r % 2 == 1) {
                label.setBackground(LIGNE_ALTERNEE);
                label.setForeground(TEXTE);
            } else {
                label.setBackground(CARTE);
                label.setForeground(TEXTE);
            }
            return label;
        }
    }

    /** Renderer de la colonne "Actions" : boutons Modifier / Supprimer par ligne. */
    private class ActionsRenderer extends JPanel implements TableCellRenderer {

        private final BoutonStyle boutonModifier = boutonAction("Modifier", BoutonStyle.Type.SECONDAIRE);
        private final BoutonStyle boutonSupprimer = boutonAction("Supprimer", BoutonStyle.Type.DESTRUCTIF);

        ActionsRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, MARGE_DROITE_ACTIONS));
            boutonModifier.setFocusable(false);
            boutonSupprimer.setFocusable(false);
            add(Box.createHorizontalGlue());
            add(boutonModifier);
            add(Box.createHorizontalStrut(ECART_ACTIONS));
            add(boutonSupprimer);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int r, int c) {
            if (sel) {
                setBackground(SELECTION);
            } else if (r == ligneSurvol) {
                setBackground(SURVOL_LIGNE);
            } else if (r % 2 == 1) {
                setBackground(LIGNE_ALTERNEE);
            } else {
                setBackground(CARTE);
            }
            // Etat de survol mémorisé par le MouseMotionListener de la table.
            boutonModifier.getModel().setRollover(r == ligneSurvolee && survolModifier);
            boutonSupprimer.getModel().setRollover(r == ligneSurvolee && survolSupprimer);
            return this;
        }
    }

    /** Editeur de la colonne "Actions" : boutons actifs qui declenchent le CRUD. */
    private class ActionsEditor extends AbstractCellEditor implements TableCellEditor {

        private final JPanel panneau = new JPanel();
        private final BoutonStyle boutonModifier = boutonAction("Modifier", BoutonStyle.Type.SECONDAIRE);
        private final BoutonStyle boutonSupprimer = boutonAction("Supprimer", BoutonStyle.Type.DESTRUCTIF);

        ActionsEditor() {
            panneau.setLayout(new BoxLayout(panneau, BoxLayout.X_AXIS));
            panneau.setOpaque(true);
            panneau.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, MARGE_DROITE_ACTIONS));
            boutonModifier.setFocusable(false);
            boutonSupprimer.setFocusable(false);
            boutonModifier.addActionListener(e -> actionModifier());
            boutonSupprimer.addActionListener(e -> actionSupprimer());
            panneau.add(Box.createHorizontalGlue());
            panneau.add(boutonModifier);
            panneau.add(Box.createHorizontalStrut(ECART_ACTIONS));
            panneau.add(boutonSupprimer);
        }

        private void actionModifier() {
            int ligne = table.getEditingRow();
            if (ligne >= 0) {
                table.setRowSelectionInterval(ligne, ligne);
            }
            entrerModeEdition();
            arreterEdition();
        }

        private void actionSupprimer() {
            int ligne = table.getEditingRow();
            if (ligne >= 0) {
                table.setRowSelectionInterval(ligne, ligne);
            }
            supprimer();
            arreterEdition();
        }

        private void arreterEdition() {
            SwingUtilities.invokeLater(this::fireEditingStopped);
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel,
                int r, int c) {
            if (sel) {
                panneau.setBackground(SELECTION);
            } else if (r == ligneSurvol) {
                panneau.setBackground(SURVOL_LIGNE);
            } else if (r % 2 == 1) {
                panneau.setBackground(LIGNE_ALTERNEE);
            } else {
                panneau.setBackground(CARTE);
            }
            return panneau;
        }
    }
}