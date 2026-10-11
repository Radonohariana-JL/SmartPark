package view;

import dao.ResultatOperation;
import dao.StationnementDAO;
import dao.TarifDAO;
import dao.VehiculeDAO;
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
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.Scrollable;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import model.Stationnement;
import model.Vehicule;
import util.Devise;
import util.NotificationCentre;

/**
 * Enregistre la sortie d'un vehicule : calcule la duree et le montant,
 * puis libere la place. Design clair reproduit depuis le modele de reference
 * "sortie_model1" (bandeau de page avec horodatage, carte des sessions actives,
 * deux cartes cote a cote Details / Recapitulatif, barre d'actions).
 * Le JComboBox<Stationnement> est conserve (decision utilisateur) : il remplace
 * la zone de recherche + le tableau du modele. La logique est inchangee.
 */
public class SortieForm extends JPanel {

    // ---------------------------------------------------------------------
    // PALETTE DE COULEURS (version claire)
    // ---------------------------------------------------------------------
    private static final Color FOND              = new Color(0xF1F5F9);
    private static final Color CARTE             = new Color(0xFFFFFF);
    private static final Color BANDE             = new Color(0xF8FAFC);
    private static final Color BORDURE           = new Color(0xE2E8F0);
    private static final Color PRIMAIRE          = new Color(0x2563EB);
    private static final Color PRIMAIRE_HOVER    = new Color(0x1D4ED8);
    private static final Color PRIMAIRE_PRESSED  = new Color(0x1E40AF);
    private static final Color SECONDAIRE        = new Color(0x64748B);
    private static final Color TEXTE             = new Color(0x0F172A);
    private static final Color TEXTE_SECONDAIRE  = new Color(0x64748B);
    private static final Color LABEL             = new Color(0x334155);
    private static final Color BORDURE_CHAMP     = new Color(0xCBD5E1);
    private static final Color SELECTION         = new Color(0xDBEAFE);
    private static final Color VERT              = new Color(0x16A34A);
    private static final Color VERT_FOND         = new Color(0xDCFCE7);
    private static final Color BLEU_FOND         = new Color(0xEFF6FF);
    private static final Color BLEU_BORDURE      = new Color(0xBFDBFE);

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HORLOGE = DateTimeFormatter.ofPattern("dd/MM/yyyy — HH:mm:ss");

    // ---------------------------------------------------------------------
    // CHAMPS OBJETS CONSERVES (JComboBox + JLabels + boutons)
    // ---------------------------------------------------------------------
    private final JComboBox<Stationnement> comboStationnement = new JComboBox<>();

    private final JLabel lblImmatriculation = new JLabel("-");
    private final JLabel lblProprietaire = new JLabel("-");
    private final JLabel lblPlace = new JLabel("-");
    private final JLabel lblType = new JLabel("-");
    private final JLabel lblDateEntree = new JLabel("-");
    private final JLabel lblDateSortie = new JLabel("-");
    private final JLabel lblDuree = new JLabel("-");
    private final JLabel lblMontant = new JLabel("-");

    // Champs supplementaires pour la reproduction fidele du modele.
    private final JLabel lblMarque = new JLabel("-");
    private final JLabel lblCouleur = new JLabel("-");
    private final JLabel lblModele = new JLabel("-");
    private final JLabel lblCategorie = new JLabel("-");
    private final JLabel lblHorloge = new JLabel("-");
    private final JLabel lblCompteur = new JLabel("0 Véhicules détectés");
    private final JLabel lblStatut = new JLabel("EN COURS");
    private final JLabel lblSession = new JLabel("SESSION");
    private final JLabel lblHeureEntree = new JLabel("-");
    private final JLabel lblHeureSortie = new JLabel("-");
    private final JLabel lblTarif = new JLabel("-");
    private final JLabel lblNoteTarif = new JLabel(" ");
    private final JLabel lblNoteCalcul = new JLabel(" ");
    private final JLabel lblInfo = new JLabel("Sélectionnez une session en cours.");

    private final JButton btnAnnuler = new BoutonStyle("Annuler", BoutonStyle.Type.NEUTRE);
    private final JButton btnEnregistrer = new BoutonStyle("Confirmer la sortie",
            BoutonStyle.Type.PRIMAIRE);
    private final JButton btnActualiser = new BoutonStyle("Actualiser", BoutonStyle.Type.NEUTRE);

    private final StationnementDAO stationnementDAO = new StationnementDAO();
    private final TarifDAO tarifDAO = new TarifDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    public SortieForm() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        add(creerFormulaire(), BorderLayout.CENTER);
        stylerValeurs();
        actualiser();
        demarrerHorloge();
    }

    // ---------------------------------------------------------------------
    // ZONE DEFILANTE + CONTENU
    // ---------------------------------------------------------------------
    private JPanel creerFormulaire() {
        PanneauDefilant fond = new PanneauDefilant(new GridBagLayout());
        fond.setOpaque(true);
        fond.setBackground(FOND);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(0, 0, 8, 0);
        fond.add(creerContenu(), g);

        JScrollPane defilement = new JScrollPane(fond,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defilement.setBorder(null);
        defilement.getViewport().setBackground(FOND);
        defilement.getVerticalScrollBar().setUnitIncrement(16);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setOpaque(false);
        racine.add(defilement, BorderLayout.CENTER);
        return racine;
    }

    private JPanel creerContenu() {
        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 16, 0);
        colonne.add(creerEnTete(), c);

        c.gridy = 1;
        colonne.add(creerCarteSessions(), c);

        c.gridy = 2;
        colonne.add(creerCorps(), c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        colonne.add(creerBarreActions(), c);
        return colonne;
    }

    /** En-tete de page : badges du poste a gauche, horodatage systeme a droite. */
    private JPanel creerEnTete() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel haut = new JPanel(new BorderLayout(12, 0));
        haut.setOpaque(false);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badges.setOpaque(false);
        JLabel poste = new JLabel("POSTE SORTIE // VOIE 02");
        poste.setFont(police(Font.BOLD, 11));
        poste.setForeground(LABEL);
        badges.add(new Chip(BANDE, poste));
        JLabel barriere = new JLabel("BARRIÈRE S-02 PRÊTE");
        barriere.setFont(police(Font.BOLD, 11));
        barriere.setForeground(VERT);
        JPanel droit = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        droit.setOpaque(false);
        droit.add(new PastilleDot(VERT, 8));
        droit.add(barriere);
        badges.add(new Chip(BANDE, droit));
        haut.add(badges, BorderLayout.WEST);

        haut.add(carteHorloge(), BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);
        panneau.add(haut, c);

        JLabel titre = new JLabel("Enregistrer une sortie");
        titre.setFont(police(Font.BOLD, 22));
        titre.setForeground(TEXTE);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        panneau.add(titre, c);

        JLabel sousTitre = new JLabel(
                "Consultez le stationnement en cours, calculez le montant et libérez "
                + "la place à la sortie du véhicule.");
        sousTitre.setFont(police(Font.PLAIN, 13));
        sousTitre.setForeground(TEXTE_SECONDAIRE);
        c.gridy = 2;
        c.insets = new Insets(4, 0, 0, 0);
        panneau.add(sousTitre, c);
        return panneau;
    }

    private JPanel carteHorloge() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(BANDE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        JLabel etiquette = new JLabel("HORODATAGE SYSTÈME");
        etiquette.setFont(police(Font.BOLD, 10));
        etiquette.setForeground(SECONDAIRE);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 2, 0);
        carte.add(etiquette, c);

        lblHorloge.setFont(police(Font.BOLD, 13));
        lblHorloge.setForeground(TEXTE);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        carte.add(lblHorloge, c);
        return carte;
    }

    // ---------------------------------------------------------------------
    // CARTE DES SESSIONS ACTIVES
    // ---------------------------------------------------------------------
    private JPanel creerCarteSessions() {
        JPanel corps = new JPanel(new GridBagLayout());
        corps.setOpaque(false);
        corps.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel entete = new JPanel(new BorderLayout(12, 0));
        entete.setOpaque(false);
        entete.add(blocTitre(Icone.Type.VEHICULE, "Recherche des sessions actives"),
                BorderLayout.WEST);
        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);
        droite.add(chipTexte(VERT_FOND, VERT, lblCompteur));
        droite.add(btnActualiser);
        entete.add(droite, BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        corps.add(entete, c);

        styleCombo(comboStationnement, "Sélectionner une session en cours...");
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        corps.add(comboStationnement, c);

        c.gridy = 2;
        c.insets = new Insets(6, 0, 0, 0);
        corps.add(aide("Le montant est recalculé automatiquement pour la session sélectionnée."), c);

        comboStationnement.addActionListener(e -> afficherDetail());
        btnActualiser.addActionListener(e -> actualiser());

        return carteBlanche(corps);
    }

    private JPanel carteBlanche(JPanel corps) {
        JPanel racine = new JPanel(new BorderLayout());
        racine.setOpaque(true);
        racine.setBackground(CARTE);
        racine.setBorder(new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)));
        racine.add(corps, BorderLayout.CENTER);
        return racine;
    }

    // ---------------------------------------------------------------------
    // CORPS : DEUX CARTES COTE A COTE
    // ---------------------------------------------------------------------
    private JPanel creerCorps() {
        JPanel corps = new JPanel(new GridBagLayout());
        corps.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weighty = 0;

        c.gridx = 0;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 0, 16);
        corps.add(creerCarteDetails(), c);

        c.gridx = 1;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 0, 0);
        corps.add(creerCarteRecap(), c);
        return corps;
    }

    private JPanel creerCarteDetails() {
        JPanel corps = new JPanel(new GridBagLayout());
        corps.setOpaque(false);
        corps.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        corps.add(blocPlaque(), c);

        c.gridy = 1;
        corps.add(grilleTiles(), c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        corps.add(blocChrono(), c);

        return carteAvecBande("Détails du stationnement sélectionné", chipStatut(), corps);
    }

    private JPanel carteAvecBande(String titre, JComponent est, JPanel corps) {
        JPanel racine = new JPanel(new BorderLayout());
        racine.setOpaque(true);
        racine.setBackground(CARTE);
        racine.setBorder(new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)));
        racine.add(creerBande(Icone.Type.PROPRIETAIRE, titre, est), BorderLayout.NORTH);
        racine.add(corps, BorderLayout.CENTER);
        return racine;
    }

    /** Grand bloc plaque : immatriculation + modele, place a droite. */
    private JPanel blocPlaque() {
        JPanel bloc = new JPanel(new BorderLayout(16, 0));
        bloc.setOpaque(true);
        bloc.setBackground(CARTE);
        bloc.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JPanel gauche = new JPanel(new GridBagLayout());
        gauche.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        lblImmatriculation.setFont(police(Font.BOLD, 18));
        lblImmatriculation.setForeground(TEXTE);
        g.gridy = 0;
        g.insets = new Insets(0, 0, 4, 0);
        gauche.add(new Chip(BANDE, lblImmatriculation), g);

        lblModele.setFont(police(Font.BOLD, 15));
        lblModele.setForeground(TEXTE);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 2, 0);
        gauche.add(lblModele, g);

        lblCategorie.setFont(police(Font.PLAIN, 12));
        lblCategorie.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 2;
        g.insets = new Insets(0, 0, 0, 0);
        gauche.add(lblCategorie, g);
        bloc.add(gauche, BorderLayout.WEST);

        JPanel droite = new JPanel(new GridBagLayout());
        droite.setOpaque(false);
        JLabel etiquette = new JLabel("PLACE");
        etiquette.setFont(police(Font.BOLD, 10));
        etiquette.setForeground(SECONDAIRE);
        g.gridy = 0;
        g.insets = new Insets(0, 0, 4, 0);
        droite.add(etiquette, g);

        lblPlace.setFont(police(Font.BOLD, 14));
        lblPlace.setForeground(PRIMAIRE);
        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        contenu.setOpaque(false);
        contenu.add(lblPlace);
        JLabel occupee = new JLabel("(Occupée)");
        occupee.setFont(police(Font.PLAIN, 11));
        occupee.setForeground(TEXTE_SECONDAIRE);
        contenu.add(occupee);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 0, 0);
        droite.add(new Chip(BANDE, contenu), g);
        bloc.add(droite, BorderLayout.EAST);
        return bloc;
    }

    /** Grille 2x2 de tuiles : marque, couleur, categorie, proprietaire. */
    private JPanel grilleTiles() {
        JPanel grille = new JPanel(new GridBagLayout());
        grille.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 8, 0);

        c.gridx = 0;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 8, 8);
        grille.add(tile("Marque & Modèle", lblMarque), c);
        c.gridx = 1;
        c.insets = new Insets(0, 8, 8, 0);
        grille.add(tile("Couleur Carrosserie", lblCouleur), c);
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(8, 0, 0, 8);
        grille.add(tile("Catégorie Tarifaire", lblType), c);
        c.gridx = 1;
        c.insets = new Insets(8, 8, 0, 0);
        grille.add(tile("Propriétaire Enregistré", lblProprietaire), c);
        return grille;
    }

    private JPanel tile(String titre, JLabel valeur) {
        JPanel tuile = new JPanel(new GridBagLayout());
        tuile.setOpaque(true);
        tuile.setBackground(BANDE);
        tuile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel t = new JLabel(titre);
        t.setFont(police(Font.BOLD, 10));
        t.setForeground(SECONDAIRE);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        tuile.add(t, c);

        valeur.setFont(police(Font.BOLD, 13));
        valeur.setForeground(TEXTE);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        tuile.add(valeur, c);
        return tuile;
    }

    /** Bloc chronologie horodatée : entrees / sortie auto. */
    private JPanel blocChrono() {
        JPanel bloc = new JPanel(new GridBagLayout());
        bloc.setOpaque(true);
        bloc.setBackground(CARTE);
        bloc.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel entrees = new JLabel("Chronologie Horodatée");
        entrees.setFont(police(Font.BOLD, 11));
        entrees.setForeground(LABEL);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);
        bloc.add(entrees, c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 6, 0);
        bloc.add(ligneChrono(Icone.Type.CADENAS, "Entrée", lblDateEntree), c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        bloc.add(ligneChrono(Icone.Type.CALENDRIER, "Sortie Auto", lblDateSortie), c);
        return bloc;
    }

    private JPanel ligneChrono(Icone.Type icone, String nom, JLabel valeur) {
        JPanel ligne = new JPanel(new BorderLayout(8, 0));
        ligne.setOpaque(true);
        ligne.setBackground(BANDE);
        ligne.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 6, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        gauche.setOpaque(false);
        gauche.add(new JLabel(new Icone(icone, 16, SECONDAIRE)));
        JLabel label = new JLabel(nom);
        label.setFont(police(Font.PLAIN, 12));
        label.setForeground(TEXTE_SECONDAIRE);
        gauche.add(label);
        ligne.add(gauche, BorderLayout.WEST);

        valeur.setFont(police(Font.BOLD, 13));
        valeur.setForeground(TEXTE);
        ligne.add(valeur, BorderLayout.EAST);
        return ligne;
    }

    // ---------------------------------------------------------------------
    // RECAPITULATIF
    // ---------------------------------------------------------------------
    private JPanel creerCarteRecap() {
        JPanel corps = new JPanel(new GridBagLayout());
        corps.setOpaque(false);
        corps.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        corps.add(blocLignes(), c);

        c.gridy = 1;
        corps.add(blocMontant(), c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        corps.add(blocModes(), c);

        lblSession.setFont(police(Font.BOLD, 11));
        lblSession.setForeground(SECONDAIRE);
        return carteAvecBande("Récapitulatif du stationnement",
                new Chip(BANDE, lblSession), corps);
    }

    /** Liste des lignes de calcul : entree, sortie, duree, tarif. */
    private JPanel blocLignes() {
        JPanel bloc = new JPanel(new GridBagLayout());
        bloc.setOpaque(true);
        bloc.setBackground(CARTE);
        bloc.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(4, 0, 4, 0);
        bloc.add(ligneRecap("Heure d'entrée enregistrée :", lblHeureEntree), c);

        c.gridy = 1;
        bloc.add(ligneRecap("Heure de sortie constatée :", lblHeureSortie), c);

        c.gridy = 2;
        bloc.add(ligneRecapPill("Durée totale calculée :", lblDuree), c);

        c.gridy = 3;
        bloc.add(ligneRecapTarif(), c);
        return bloc;
    }

    private JPanel ligneRecap(String nom, JLabel valeur) {
        JPanel ligne = new JPanel(new BorderLayout(12, 0));
        ligne.setOpaque(false);
        JLabel gauche = new JLabel(nom);
        gauche.setFont(police(Font.PLAIN, 12));
        gauche.setForeground(TEXTE_SECONDAIRE);
        ligne.add(gauche, BorderLayout.WEST);
        valeur.setFont(police(Font.BOLD, 13));
        valeur.setForeground(TEXTE);
        ligne.add(valeur, BorderLayout.EAST);
        return ligne;
    }

    private JPanel ligneRecapPill(String nom, JLabel valeur) {
        JPanel ligne = new JPanel(new BorderLayout(12, 0));
        ligne.setOpaque(false);
        JLabel gauche = new JLabel(nom);
        gauche.setFont(police(Font.PLAIN, 12));
        gauche.setForeground(TEXTE_SECONDAIRE);
        ligne.add(gauche, BorderLayout.WEST);
        valeur.setFont(police(Font.BOLD, 13));
        valeur.setForeground(TEXTE);
        ligne.add(new Chip(BANDE, valeur), BorderLayout.EAST);
        return ligne;
    }

    private JPanel ligneRecapTarif() {
        JPanel ligne = new JPanel(new BorderLayout(12, 0));
        ligne.setOpaque(false);

        JPanel gauche = new JPanel(new GridBagLayout());
        gauche.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JLabel nom = new JLabel("Tarif horaire appliqué :");
        nom.setFont(police(Font.PLAIN, 12));
        nom.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 0;
        gauche.add(nom, g);
        lblNoteTarif.setFont(police(Font.PLAIN, 11));
        lblNoteTarif.setForeground(SECONDAIRE);
        g.gridy = 1;
        gauche.add(lblNoteTarif, g);
        ligne.add(gauche, BorderLayout.WEST);

        lblTarif.setFont(police(Font.BOLD, 13));
        lblTarif.setForeground(TEXTE);
        ligne.add(lblTarif, BorderLayout.EAST);
        return ligne;
    }

    /** Grand bloc du montant total a payer. */
    private JPanel blocMontant() {
        JPanel bloc = new JPanel(new GridBagLayout());
        bloc.setOpaque(true);
        bloc.setBackground(BLEU_FOND);
        bloc.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BLEU_BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(20, 16, 20, 16)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        JLabel etiquette = new JLabel("MONTANT TOTAL À PAYER");
        etiquette.setFont(police(Font.BOLD, 10));
        etiquette.setForeground(SECONDAIRE);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        bloc.add(etiquette, c);

        lblMontant.setFont(police(Font.BOLD, 34));
        lblMontant.setForeground(PRIMAIRE);
        lblMontant.setHorizontalAlignment(JLabel.CENTER);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 4, 0);
        bloc.add(lblMontant, c);

        lblNoteCalcul.setFont(police(Font.PLAIN, 11));
        lblNoteCalcul.setForeground(TEXTE_SECONDAIRE);
        lblNoteCalcul.setHorizontalAlignment(JLabel.CENTER);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        bloc.add(lblNoteCalcul, c);
        return bloc;
    }

    /** Deux tuiles : mode de reglement et liberation. */
    private JPanel blocModes() {
        JPanel grille = new JPanel(new GridBagLayout());
        grille.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, 0, 8);
        grille.add(modeTile("Mode de règlement", "Espèces / Guichet", TEXTE), c);

        c.gridx = 1;
        c.insets = new Insets(0, 8, 0, 0);
        grille.add(modeTile("Libération", "Immédiate", VERT), c);
        return grille;
    }

    private JPanel modeTile(String titre, String valeur, Color couleurValeur) {
        JPanel tuile = new JPanel(new GridBagLayout());
        tuile.setOpaque(true);
        tuile.setBackground(BANDE);
        tuile.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel t = new JLabel(titre);
        t.setFont(police(Font.BOLD, 10));
        t.setForeground(SECONDAIRE);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        tuile.add(t, c);

        JLabel v = new JLabel(valeur);
        v.setFont(police(Font.BOLD, 13));
        v.setForeground(couleurValeur);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        tuile.add(v, c);
        return tuile;
    }

    // ---------------------------------------------------------------------
    // BARRE D'ACTIONS
    // ---------------------------------------------------------------------
    private JPanel creerBarreActions() {
        JPanel barre = new JPanel(new BorderLayout(12, 0));
        barre.setOpaque(true);
        barre.setBackground(CARTE);
        barre.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        lblInfo.setFont(police(Font.PLAIN, 13));
        lblInfo.setForeground(TEXTE_SECONDAIRE);
        barre.add(lblInfo, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);
        btnAnnuler.addActionListener(e -> annuler());
        btnEnregistrer.addActionListener(e -> enregistrerSortie());
        droite.add(btnAnnuler);
        droite.add(btnEnregistrer);
        barre.add(droite, BorderLayout.EAST);
        return barre;
    }

    // ---------------------------------------------------------------------
    // FABRIQUES D'ELEMENTS
    // ---------------------------------------------------------------------
    private JPanel blocTitre(Icone.Type icone, String titre) {
        JPanel pan = new JPanel(new BorderLayout(12, 0));
        pan.setOpaque(false);
        pan.add(new CarreIcone(icone, 28, SELECTION, PRIMAIRE), BorderLayout.WEST);

        JPanel titres = new JPanel(new GridBagLayout());
        titres.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JLabel t = new JLabel(titre);
        t.setFont(police(Font.BOLD, 16));
        t.setForeground(TEXTE);
        g.gridy = 0;
        titres.add(t, g);
        pan.add(titres, BorderLayout.CENTER);
        return pan;
    }

    private JPanel creerBande(Icone.Type icone, String titre, JComponent est) {
        Bande bande = new Bande(true, false);
        bande.add(blocTitre(icone, titre), BorderLayout.WEST);
        if (est != null) {
            bande.add(est, BorderLayout.EAST);
        }
        return bande;
    }

    private Chip chipTexte(Color fond, Color couleur, JLabel source) {
        source.setFont(police(Font.BOLD, 11));
        source.setForeground(couleur);
        source.setOpaque(false);
        source.setHorizontalAlignment(JLabel.LEFT);
        return new Chip(fond, source);
    }

    private Chip chipStatut() {
        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        contenu.setOpaque(false);
        contenu.add(new PastilleDot(VERT, 8));
        lblStatut.setFont(police(Font.BOLD, 11));
        lblStatut.setForeground(VERT);
        contenu.add(lblStatut);
        return new Chip(BANDE, contenu);
    }

    private static JLabel aide(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.PLAIN, 11));
        label.setForeground(TEXTE_SECONDAIRE);
        return label;
    }

    private void demarrerHorloge() {
        lblHorloge.setText(LocalDateTime.now().format(HORLOGE));
        javax.swing.Timer minuteur = new javax.swing.Timer(1000, e ->
                lblHorloge.setText(LocalDateTime.now().format(HORLOGE)));
        minuteur.start();
    }

    private static String valeur(String texte) {
        return (texte == null || texte.trim().isEmpty()) ? "-" : texte.trim();
    }

    private static String nombre(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v)) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    private static String formaterDuree(double heures) {
        int h = (int) heures;
        int m = (int) Math.round((heures - h) * 60);
        if (m == 60) {
            h++;
            m = 0;
        }
        if (h == 0) {
            return m + " min";
        }
        if (m == 0) {
            return h + " h";
        }
        return h + " h " + m + " min";
    }

    // ---------------------------------------------------------------------
    // LOGIQUE INCHANGEE
    // ---------------------------------------------------------------------
    private void actualiser() {
        comboStationnement.removeAllItems();
        for (Stationnement s : stationnementDAO.listerEnCours()) {
            s.setMontant(calculeMontant(s));
            comboStationnement.addItem(s);
        }
        lblCompteur.setText(comboStationnement.getItemCount() + " Véhicules détectés");
        afficherDetail();
    }

    /**
     * Recharge la liste des stationnements en cours depuis la base. Appele par
     * le Dashboard a l'ouverture de la page afin que les entrees enregistrees
     * ailleurs apparaissent immediatement.
     */
    public void rafraichir() {
        actualiser();
    }

    private Stationnement getSelection() {
        return (Stationnement) comboStationnement.getSelectedItem();
    }

    private double calculeDuree(Stationnement s) {
        double duree = ChronoUnit.MINUTES.between(s.getDateEntree(), LocalDateTime.now()) / 60.0;
        if (duree < 1) {
            duree = 1;
        }
        return Math.round(duree * 100.0) / 100.0;
    }

    private double calculeMontant(Stationnement s) {
        double prix = tarifDAO.getPrixHeure(s.getTypeVehicule());
        return Math.round(calculeDuree(s) * prix * 100.0) / 100.0;
    }

    /** Recherche (lecture seule) des caracteristiques du vehicule pour l'affichage. */
    private Vehicule chercherVehicule(String immatriculation) {
        for (Vehicule v : vehiculeDAO.rechercher(immatriculation)) {
            return v;
        }
        return null;
    }

    private void afficherDetail() {
        Stationnement s = getSelection();
        if (s == null) {
            lblImmatriculation.setText("-");
            lblProprietaire.setText("-");
            lblPlace.setText("-");
            lblType.setText("-");
            lblDateEntree.setText("-");
            lblDateSortie.setText("-");
            lblDuree.setText("-");
            lblMontant.setText("-");
            lblMarque.setText("-");
            lblCouleur.setText("-");
            lblModele.setText("-");
            lblCategorie.setText("-");
            lblHeureEntree.setText("-");
            lblHeureSortie.setText("-");
            lblTarif.setText("-");
            lblNoteTarif.setText(" ");
            lblNoteCalcul.setText(" ");
            lblStatut.setText("—");
            lblStatut.setForeground(TEXTE_SECONDAIRE);
            lblSession.setText("SESSION");
            lblInfo.setText("Sélectionnez une session en cours.");
            btnEnregistrer.setEnabled(false);
            return;
        }

        btnEnregistrer.setEnabled(true);
        lblImmatriculation.setText(valeur(s.getImmatriculation()));
        lblProprietaire.setText(valeur(s.getNomProprietaire()));
        lblPlace.setText(valeur(s.getNumeroPlace()));
        lblType.setText(valeur(s.getTypeVehicule()));

        Vehicule vehicule = chercherVehicule(s.getImmatriculation());
        String marque = vehicule != null ? valeur(vehicule.getMarque()) : "-";
        String couleur = vehicule != null ? valeur(vehicule.getCouleur()) : "-";
        lblModele.setText(marque);
        lblCategorie.setText(valeur(s.getTypeVehicule()));
        lblMarque.setText(marque);
        lblCouleur.setText(couleur);

        String formatEntree = s.getDateEntree() != null
                ? s.getDateEntree().format(FORMAT) : "-";
        lblDateEntree.setText(formatEntree);
        lblDateSortie.setText(LocalDateTime.now().format(FORMAT));
        lblHeureEntree.setText(s.getDateEntree() != null
                ? s.getDateEntree().format(HEURE) : "-");
        lblHeureSortie.setText(LocalDateTime.now().format(HEURE));

        double duree = calculeDuree(s);
        double prix = tarifDAO.getPrixHeure(s.getTypeVehicule());
        lblDuree.setText(formaterDuree(duree));
        lblTarif.setText(Devise.formater(prix) + " / h");
        lblNoteTarif.setText("Tarif " + valeur(s.getTypeVehicule()));
        lblMontant.setText(Devise.formater(s.getMontant()));
        lblNoteCalcul.setText("Calculé automatiquement ("
                + formaterDuree(duree) + " @ " + Devise.formater(prix) + "/h)");

        lblStatut.setText("EN COURS");
        lblStatut.setForeground(VERT);
        lblSession.setText("SESSION #ST-" + s.getIdStationnement());
        lblInfo.setText("La place " + valeur(s.getNumeroPlace())
                + " sera automatiquement libérée et réattribuée dès confirmation.");
    }

    private void annuler() {
        comboStationnement.setSelectedIndex(-1);
        afficherDetail();
    }

    private void enregistrerSortie() {
        Stationnement s = getSelection();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Aucun vehicule stationne.");
            return;
        }

        double prix = tarifDAO.getPrixHeure(s.getTypeVehicule());
        ResultatOperation res = stationnementDAO.terminer(s, prix);

        if (res.estSucces()) {
            NotificationCentre.sortie(s.getImmatriculation(), res.getMontant());
            JOptionPane.showMessageDialog(this,
                    "Sortie enregistree : " + s.getImmatriculation()
                            + " - montant " + Devise.formater(res.getMontant()) + ".");
            actualiser();
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage());
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

    private void stylerValeurs() {
        Font f = police(Font.BOLD, 13);
        JLabel[] valeurs = {lblProprietaire, lblPlace, lblType,
                lblDateEntree, lblDateSortie, lblDuree,
                lblMarque, lblCouleur, lblHeureEntree, lblHeureSortie, lblTarif};
        for (JLabel l : valeurs) {
            l.setFont(f);
            l.setForeground(TEXTE);
        }
    }

    private <T> void styleCombo(JComboBox<T> combo, String placeholder) {
        combo.setOpaque(true);
        combo.setBackground(CARTE);
        combo.setForeground(TEXTE);
        combo.setFont(police(Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(120, 36));
        combo.setMinimumSize(new Dimension(60, 36));
        combo.setBorder(BorderFactory.createLineBorder(BORDURE_CHAMP));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new ComboRenderer(placeholder));
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS DE DESSIN
    // ---------------------------------------------------------------------
    /** Pastille arrondie (pills) contenant un composant. */
    private static class Chip extends JComponent {
        private final Color fond;

        Chip(Color fond, JComponent contenu) {
            this.fond = fond;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            add(contenu, BorderLayout.CENTER);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fond);
            int h = getHeight();
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Bandeau a coins arrondis (haut ou bas) avec trait de separation. */
    private static class Bande extends JPanel {
        private final boolean coinsHaut;
        private final boolean traitEnBas;
        private static final int RAYON = 10;

        Bande(boolean coinsHaut, boolean traitEnBas) {
            super(new BorderLayout(12, 0));
            this.coinsHaut = coinsHaut;
            this.traitEnBas = traitEnBas;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            double arc = RAYON * 2.0;
            RoundRectangle2D forme = coinsHaut
                    ? new RoundRectangle2D.Double(0, 0, w, h + arc, arc, arc)
                    : new RoundRectangle2D.Double(0, -arc, w, h + arc, arc, arc);
            g2.setColor(BANDE);
            g2.fill(forme);
            g2.setColor(BORDURE);
            if (traitEnBas) {
                g2.drawLine(0, 0, w, 0);
            } else {
                g2.drawLine(0, h - 1, w, h - 1);
            }
            g2.dispose();
        }
    }

    /** Petite pastille ronde coloree. */
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
            g2.fill(new Ellipse2D.Double(0, 0, diametre, diametre));
            g2.dispose();
        }
    }

    /** Carre arrondi contenant une icone centree. */
    private static class CarreIcone extends JComponent {
        private final Icone icone;
        private final int cote;
        private final Color fond;

        CarreIcone(Icone.Type type, int cote, Color fond, Color couleurIcone) {
            this.icone = new Icone(type, Math.max(1, cote - 12), couleurIcone);
            this.cote = cote;
            this.fond = fond;
            setPreferredSize(new Dimension(cote, cote));
            setMinimumSize(new Dimension(cote, cote));
            setMaximumSize(new Dimension(cote, cote));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fond);
            g2.fillRoundRect(0, 0, cote, cote, 8, 8);
            icone.paintIcon(this, g2, (cote - icone.getIconWidth()) / 2,
                    (cote - icone.getIconHeight()) / 2);
            g2.dispose();
        }
    }

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

    private static class BoutonStyle extends JButton {
        enum Type { PRIMAIRE, NEUTRE }

        private final Type type;
        private final int taillePolice;

        BoutonStyle(String texte, Type type) {
            super(texte);
            this.type = type;
            this.taillePolice = 13;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, taillePolice));

            FontMetrics fm = getFontMetrics(getFont());
            int largeur = fm.stringWidth(texte) + 48;
            setPreferredSize(new Dimension(largeur, 36));
            setMinimumSize(new Dimension(largeur, 36));
            setMaximumSize(new Dimension(largeur, 36));
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
                fond = type == Type.PRIMAIRE ? PRIMAIRE_PRESSED : BORDURE;
                texte = type == Type.NEUTRE ? LABEL : Color.WHITE;
            } else if (modele.isRollover()) {
                fond = type == Type.PRIMAIRE ? PRIMAIRE_HOVER : FOND;
                texte = type == Type.NEUTRE ? LABEL : Color.WHITE;
            } else {
                fond = type == Type.PRIMAIRE ? PRIMAIRE : CARTE;
                texte = type == Type.NEUTRE ? LABEL : Color.WHITE;
            }

            g2.setColor(fond);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            if (type == Type.NEUTRE && modele.isEnabled()) {
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
    }

    private static class ComboRenderer implements ListCellRenderer<Object> {
        private final javax.swing.DefaultListCellRenderer base =
                new javax.swing.DefaultListCellRenderer();
        private final String placeholder;

        ComboRenderer(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) base.getListCellRendererComponent(
                    list, value, index, selected, focus);
            if (value == null) {
                label.setText(placeholder);
                label.setForeground(TEXTE_SECONDAIRE);
            } else if (value instanceof Stationnement) {
                Stationnement s = (Stationnement) value;
                label.setText(s.getImmatriculation() + " — Place " + s.getNumeroPlace()
                        + " (" + s.getTypeVehicule() + ", En cours)");
                label.setForeground(TEXTE);
            } else {
                label.setText(value.toString());
                label.setForeground(TEXTE);
            }
            label.setFont(police(Font.PLAIN, 13));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            label.setBackground(selected ? SELECTION : CARTE);
            return label;
        }
    }

    /** Panneau pour JScrollPane : suit la largeur, remplit la hauteur si possible. */
    private static class PanneauDefilant extends JPanel implements Scrollable {
        PanneauDefilant(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return Math.max(16, r.height - 32);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport
                    && getParent().getHeight() > getPreferredSize().height;
        }
    }
}