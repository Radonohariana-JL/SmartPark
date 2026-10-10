package view;

import dao.PlaceDAO;
import dao.StationnementDAO;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import model.Place;
import model.Stationnement;
import model.Vehicule;
import util.DateUtils;

/**
 * Enregistre l'entree d'un vehicule dans une place libre.
 * Cree un stationnement EN_COURS et met la place en OCCUPEE.
 * Design clair reproduit depuis le modele de reference "entree_model" :
 * bandeau de page, carte "ticket" (en-tete, corps a 2 colonnes, pied d'actions).
 * Les objets JComboBox (comboVehicule, comboPlace) et la logique restent inchanges.
 */
public class EntreeForm extends JPanel {

    private final JComboBox<Vehicule> comboVehicule = new JComboBox<>();
    private final JComboBox<Place> comboPlace = new JComboBox<>();
    private final JLabel lblHorodatage = new JLabel();
    private final JButton btnEnregistrer = new BoutonStyle("Enregistrer l'entrée",
            BoutonStyle.Type.PRIMAIRE);
    private final JButton btnAnnuler = new BoutonStyle("Annuler", BoutonStyle.Type.NEUTRE);

    // Panneaux d'information (Details du vehicule / Emplacement selectionne).
    private final JLabel lblVehEtat = new JLabel();
    private final JLabel lblVehImmat = new JLabel("-");
    private final JLabel lblVehModele = new JLabel("-");
    private final JLabel lblVehTitulaire = new JLabel("-");
    private final JLabel lblVehType = new JLabel("-");
    private final JPanel panneauVehVide = new JPanel(new BorderLayout());
    private final JPanel panneauVehDetail = new JPanel(new GridBagLayout());
    private final JLabel lblPlaceNum = new JLabel("Aucune place attribuée");
    private final JLabel lblPlaceEtat = new JLabel("EN ATTENTE");
    private final JLabel lblDispo = new JLabel("0 DISPONIBLES");

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final PlaceDAO placeDAO = new PlaceDAO();
    private final StationnementDAO stationnementDAO = new StationnementDAO();

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
    private static final Color PILL_FOND         = new Color(0xF1F5F9);

    private static final int GRANDE_LARGEUR = 4000;

    public EntreeForm() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);
        add(creerFormulaire(), BorderLayout.CENTER);
        demarrerHorloge();
        actualiser();
        installerRaccourcis();
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
        c.insets = new Insets(0, 0, 0, 0);
        colonne.add(creerCarteTicket(), c);
        return colonne;
    }

    /** En-tete de page : ligne de statut + titre + sous-titre. */
    private JPanel creerEnTete() {
        JPanel panneau = new JPanel(new GridBagLayout());
        panneau.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ligne.setOpaque(false);
        JLabel poste = new JLabel("POSTE ACCÈS // VOIE 01");
        poste.setFont(police(Font.BOLD, 11));
        poste.setForeground(SECONDAIRE);
        ligne.add(poste);
        ligne.add(new PastilleDot(VERT, 8));
        JLabel statut = new JLabel("STATUT: PRÊT");
        statut.setFont(police(Font.PLAIN, 11));
        statut.setForeground(TEXTE_SECONDAIRE);
        ligne.add(statut);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        panneau.add(ligne, c);

        JLabel titre = new JLabel("Enregistrer une entrée");
        titre.setFont(police(Font.BOLD, 22));
        titre.setForeground(TEXTE);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        panneau.add(titre, c);

        JLabel sousTitre = new JLabel(
                "Enregistrez l'arrivée d'un véhicule et attribuez-lui une place disponible.");
        sousTitre.setFont(police(Font.PLAIN, 13));
        sousTitre.setForeground(TEXTE_SECONDAIRE);
        c.gridy = 2;
        c.insets = new Insets(4, 0, 0, 0);
        panneau.add(sousTitre, c);
        return panneau;
    }

    // ---------------------------------------------------------------------
    // CARTE TICKET
    // ---------------------------------------------------------------------
    private JPanel creerCarteTicket() {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(new RoundedBorder(BORDURE, 10, 1, new Insets(0, 0, 0, 0)));
        carte.setMaximumSize(new Dimension(GRANDE_LARGEUR, 32767));

        carte.add(creerEnteteCarte(), BorderLayout.NORTH);
        carte.add(creerCorpsCarte(), BorderLayout.CENTER);
        carte.add(creerPiedCarte(), BorderLayout.SOUTH);
        return carte;
    }

    /** Bandeau d'en-tete de la carte : titre du ticket + etat de la boucle sol. */
    private JPanel creerEnteteCarte() {
        Bande entete = new Bande(true, false);

        JPanel gauche = new JPanel(new BorderLayout(12, 0));
        gauche.setOpaque(false);
        gauche.add(new CarreIcone(Icone.Type.ENTREE, 28, SELECTION, PRIMAIRE), BorderLayout.WEST);

        JPanel titres = new JPanel(new GridBagLayout());
        titres.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        JLabel t = new JLabel("Nouveau ticket d'entrée");
        t.setFont(police(Font.BOLD, 16));
        t.setForeground(TEXTE);
        c.gridy = 0;
        titres.add(t, c);
        JLabel s = new JLabel("FORMULAIRE OPERATEUR #TK-ENT-2023-882");
        s.setFont(police(Font.PLAIN, 11));
        s.setForeground(TEXTE_SECONDAIRE);
        c.gridy = 1;
        titres.add(s, c);
        gauche.add(titres, BorderLayout.CENTER);
        entete.add(gauche, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        droite.setOpaque(false);
        droite.add(new PastilleDot(VERT, 8));
        JLabel boucle = new JLabel("Boucle sol:");
        boucle.setFont(police(Font.PLAIN, 11));
        boucle.setForeground(TEXTE_SECONDAIRE);
        droite.add(boucle);
        JLabel detectee = new JLabel("DÉTECTÉE");
        detectee.setFont(police(Font.BOLD, 11));
        detectee.setForeground(VERT);
        droite.add(detectee);
        entete.add(droite, BorderLayout.EAST);

        return entete;
    }

    /** Corps : deux colonnes (champs a gauche, information a droite). */
    private JPanel creerCorpsCarte() {
        JPanel corps = new JPanel(new GridBagLayout());
        corps.setOpaque(false);
        corps.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 0, 16);
        corps.add(creerColonneGauche(), c);

        c.gridx = 1;
        c.weightx = 0.42;
        c.insets = new Insets(0, 0, 0, 0);
        corps.add(creerColonneDroite(), c);
        return corps;
    }

    private JPanel creerColonneGauche() {
        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 16, 0);
        colonne.add(champVehicule(), c);

        c.gridy = 1;
        colonne.add(champPlace(), c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        colonne.add(champDate(), c);
        return colonne;
    }

    private JPanel champVehicule() {
        styleComboVehicule(comboVehicule);
        JPanel groupe = new JPanel(new GridBagLayout());
        groupe.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 6, 0);
        groupe.add(ligneLibelle("Véhicule (Immatriculation / Modèle)", Icone.Type.VEHICULE,
                "REQUIS [JComboBox]"), c);
        c.gridy = 1;
        groupe.add(comboVehicule, c);
        c.gridy = 2;
        c.insets = new Insets(6, 0, 0, 0);
        groupe.add(aide("Seuls les véhicules enregistrés dans la base apparaissent ici."), c);
        return groupe;
    }

    private JPanel champPlace() {
        styleComboPlace(comboPlace);
        JPanel groupe = new JPanel(new GridBagLayout());
        groupe.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.add(libelleIcone("Place de parking", Icone.Type.PARKING, VERT), BorderLayout.WEST);
        Pill dispo = new Pill(VERT_FOND, VERT);
        dispo.setTexte(lblDispo);
        ligne.add(dispo, BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 6, 0);
        groupe.add(ligne, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        groupe.add(comboPlace, c);
        c.gridy = 2;
        c.insets = new Insets(6, 0, 0, 0);
        groupe.add(aide("Attribution selon le gabarit et abonnement du véhicule."), c);
        return groupe;
    }

    private JPanel champDate() {
        JPanel groupe = new JPanel(new GridBagLayout());
        groupe.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.add(libelleIcone("Date et heure d'entrée", Icone.Type.CALENDRIER, TEXTE_SECONDAIRE),
                BorderLayout.WEST);
        JLabel horloge = new JLabel("HORLOGE SYSTÈME NTP");
        horloge.setFont(police(Font.PLAIN, 11));
        horloge.setForeground(TEXTE_SECONDAIRE);
        ligne.add(horloge, BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 6, 0);
        groupe.add(ligne, c);

        JPanel pastille = new JPanel(new BorderLayout(8, 0));
        pastille.setOpaque(true);
        pastille.setBackground(BANDE);
        pastille.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel cadenas = new JLabel(new Icone(Icone.Type.CADENAS, 16, TEXTE_SECONDAIRE));
        pastille.add(cadenas, BorderLayout.WEST);
        lblHorodatage.setFont(police(Font.BOLD, 14));
        lblHorodatage.setForeground(TEXTE);
        pastille.add(lblHorodatage, BorderLayout.CENTER);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        groupe.add(pastille, c);
        c.gridy = 2;
        c.insets = new Insets(6, 0, 0, 0);
        groupe.add(aide("Timestamp certifié non modifiable manuellement."), c);
        return groupe;
    }

    private JPanel creerColonneDroite() {
        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        colonne.add(carteDetailsVehicule(), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        colonne.add(carteEmplacement(), c);
        return colonne;
    }

    private JPanel carteDetailsVehicule() {
        JPanel carte = sousCarte();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(libelleSection("DÉTAILS DU VÉHICULE", Icone.Type.VEHICULE), BorderLayout.WEST);
        lblVehEtat.setFont(police(Font.BOLD, 11));
        lblVehEtat.setForeground(TEXTE_SECONDAIRE);
        entete.add(lblVehEtat, BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);
        carte.add(entete, c);

        // Etat vide.
        panneauVehVide.setOpaque(false);
        JPanel videBloc = new JPanel(new GridBagLayout());
        videBloc.setOpaque(false);
        GridBagConstraints cv = new GridBagConstraints();
        cv.gridx = 0;
        cv.weightx = 1;
        cv.fill = GridBagConstraints.HORIZONTAL;
        cv.anchor = GridBagConstraints.WEST;
        JLabel vide = new JLabel("Veuillez sélectionner un véhicule pour afficher ses caractéristiques.");
        vide.setFont(police(Font.PLAIN, 12));
        vide.setForeground(TEXTE_SECONDAIRE);
        cv.gridy = 0;
        videBloc.add(vide, cv);
        JLabel source = new JLabel("DONNÉES FICHIER CLIENT");
        source.setFont(police(Font.PLAIN, 11));
        source.setForeground(SECONDAIRE);
        cv.gridy = 1;
        cv.insets = new Insets(4, 0, 0, 0);
        videBloc.add(source, cv);
        panneauVehVide.add(videBloc, BorderLayout.NORTH);

        // Etat renseigne.
        panneauVehDetail.setOpaque(false);
        remplir(panneauVehDetail, "Immatriculation", lblVehImmat, 0);
        remplir(panneauVehDetail, "Modèle", lblVehModele, 1);
        remplir(panneauVehDetail, "Titulaire", lblVehTitulaire, 2);
        remplir(panneauVehDetail, "Catégorie", lblVehType, 3);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        carte.add(panneauVehVide, c);
        carte.add(panneauVehDetail, c);
        return carte;
    }

    private JPanel carteEmplacement() {
        JPanel carte = sousCarte();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(false);
        entete.add(libelleSection("EMPLACEMENT SÉLECTIONNÉ", Icone.Type.PARKING), BorderLayout.WEST);
        JLabel resa = new JLabel("RÉSERVATION");
        resa.setFont(police(Font.PLAIN, 11));
        resa.setForeground(TEXTE_SECONDAIRE);
        entete.add(resa, BorderLayout.EAST);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 0);
        carte.add(entete, c);

        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        lblPlaceNum.setFont(police(Font.BOLD, 14));
        lblPlaceNum.setForeground(TEXTE);
        ligne.add(lblPlaceNum, BorderLayout.WEST);
        lblPlaceEtat.setFont(police(Font.BOLD, 11));
        lblPlaceEtat.setForeground(TEXTE_SECONDAIRE);
        ligne.add(lblPlaceEtat, BorderLayout.EAST);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 0);
        carte.add(ligne, c);
        return carte;
    }

    /** Pied de carte : raccourcis clavier + boutons Annuler / Enregistrer. */
    private JPanel creerPiedCarte() {
        Bande pied = new Bande(false, true);

        JLabel aide = new JLabel("Raccourcis clavier : [Entrée] Valider | [Échap] Annuler");
        aide.setFont(police(Font.PLAIN, 11));
        aide.setForeground(TEXTE_SECONDAIRE);
        pied.add(aide, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        droite.setOpaque(false);
        btnEnregistrer.setPreferredSize(new Dimension(190, 36));
        btnEnregistrer.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> annuler());
        droite.add(btnAnnuler);
        droite.add(btnEnregistrer);
        pied.add(droite, BorderLayout.EAST);
        return pied;
    }

    // ---------------------------------------------------------------------
    // FABRIQUES D'ELEMENTS
    // ---------------------------------------------------------------------
    private JPanel sousCarte() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(BANDE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 8, 1, new Insets(0, 0, 0, 0)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return carte;
    }

    private void remplir(JPanel grille, String nom, JLabel valeur, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);

        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        JLabel g = new JLabel(nom);
        g.setFont(police(Font.PLAIN, 12));
        g.setForeground(TEXTE_SECONDAIRE);
        ligne.add(g, BorderLayout.WEST);
        valeur.setFont(police(Font.BOLD, 13));
        valeur.setForeground(TEXTE);
        valeur.setHorizontalAlignment(JLabel.RIGHT);
        ligne.add(valeur, BorderLayout.CENTER);
        grille.add(ligne, c);
    }

    /** Ligne d'en-tete de champ : libelle a gauche, mention a droite. */
    private JPanel ligneLibelle(String texte, Icone.Type icone, String mention) {
        JPanel ligne = new JPanel(new BorderLayout());
        ligne.setOpaque(false);
        ligne.add(libelleIcone(texte, icone, SECONDAIRE), BorderLayout.WEST);
        JLabel droite = new JLabel(mention);
        droite.setFont(police(Font.PLAIN, 11));
        droite.setForeground(TEXTE_SECONDAIRE);
        ligne.add(droite, BorderLayout.EAST);
        return ligne;
    }

    private static JLabel libelleIcone(String texte, Icone.Type icone, Color couleurIcone) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 13));
        label.setForeground(LABEL);
        label.setIcon(new Icone(icone, 16, couleurIcone));
        label.setIconTextGap(6);
        return label;
    }

    private static JLabel libelleSection(String texte, Icone.Type icone) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 11));
        label.setForeground(LABEL);
        label.setIcon(new Icone(icone, 16, SECONDAIRE));
        label.setIconTextGap(6);
        return label;
    }

    private static JLabel aide(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.PLAIN, 11));
        label.setForeground(TEXTE_SECONDAIRE);
        return label;
    }

    private void demarrerHorloge() {
        // Met a jour l'horodatage affiche.
        lblHorodatage.setText(DateUtils.format(LocalDateTime.now()));
        javax.swing.Timer minuteur = new javax.swing.Timer(1000, e ->
                lblHorodatage.setText(DateUtils.format(LocalDateTime.now())));
        minuteur.start();
    }

    // ---------------------------------------------------------------------
    // LOGIQUE INCHANGEE
    // ---------------------------------------------------------------------
    /**
     * Charge les vehicules qui ne sont pas deja gares
     * et les places libres.
     */
    private void actualiser() {
        chargerVehiculesDisponibles();
        chargerPlacesLibres();
        comboVehicule.setSelectedIndex(-1);
        comboPlace.setSelectedIndex(-1);
        majDetailsVehicule();
        majDetailsPlace();
        majDisponibles();
        majBoutonValider();
    }

    private void chargerVehiculesDisponibles() {
        // Vehicules ayant deja un stationnement EN_COURS.
        List<Integer> gares = new ArrayList<>();
        for (Stationnement s : stationnementDAO.listerEnCours()) {
            gares.add(s.getIdVehicule());
        }

        comboVehicule.removeAllItems();
        for (Vehicule v : vehiculeDAO.lister()) {
            if (!gares.contains(v.getIdVehicule())) {
                comboVehicule.addItem(v);
            }
        }
    }

    private void chargerPlacesLibres() {
        comboPlace.removeAllItems();
        for (Place p : placeDAO.listerLibres()) {
            comboPlace.addItem(p);
        }
    }

    /** Met a jour le panneau "Details du vehicule" selon la selection. */
    private void majDetailsVehicule() {
        Vehicule v = (Vehicule) comboVehicule.getSelectedItem();
        boolean actif = v != null;
        lblVehEtat.setText(actif ? "ACTIF" : "NON SÉLECTIONNÉ");
        lblVehEtat.setForeground(actif ? VERT : TEXTE_SECONDAIRE);
        panneauVehVide.setVisible(!actif);
        panneauVehDetail.setVisible(actif);
        if (actif) {
            lblVehImmat.setText(valeur(v.getImmatriculation()));
            lblVehModele.setText(valeur(v.getMarque()));
            lblVehTitulaire.setText(valeur(v.getNomProprietaire()));
            lblVehType.setText(valeur(v.getTypeVehicule()));
        }
        panneauVehVide.revalidate();
        panneauVehDetail.revalidate();
    }

    /** Met a jour le panneau "Emplacement selectionne" selon la selection. */
    private void majDetailsPlace() {
        Place p = (Place) comboPlace.getSelectedItem();
        boolean choisi = p != null;
        lblPlaceNum.setText(choisi ? p.getNumeroPlace() : "Aucune place attribuée");
        lblPlaceEtat.setText(choisi ? "LIBRE" : "EN ATTENTE");
        lblPlaceEtat.setForeground(choisi ? VERT : TEXTE_SECONDAIRE);
    }

    private void majDisponibles() {
        lblDispo.setText(comboPlace.getItemCount() + " DISPONIBLES");
    }

    /** Rend le bouton "Enregistrer l'entree" actif uniquement si tout est choisi. */
    private void majBoutonValider() {
        boolean pret = comboVehicule.getSelectedItem() != null
                && comboPlace.getSelectedItem() != null;
        btnEnregistrer.setEnabled(pret);
    }

    /** Raccourcis [Entree] Valider et [Echap] Annuler (annonces dans le pied de carte). */
    private void installerRaccourcis() {
        javax.swing.InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "valider");
        im.put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "annuler");
        getActionMap().put("valider", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnEnregistrer.isEnabled()) {
                    enregistrer();
                }
            }
        });
        getActionMap().put("annuler", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                annuler();
            }
        });
    }

    private void annuler() {
        comboVehicule.setSelectedIndex(-1);
        comboPlace.setSelectedIndex(-1);
        majDetailsVehicule();
        majDetailsPlace();
        majBoutonValider();
    }

    private static String valeur(String texte) {
        return (texte == null || texte.trim().isEmpty()) ? "-" : texte.trim();
    }

    private void enregistrer() {
        if (comboVehicule.getSelectedItem() == null || comboPlace.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Aucun vehicule disponible ou aucune place libre.");
            return;
        }

        Vehicule vehicule = (Vehicule) comboVehicule.getSelectedItem();
        Place place = (Place) comboPlace.getSelectedItem();

        boolean ok = stationnementDAO.creerEntree(vehicule.getIdVehicule(), place.getIdPlace());

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Entree enregistree : " + vehicule.getImmatriculation()
                    + " en place " + place.getNumeroPlace() + ".");
            actualiser();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de l'entree.");
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

    private <T> void styleComboVehicule(JComboBox<T> combo) {
        styleComboGenerique(combo, "Sélectionner un véhicule enregistré...");
    }

    private <T> void styleComboPlace(JComboBox<T> combo) {
        styleComboGenerique(combo, "Choisir une place disponible...");
    }

    private <T> void styleComboGenerique(JComboBox<T> combo, String placeholder) {
        combo.setOpaque(true);
        combo.setBackground(CARTE);
        combo.setForeground(TEXTE);
        combo.setFont(police(Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(120, 36));
        combo.setMaximumSize(new Dimension(GRANDE_LARGEUR, 36));
        combo.setBorder(BorderFactory.createLineBorder(BORDURE_CHAMP));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new ComboRenderer(placeholder));
        combo.addActionListener(e -> {
            majDetailsVehicule();
            majDetailsPlace();
            majBoutonValider();
        });
        combo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                combo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMAIRE, 2),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                combo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDURE_CHAMP, 1),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }
        });
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS DE DESSIN
    // ---------------------------------------------------------------------
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
            setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
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

    /** Petite pastille ronde colorée. */
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

    /** Pastille arrondie contenant un libelle. */
    private static class Pill extends JLabel {
        private final Color fond;
        private final Color texte;
        private JLabel contenu;

        Pill(Color fond, Color texte) {
            this.fond = fond;
            this.texte = texte;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        }

        void setTexte(JLabel source) {
            this.contenu = source;
            source.setFont(police(Font.BOLD, 11));
            source.setForeground(texte);
            source.setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = contenu != null ? contenu.getPreferredSize() : super.getPreferredSize();
            Insets in = getInsets();
            return new Dimension(d.width + in.left + in.right, d.height + in.top + in.bottom);
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
            if (contenu != null) {
                Insets in = getInsets();
                contenu.setSize(getWidth() - in.left - in.right, h - in.top - in.bottom);
                Graphics g3 = g.create(in.left, in.top,
                        getWidth() - in.left - in.right, h - in.top - in.bottom);
                contenu.paint(g3);
                g3.dispose();
            }
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

        BoutonStyle(String texte, Type type) {
            super(texte);
            this.type = type;
            this.taillePolice = 13;
            int hauteur = 36;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, taillePolice));

            FontMetrics fm = getFontMetrics(getFont());
            int largeur = fm.stringWidth(texte) + 48;
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
                case DESTRUCTIF:  return new Color(0xDC2626);
                default:          return CARTE;
            }
        }

        private Color couleurSurvol() {
            switch (type) {
                case PRIMAIRE:    return PRIMAIRE_HOVER;
                case SECONDAIRE:  return new Color(0x475569);
                case DESTRUCTIF:  return new Color(0xB91C1C);
                default:          return FOND;
            }
        }

        private Color couleurEnfoncee() {
            switch (type) {
                case PRIMAIRE:    return PRIMAIRE_PRESSED;
                case SECONDAIRE:  return new Color(0x334155);
                case DESTRUCTIF:  return new Color(0x7F1D1D);
                default:          return BORDURE;
            }
        }

        private Color couleurTexte() {
            return type == Type.NEUTRE ? LABEL : Color.WHITE;
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
            } else if (value instanceof Vehicule) {
                Vehicule v = (Vehicule) value;
                label.setText(v.getImmatriculation() + " — " + v.getMarque()
                        + " (" + valeur(v.getNomProprietaire()) + ")");
                label.setForeground(TEXTE);
            } else if (value instanceof Place) {
                label.setText("Place " + ((Place) value).getNumeroPlace());
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
