package view;

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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import dao.PlaceDAO;
import dao.StationnementDAO;
import model.Stationnement;
import model.Utilisateur;

/**
 * Fenetre principale de SmartPark.
 * Barre laterale bleu nuit, zone de contenu claire. Les statistiques de la
 * page d'accueil sont rafraichies a l'ouverture, au retour sur la carte
 * "dashboard", a la reactivation de la fenetre et toutes les 30 secondes.
 */
public class Dashboard extends JFrame {

    private final java.awt.CardLayout cartes;
    private final JPanel content;
    private final JPanel menu;
    private final Utilisateur utilisateur;

    // Panels des modules (navigation inchangee).
    private final ProprietaireFormPanel panelProprietaire;
    private final VehiculeForm panelVehicule;
    private final EntreeForm panelEntree;
    private final SortieForm panelSortie;
    private final HistoriqueForm panelHistorique;
    private final StatistiqueForm panelStatistique;
    private final ParametreForm panelParametre;

    // Boutons du menu : permet de connaitre LA page active.
    private final Map<String, BoutonMenu> boutonsNav = new LinkedHashMap<>();

    // Sources de donnees de la page d'accueil (DAO deja utilises).
    private final StationnementDAO stationnementDAO = new StationnementDAO();
    private final PlaceDAO placeDAO = new PlaceDAO();

    // References pour rafraichir le nom affiche (surProfilModifie).
    private JLabel labelConnecte;
    private JLabel labelNomEntete;
    private PastilleUtilisateur pastilleUtilisateur;

    // Groupe "Stationnement" du menu (sous-menu deroulant Entree / Sortie).
    private JPanel nav;
    private BoutonGroupe boutonStationnement;
    private JPanel sousMenuStationnement;
    private BoutonMenu boutonEntree;
    private BoutonMenu boutonSortie;
    private boolean stationnementOuvert;

    // Rafraichissement automatique des statistiques.
    private javax.swing.Timer minuterie;
    private String carteActive = "dashboard";

    // References de la page d'accueil (KPI, graphique, journal).
    private CarteKpi cartePlaces;
    private CarteKpi carteVehicules;
    private CarteKpi carteRecette;
    private CarteKpi carteSorties;
    private PanneauGraphique graphique;
    private JLabel labelTotal;
    private JLabel labelMoyenne;
    private JLabel labelPic;
    private PanneauJournal journal;

    // ---------------------------------------------------------------------
    // PALETTE MENU LATERAL (bleu nuit)
    // ---------------------------------------------------------------------
    private static final Color MENU_FOND        = new Color(0x1B2B3F);
    private static final Color MENU_SURVOL      = new Color(0x26364A);
    private static final Color MENU_ACTIF       = new Color(0x2563EB);
    private static final Color MENU_TEXTE       = new Color(0xCBD5E1);
    private static final Color MENU_TEXTE_ACTIF = new Color(0xFFFFFF);
    private static final Color MENU_BORDURE     = new Color(255, 255, 255, 26);
    private static final Color MENU_DECO_SURVOL = new Color(0xDC2626);

    // ---------------------------------------------------------------------
    // PALETTE CONTENU (clair)
    // ---------------------------------------------------------------------
    private static final Color FOND           = new Color(0xF1F5F9);
    private static final Color CARTE          = new Color(0xFFFFFF);
    private static final Color CARTE_ENTETE   = new Color(0xF8FAFC);
    private static final Color BORDURE        = new Color(0xE2E8F0);
    private static final Color TEXTE          = new Color(0x0F172A);
    private static final Color TEXTE_DOUX     = new Color(0x64748B);
    private static final Color PRIMAIRE       = new Color(0x2563EB);
    private static final Color PRIMAIRE_FONCE = new Color(0x1E40AF);
    private static final Color SUCCES         = new Color(0x16A34A);
    private static final Color SUCCES_FOND    = new Color(0xDCFCE7);
    private static final Color SUCCES_BORD    = new Color(0xBBF7D0);
    private static final Color AMBRE          = new Color(0xD97706);
    private static final Color AMBRE_FOND     = new Color(0xFEF3C7);
    private static final Color AMBRE_BORD     = new Color(0xFDE68A);
    private static final Color ERREUR         = new Color(0xDC2626);
    private static final Color NEUTRE         = new Color(0x94A3B8);

    private static final int LARGEUR_MENU = 208;
    private static final int HAUTEUR_KPI = 158;

    public Dashboard(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.cartes = new java.awt.CardLayout();
        this.content = new JPanel(cartes);
        this.menu = new JPanel();

        // Creation des modules une seule fois (inchange).
        panelProprietaire = new ProprietaireFormPanel();
        panelVehicule = new VehiculeForm();
        panelEntree = new EntreeForm();
        panelSortie = new SortieForm();
        panelHistorique = new HistoriqueForm();
        panelStatistique = new StatistiqueForm();
        panelParametre = new ParametreForm(utilisateur);

        construireFenetre();
        construireMenu();
        construireContenu();
        actualiserAccueil();

        // Rafraichit le nom affiche (barre laterale + entete) quand le profil change.
        if (panelParametre != null) {
            panelParametre.setSurProfilModifie(this::rafraichirProfil);
        }

        // Rafraichissement periodique : uniquement quand la carte visible est "dashboard".
        minuterie = new javax.swing.Timer(30000, e -> {
            if ("dashboard".equals(carteActive)) {
                actualiserAccueil();
            }
        });
        minuterie.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                if ("dashboard".equals(carteActive)) {
                    actualiserAccueil();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (minuterie != null) {
                    minuterie.stop();
                }
            }
        });
    }

    private void construireFenetre() {
        setTitle("SmartPark - Gestion de parking");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1180, 690));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // ---------------------------------------------------------------------
    // MENU LATERAL (aside w-[208px], bleu nuit)
    // ---------------------------------------------------------------------
    private void construireMenu() {
        menu.setBackground(MENU_FOND);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(LARGEUR_MENU, 0));
        menu.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, MENU_BORDURE));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- En-tete (h-20 = 80) : logo + titres centres verticalement ---
        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(true);
        entete.setBackground(MENU_FOND);
        entete.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        entete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        entete.setPreferredSize(new Dimension(LARGEUR_MENU, 80));
        entete.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ligne = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        ligne.setOpaque(false);
        ligne.add(new CarreLogo());

        JPanel titres = new JPanel();
        titres.setOpaque(false);
        titres.setLayout(new BoxLayout(titres, BoxLayout.Y_AXIS));
        JLabel titre = new JLabel("SmartPark");
        titre.setFont(police(Font.BOLD, 16));
        titre.setForeground(MENU_TEXTE_ACTIF);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        titres.add(titre);
        JLabel sousTitre = new JLabel("GESTION DE PARKING");
        sousTitre.setFont(police(Font.BOLD, 11));
        sousTitre.setForeground(MENU_TEXTE);
        sousTitre.setAlignmentX(Component.LEFT_ALIGNMENT);
        titres.add(sousTitre);
        ligne.add(titres);
        entete.add(ligne, BorderLayout.CENTER);

        menu.add(entete);

        // --- Separateur + vrai espace entre l'en-tete et la navigation ---
        menu.add(separateurMenu());
        menu.add(Box.createVerticalStrut(16));

        // --- Navigation ---
        nav = new JPanel();
        nav.setOpaque(true);
        nav.setBackground(MENU_FOND);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 8, 8, 8));
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);

        ajouterBouton(nav, "Dashboard", Icone.Type.ACCUEIL, "dashboard");
        ajouterBouton(nav, "Proprietaires", Icone.Type.PROPRIETAIRE, "proprietaire");
        ajouterBouton(nav, "Vehicules", Icone.Type.VEHICULE, "vehicule");

        // Groupe "Stationnement" : Entree / Sortie dans un sous-menu deroulant.
        boutonStationnement = new BoutonGroupe("Stationnement", Icone.Type.STATIONNEMENT);
        boutonStationnement.addActionListener(e -> basculerStationnement());
        nav.add(boutonStationnement);
        nav.add(Box.createVerticalStrut(6));

        sousMenuStationnement = new JPanel();
        sousMenuStationnement.setOpaque(true);
        sousMenuStationnement.setBackground(MENU_FOND);
        sousMenuStationnement.setLayout(new BoxLayout(sousMenuStationnement, BoxLayout.Y_AXIS));
        sousMenuStationnement.setAlignmentX(Component.LEFT_ALIGNMENT);

        boutonEntree = new BoutonMenu("Entree", Icone.Type.ENTREE, false);
        boutonEntree.setIndente(true);
        boutonEntree.addActionListener(e -> afficherCarte("entree"));
        boutonsNav.put("entree", boutonEntree);

        boutonSortie = new BoutonMenu("Sortie", Icone.Type.SORTIE, false);
        boutonSortie.setIndente(true);
        boutonSortie.addActionListener(e -> afficherCarte("sortie"));
        boutonsNav.put("sortie", boutonSortie);

        sousMenuStationnement.add(boutonEntree);
        sousMenuStationnement.add(Box.createVerticalStrut(6));
        sousMenuStationnement.add(boutonSortie);
        sousMenuStationnement.setVisible(false);
        nav.add(sousMenuStationnement);

        ajouterBouton(nav, "Historique", Icone.Type.HISTORIQUE, "historique");
        ajouterBouton(nav, "Parametres", Icone.Type.PARAMETRE, "parametre");

        menu.add(nav);

        menu.add(Box.createVerticalGlue());

        // --- Utilisateur (pastille + label fonctionnels : mis a jour par le profil) ---
        JPanel blocUtilisateur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        blocUtilisateur.setOpaque(true);
        blocUtilisateur.setBackground(MENU_FOND);
        blocUtilisateur.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        blocUtilisateur.setMaximumSize(new Dimension(LARGEUR_MENU, 52));
        blocUtilisateur.setAlignmentX(Component.LEFT_ALIGNMENT);

        pastilleUtilisateur = new PastilleUtilisateur(initiale(utilisateur));
        blocUtilisateur.add(pastilleUtilisateur);

        labelConnecte = new JLabel("Connecte : " + nomAffiche());
        labelConnecte.setFont(police(Font.PLAIN, 12));
        labelConnecte.setForeground(MENU_TEXTE);
        blocUtilisateur.add(labelConnecte);

        menu.add(blocUtilisateur);

        // --- Deconnexion (p-space-sm border-t) ---
        JPanel bas = new JPanel();
        bas.setOpaque(true);
        bas.setBackground(MENU_FOND);
        bas.setLayout(new BoxLayout(bas, BoxLayout.Y_AXIS));
        bas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, MENU_BORDURE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        bas.setMaximumSize(new Dimension(LARGEUR_MENU, 60));
        bas.setAlignmentX(Component.LEFT_ALIGNMENT);

        BoutonMenu btnDeconnexion = new BoutonMenu("Deconnexion",
                Icone.Type.DECONNEXION, true);
        btnDeconnexion.addActionListener(this::deconnecter);
        bas.add(btnDeconnexion);
        menu.add(bas);

        add(menu, BorderLayout.WEST);

        ajusterNav();
        marquerActif("dashboard");
    }

    /** Trait de separation translucide dans la barre laterale. */
    private static JPanel separateurMenu() {
        JPanel sep = new JPanel();
        sep.setOpaque(true);
        sep.setBackground(MENU_BORDURE);
        sep.setPreferredSize(new Dimension(LARGEUR_MENU, 1));
        sep.setMinimumSize(new Dimension(0, 1));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void ajouterBouton(JPanel parent, String texte, Icone.Type type, String nomCarte) {
        BoutonMenu bouton = new BoutonMenu(texte, type, false);
        bouton.addActionListener(e -> afficherCarte(nomCarte));
        boutonsNav.put(nomCarte, bouton);
        parent.add(bouton);
        parent.add(Box.createVerticalStrut(6));
    }

    /** Bascule (deplie / replie) le sous-menu "Stationnement". */
    private void basculerStationnement() {
        ouvrirStationnement(!stationnementOuvert);
    }

    /** Ouvre ou ferme le sous-menu, met a jour le chevron et la geometrie. */
    private void ouvrirStationnement(boolean ouvert) {
        if (sousMenuStationnement == null) {
            return;
        }
        stationnementOuvert = ouvert;
        sousMenuStationnement.setVisible(ouvert);
        if (boutonStationnement != null) {
            boutonStationnement.setOuvert(ouvert);
        }
        nav.revalidate();
        nav.repaint();
        ajusterNav();
    }

    /**
     * Cale la hauteur maximale du panneau de navigation sur sa hauteur reelle,
     * recalculee dynamiquement quand le sous-menu se deplie ou se replie.
     */
    private void ajusterNav() {
        if (nav == null) {
            return;
        }
        nav.setMaximumSize(new Dimension(LARGEUR_MENU, nav.getPreferredSize().height));
    }

    /** Marque visuellement la page active dans le menu. */
    private void marquerActif(String nomCarte) {
        for (Map.Entry<String, BoutonMenu> entree : boutonsNav.entrySet()) {
            entree.getValue().setActif(entree.getKey().equals(nomCarte));
        }
        boolean dansStationnement = "entree".equals(nomCarte) || "sortie".equals(nomCarte);
        if (boutonStationnement != null) {
            boutonStationnement.setEnEvidence(dansStationnement);
            if (dansStationnement && !stationnementOuvert) {
                ouvrirStationnement(true);
            }
        }
    }

    // ---------------------------------------------------------------------
    // ZONE CENTRALE
    // ---------------------------------------------------------------------
    private void construireContenu() {
        content.setBackground(FOND);
        content.add(creerAccueil(), "dashboard");
        content.add(envelopper(panelProprietaire), "proprietaire");
        content.add(envelopper(panelVehicule), "vehicule");
        content.add(envelopper(panelEntree), "entree");
        content.add(envelopper(panelSortie), "sortie");
        content.add(envelopper(panelHistorique), "historique");
        content.add(envelopper(panelParametre), "parametre");
        // Module Statistique conserve (alimente via DAO) mais non navigable.
        content.add(panelStatistique, "statistiques");

        // En-tete global et fixe : createur une seule fois, partage par toutes
        // les pages. Seul le contenu central (CardLayout) change a la navigation.
        JPanel centre = new JPanel(new BorderLayout());
        centre.setOpaque(true);
        centre.setBackground(FOND);
        centre.add(creerBandeauGlobal(), BorderLayout.NORTH);
        centre.add(content, BorderLayout.CENTER);

        add(centre, BorderLayout.CENTER);
    }

    /**
     * En-tete global de l'application (date, notifications, profil utilisateur).
     * Construit une seule fois et place en haut de la zone de contenu, a droite
     * de la barre laterale. Il reste donc fixe lors de la navigation.
     */
    private JPanel creerBandeauGlobal() {
        JPanel conteneur = new JPanel(new BorderLayout());
        conteneur.setOpaque(true);
        conteneur.setBackground(FOND);
        conteneur.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 24));
        conteneur.add(creerEnteteAccueil(), BorderLayout.CENTER);
        return conteneur;
    }

    /** Ajoute une marge interieure autour d'un panneau module. */
    private JPanel envelopper(JPanel panneau) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(FOND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        wrapper.add(panneau, BorderLayout.CENTER);
        return wrapper;
    }

    // ---------------------------------------------------------------------
    // PAGE D'ACCUEIL (contenu clair)
    // ---------------------------------------------------------------------
    private JPanel creerAccueil() {
        JPanel accueil = new JPanel(new BorderLayout());
        accueil.setOpaque(true);
        accueil.setBackground(FOND);
        accueil.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Cartes KPI (le bandeau date/notifications/profil est desormais global).
        JPanel nord = new JPanel();
        nord.setOpaque(false);
        nord.setLayout(new BoxLayout(nord, BoxLayout.Y_AXIS));
        nord.add(creerCartesKpi());
        accueil.add(nord, BorderLayout.NORTH);

        // Grille basse : graphique (3/4) + journal (1/4).
        JPanel bas = new JPanel(new GridBagLayout());
        bas.setOpaque(false);
        bas.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 0.75;
        gc.insets = new Insets(0, 0, 0, 16);
        bas.add(creerPanneauGraphique(), gc);

        gc.gridx = 1;
        gc.weightx = 0.25;
        gc.insets = new Insets(0, 0, 0, 0);
        bas.add(creerPanneauJournal(), gc);

        accueil.add(bas, BorderLayout.CENTER);
        return accueil;
    }

    /** Bandeau du haut : date a gauche, notifications + utilisateur a droite. */
    private JPanel creerEnteteAccueil() {
        JPanel bandeau = new JPanel(new BorderLayout());
        bandeau.setOpaque(false);
        bandeau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        bandeau.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gauche.setOpaque(false);
        gauche.add(new JLabel(new Icone(Icone.Type.CALENDRIER, 20, TEXTE_DOUX)));
        JLabel date = new JLabel(dateDuJour());
        date.setFont(police(Font.PLAIN, 15));
        date.setForeground(TEXTE);
        gauche.add(date);
        bandeau.add(gauche, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        droite.add(new BoutonNotifications());
        droite.add(creerChipUtilisateur());
        bandeau.add(droite, BorderLayout.EAST);
        return bandeau;
    }

    /** Chip utilisateur (avatar + nom + Online). */
    private JPanel creerChipUtilisateur() {
        FondArrondi chip = new FondArrondi(CARTE, BORDURE, 8);
        chip.setLayout(new BorderLayout(8, 0));
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        chip.add(new PastilleIcone(Icone.Type.UTILISATEUR, 20, FOND, TEXTE_DOUX),
                BorderLayout.WEST);

        JPanel colonne = new JPanel();
        colonne.setOpaque(false);
        colonne.setLayout(new BoxLayout(colonne, BoxLayout.Y_AXIS));

        labelNomEntete = new JLabel(nomAffiche());
        labelNomEntete.setFont(police(Font.BOLD, 12));
        labelNomEntete.setForeground(TEXTE);
        labelNomEntete.setAlignmentX(Component.LEFT_ALIGNMENT);
        colonne.add(labelNomEntete);

        JPanel enLigne = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        enLigne.setOpaque(false);
        enLigne.add(new PointColor(SUCCES, 6));
        enLigne.add(texte("Online", Font.PLAIN, 11, SUCCES));
        enLigne.setAlignmentX(Component.LEFT_ALIGNMENT);
        colonne.add(enLigne);

        chip.add(colonne, BorderLayout.CENTER);
        JPanel conteneur = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        conteneur.setOpaque(false);
        conteneur.add(chip);
        return conteneur;
    }

    /** Rang de 4 cartes KPI (fond blanc, bordure, barre d'accent). */
    private JPanel creerCartesKpi() {
        JPanel grille = new JPanel(new GridLayout(1, 4, 16, 0));
        grille.setOpaque(false);
        grille.setAlignmentX(Component.LEFT_ALIGNMENT);
        grille.setPreferredSize(new Dimension(10, HAUTEUR_KPI));
        grille.setMaximumSize(new Dimension(Integer.MAX_VALUE, HAUTEUR_KPI));

        cartePlaces = new CarteKpi("Capacite disponible", "Places disponibles",
                Icone.Type.PARKING, SUCCES,
                SUCCES_FOND, SUCCES_BORD, SUCCES, SUCCES, null);
        carteVehicules = new CarteKpi("Capacite occupee", "Vehicules stationnes",
                Icone.Type.VEHICULE, AMBRE,
                AMBRE_FOND, AMBRE_BORD, AMBRE, AMBRE, null);
        carteRecette = new CarteKpi("Comptabilite du jour", "Recette du jour",
                Icone.Type.TARIF, PRIMAIRE,
                SUCCES_FOND, SUCCES_BORD, SUCCES, null, Icone.Type.TENDANCE);
        carteSorties = new CarteKpi("Activite du jour", "Stationnements aujourd'hui",
                Icone.Type.COCHE, NEUTRE,
                FOND, BORDURE, TEXTE, PRIMAIRE, null);

        grille.add(cartePlaces);
        grille.add(carteVehicules);
        grille.add(carteRecette);
        grille.add(carteSorties);
        return grille;
    }

    /** Panneau graphique (bar_chart) avec en-tete, axe Y et pied de synthese. */
    private JPanel creerPanneauGraphique() {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setOpaque(true);
        carte.setBackground(CARTE);
        carte.setBorder(BorderFactory.createLineBorder(BORDURE, 1));

        // En-tete (h-12 = 48).
        JPanel entete = new JPanel(new BorderLayout());
        entete.setOpaque(true);
        entete.setBackground(CARTE_ENTETE);
        entete.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDURE),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
        entete.setPreferredSize(new Dimension(10, 48));

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gauche.setOpaque(false);
        gauche.add(new JLabel(new Icone(Icone.Type.STATISTIQUE, 18, PRIMAIRE)));
        JLabel titre = new JLabel("Recettes des 7 derniers jours");
        titre.setFont(police(Font.BOLD, 16));
        titre.setForeground(TEXTE);
        gauche.add(titre);
        gauche.add(new Chip("SERIE HEBDOMADAIRE", FOND, BORDURE, TEXTE_DOUX, null, null));
        entete.add(gauche, BorderLayout.WEST);

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        droite.setOpaque(false);
        JPanel legende = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        legende.setOpaque(false);
        legende.add(new CarrePlein(PRIMAIRE, 10));
        legende.add(texte("Recette encaissee (Ar)", Font.PLAIN, 11, TEXTE_DOUX));
        droite.add(legende);
        labelTotal = texte("Total : 0 Ar", Font.BOLD, 11, TEXTE);
        droite.add(labelTotal);
        entete.add(droite, BorderLayout.EAST);
        carte.add(entete, BorderLayout.NORTH);

        // Corps.
        JPanel corps = new JPanel(new BorderLayout(0, 8));
        corps.setOpaque(false);
        corps.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        graphique = new PanneauGraphique();
        corps.add(graphique, BorderLayout.CENTER);

        JPanel pied = new JPanel(new BorderLayout());
        pied.setOpaque(false);
        pied.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDURE),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)));

        JPanel piedGauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        piedGauche.setOpaque(false);
        labelMoyenne = texte("Moyenne journaliere : 0 Ar", Font.PLAIN, 11, TEXTE_DOUX);
        labelPic = texte("Pic hebdomadaire : 0 Ar (-)", Font.PLAIN, 11, TEXTE_DOUX);
        piedGauche.add(labelMoyenne);
        piedGauche.add(labelPic);

        JPanel piedDroite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        piedDroite.setOpaque(false);
        piedDroite.add(new JLabel(new Icone(Icone.Type.TENDANCE, 14, SUCCES)));
        piedDroite.add(texte("Tendance sur 7 jours", Font.PLAIN, 11, SUCCES));

        pied.add(piedGauche, BorderLayout.WEST);
        pied.add(piedDroite, BorderLayout.EAST);
        corps.add(pied, BorderLayout.SOUTH);

        carte.add(corps, BorderLayout.CENTER);
        return carte;
    }

    /** Panneau journal (stream) alimente par l'historique des stationnements. */
    private JPanel creerPanneauJournal() {
        journal = new PanneauJournal(() -> afficherCarte("historique"));
        return journal;
    }

    // ---------------------------------------------------------------------
    // DONNEES DE L'ACCUEIL (DAO existants, lecture seule)
    // ---------------------------------------------------------------------

    /**
     * Recharge les statistiques de la page d'accueil depuis la base.
     * Ne leve jamais d'exception : en cas de base indisponible, affiche
     * des valeurs a zero, un graphique vide et un journal vide.
     */
    private void actualiserAccueil() {
        try {
            int gares = stationnementDAO.vehiculesActuellementGares();
            int libres = placeDAO.compterLibres();
            int total = placeDAO.listerToutes().size();
            double recette = stationnementDAO.recettesAujourdhui();
            int sorties = stationnementDAO.stationnementsAujourdhui();
            double occupation = total > 0 ? (gares * 100.0 / total) : 0;

            if (cartePlaces != null) {
                cartePlaces.setValeurs(String.valueOf(libres), "/ " + nombre(total),
                        TEXTE_DOUX, libres > 0 ? "Disponible" : "Complet",
                        nombre(total) + " places au total");
                String etatOccupation = occupation >= 90 ? "Saturation"
                        : occupation >= 60 ? "Charge elevee" : "Fluide";
                carteVehicules.setValeurs(String.valueOf(gares), "", TEXTE_DOUX,
                        String.format(Locale.US, "%.1f%%", occupation) + " d'occupation",
                        etatOccupation);
                carteRecette.setValeurs(nombre(recette), "Ar", PRIMAIRE,
                        "Aujourd'hui", "Arrete " + heure(LocalDateTime.now()));
                carteSorties.setValeurs(String.valueOf(sorties), "", TEXTE_DOUX,
                        sorties > 0 ? "Journee active" : "Journee calme",
                        nombre(gares) + " vehicules gares");
            }

            if (graphique != null) {
                graphique.setDonnees(stationnementDAO.recettesParJour());
                labelTotal.setText("Total : " + nombre(graphique.getSomme()) + " Ar");
                labelMoyenne.setText("Moyenne journaliere : "
                        + nombre(graphique.getMoyenne()) + " Ar");
                labelPic.setText("Pic hebdomadaire : " + nombre(graphique.getPic())
                        + " Ar (" + graphique.getPicJour() + ")");
            }

            if (journal != null) {
                journal.setDonnees(stationnementDAO.listerHistorique(""));
            }
        } catch (Exception ex) {
            afficherAccueilParDefaut();
        }
    }

    /** Etat neutre de la page d'accueil quand la base est indisponible. */
    private void afficherAccueilParDefaut() {
        if (cartePlaces != null) {
            cartePlaces.setValeurs("0", "/ 0", TEXTE_DOUX, "Disponible",
                    "0 places au total");
            carteVehicules.setValeurs("0", "", TEXTE_DOUX, "0.0% d'occupation", "Fluide");
            carteRecette.setValeurs("0", "Ar", PRIMAIRE, "Aujourd'hui",
                    "Arrete " + heure(LocalDateTime.now()));
            carteSorties.setValeurs("0", "", TEXTE_DOUX, "Journee calme",
                    "0 vehicules gares");
        }
        if (graphique != null) {
            graphique.setDonnees(new String[0][]);
            labelTotal.setText("Total : 0 Ar");
            labelMoyenne.setText("Moyenne journaliere : 0 Ar");
            labelPic.setText("Pic hebdomadaire : 0 Ar (-)");
        }
        if (journal != null) {
            journal.setDonnees(java.util.Collections.<Stationnement>emptyList());
        }
    }

    /** Nom affichable de l'utilisateur (jamais null). */
    private String nomAffiche() {
        String nom = utilisateur != null ? utilisateur.getNom() : null;
        return (nom != null && !nom.isEmpty()) ? nom : "Utilisateur";
    }

    /** Initiale de l'utilisateur pour la pastille. */
    private static String initiale(Utilisateur utilisateur) {
        String nom = utilisateur != null ? utilisateur.getNom() : null;
        return (nom != null && !nom.isEmpty())
                ? nom.substring(0, 1).toUpperCase(Locale.FRENCH) : "?";
    }

    /** Repercute le nom du profil modifie sur la barre laterale et l'entete. */
    private void rafraichirProfil() {
        String nom = nomAffiche();
        if (labelConnecte != null) {
            labelConnecte.setText("Connecte : " + nom);
        }
        if (labelNomEntete != null) {
            labelNomEntete.setText(nom);
        }
        if (pastilleUtilisateur != null) {
            pastilleUtilisateur.setInitiale(initiale(utilisateur));
        }
    }

    private static String dateDuJour() {
        LocalDate aujourdhui = LocalDate.now();
        String texte = aujourdhui.format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
        return Character.toUpperCase(texte.charAt(0)) + texte.substring(1);
    }

    // ---------------------------------------------------------------------
    // NAVIGATION
    // ---------------------------------------------------------------------
    private void afficherCarte(String nomCarte) {
        carteActive = nomCarte;
        cartes.show(content, nomCarte);
        marquerActif(nomCarte);
        if ("dashboard".equals(nomCarte)) {
            actualiserAccueil();
            panelStatistique.actualiser();
        }
    }

    private void deconnecter(ActionEvent e) {
        dispose();
        Login login = new Login();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }

    public static void main(String[] args) {
        new Dashboard(new Utilisateur()).setVisible(true);
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

    /** Label de texte simple, deja configure. */
    private static JLabel texte(String libelle, int style, int taille, Color couleur) {
        JLabel label = new JLabel(libelle);
        label.setFont(police(style, taille));
        label.setForeground(couleur);
        return label;
    }

    /** Entier avec separateurs de milliers (espaces). */
    private static String nombre(double valeur) {
        return String.format(Locale.US, "%,d", Math.round(valeur)).replace(',', ' ');
    }

    /** Valeur compacte pour l'etiquette au-dessus d'une barre (1.42M). */
    private static String compact(double valeur) {
        if (valeur >= 1_000_000) {
            return String.format(Locale.US, "%.2fM", valeur / 1_000_000.0);
        }
        if (valeur >= 1_000) {
            return String.format(Locale.US, "%.0fk", valeur / 1_000.0);
        }
        return String.format(Locale.US, "%.0f", valeur);
    }

    private static String heure(LocalDateTime moment) {
        if (moment == null) {
            return "--:--:--";
        }
        return moment.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private static String duree(double heures) {
        int h = (int) Math.floor(heures);
        int m = (int) Math.round((heures - h) * 60);
        if (m == 60) {
            h++;
            m = 0;
        }
        return String.format(Locale.US, "%02dh %02dm", h, m);
    }

    private static String valeurOu(String v, String defaut) {
        return (v != null && !v.isEmpty()) ? v : defaut;
    }

    // ---------------------------------------------------------------------
    // BOUTON DU MENU LATERAL (coins francs, h-40, label-lg)
    // ---------------------------------------------------------------------
    private static class BoutonMenu extends JButton {

        private final Icone icone;
        private final boolean deconnexion;
        private boolean actif;
        private int indentage;
        private int hauteur = 40;
        private int tailleTexte = 14;

        BoutonMenu(String texte, Icone.Type type, boolean deconnexion) {
            super(texte);
            this.deconnexion = deconnexion;
            this.icone = new Icone(type, 20, MENU_TEXTE);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setPreferredSize(new Dimension(LARGEUR_MENU - 16, hauteur));
            setMaximumSize(new Dimension(LARGEUR_MENU - 16, hauteur));
        }

        /** Variante indentee + compacte pour les elements du sous-menu. */
        void setIndente(boolean indente) {
            this.indentage = indente ? 16 : 0;
            this.hauteur = indente ? 34 : 40;
            this.tailleTexte = indente ? 13 : 14;
            setPreferredSize(new Dimension(LARGEUR_MENU - 16, hauteur));
            setMaximumSize(new Dimension(LARGEUR_MENU - 16, hauteur));
        }

        void setActif(boolean actif) {
            if (this.actif != actif) {
                this.actif = actif;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            ButtonModel modele = getModel();
            boolean survol = modele.isRollover();

            // Fond plein : MENU_ACTIF si actif, MENU_SURVOL / MENU_DECO_SURVOL au survol.
            Color fond = null;
            if (actif) {
                fond = MENU_ACTIF;
            } else if (survol) {
                fond = deconnexion ? MENU_DECO_SURVOL : MENU_SURVOL;
            }
            if (fond != null) {
                g2.setColor(fond);
                g2.fillRect(0, 0, w, h);
            }

            // Texte/icone clairs sur le bleu nuit.
            Color couleur;
            if (actif || survol) {
                couleur = MENU_TEXTE_ACTIF;
            } else {
                couleur = MENU_TEXTE;
            }

            icone.setCouleur(couleur);
            icone.paintIcon(this, g2, 12 + indentage, (h - 20) / 2);

            g2.setFont(police(actif ? Font.BOLD : Font.PLAIN, tailleTexte));
            FontMetrics fm = g2.getFontMetrics();
            int yBase = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(couleur);
            g2.drawString(getText(), 44 + indentage, yBase);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // ENTREE DE GROUPE DU MENU (deplie/replie le sous-menu "Stationnement")
    // ---------------------------------------------------------------------
    private static class BoutonGroupe extends JButton {

        private final Icone icone;
        private boolean ouvert;
        private boolean enEvidence;

        BoutonGroupe(String texte, Icone.Type type) {
            super(texte);
            this.icone = new Icone(type, 20, MENU_TEXTE);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setPreferredSize(new Dimension(LARGEUR_MENU - 16, 40));
            setMaximumSize(new Dimension(LARGEUR_MENU - 16, 40));
        }

        void setOuvert(boolean ouvert) {
            if (this.ouvert != ouvert) {
                this.ouvert = ouvert;
                repaint();
            }
        }

        void setEnEvidence(boolean enEvidence) {
            if (this.enEvidence != enEvidence) {
                this.enEvidence = enEvidence;
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            boolean survol = getModel().isRollover();

            if (survol) {
                g2.setColor(MENU_SURVOL);
                g2.fillRect(0, 0, w, h);
            }

            // Mise en evidence (page Entree/Sortie active) sans fond plein.
            Color couleur = (survol || enEvidence) ? MENU_TEXTE_ACTIF : MENU_TEXTE;
            icone.setCouleur(couleur);
            icone.paintIcon(this, g2, 12, (h - 20) / 2);

            g2.setFont(police(enEvidence ? Font.BOLD : Font.PLAIN, 14));
            FontMetrics fm = g2.getFontMetrics();
            int yBase = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(couleur);
            g2.drawString(getText(), 44, yBase);

            Icone.Type chevron = ouvert ? Icone.Type.CHEVRON_HAUT : Icone.Type.CHEVRON_BAS;
            new Icone(chevron, 18, couleur).paintIcon(this, g2, w - 28, (h - 18) / 2);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS DE DESSIN
    // ---------------------------------------------------------------------

    /** Panneau a fond/bordure (rayon 0 = coins francs). */
    private static class FondArrondi extends JPanel {

        private final Color fond;
        private final Color bordure;
        private final int rayon;

        FondArrondi(Color fond, Color bordure, int rayon) {
            this.fond = fond;
            this.bordure = bordure;
            this.rayon = rayon;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (fond != null) {
                g2.setColor(fond);
                g2.fillRoundRect(0, 0, w, h, rayon, rayon);
            }
            if (bordure != null) {
                g2.setColor(bordure);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, rayon, rayon));
            }
            g2.dispose();
        }
    }

    /** Carte KPI (fond blanc, bordure, barre d'accent basse). */
    private static class CarteKpi extends FondArrondi {

        private final JLabel labelValeur;
        private final JLabel labelSuffixe;
        private final Chip chip;
        private final JLabel labelDroite;
        private final Color accent;

        CarteKpi(String surTitre, String titre, Icone.Type icone, Color accent,
                Color chipFond, Color chipBordure, Color chipTexte,
                Color chipPoint, Icone.Type chipIcone) {
            super(CARTE, BORDURE, 0);
            this.accent = accent;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(16, 16, 18, 16));

            JPanel entete = new JPanel(new BorderLayout(8, 0));
            entete.setOpaque(false);
            JPanel textes = new JPanel();
            textes.setOpaque(false);
            textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
            JLabel sur = new JLabel(surTitre.toUpperCase(Locale.FRENCH));
            sur.setFont(police(Font.BOLD, 11));
            sur.setForeground(TEXTE_DOUX);
            sur.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel tit = new JLabel(titre);
            tit.setFont(police(Font.BOLD, 16));
            tit.setForeground(TEXTE);
            tit.setAlignmentX(Component.LEFT_ALIGNMENT);
            textes.add(sur);
            textes.add(Box.createVerticalStrut(4));
            textes.add(tit);
            entete.add(textes, BorderLayout.CENTER);
            entete.add(new BoiteIcone(icone, 20, accent), BorderLayout.EAST);
            add(entete, BorderLayout.NORTH);

            JPanel valeur = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            valeur.setOpaque(false);
            valeur.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
            labelValeur = new JLabel("");
            labelValeur.setFont(police(Font.BOLD, 32));
            labelValeur.setForeground(TEXTE);
            labelSuffixe = new JLabel("");
            labelSuffixe.setFont(police(Font.PLAIN, 20));
            labelSuffixe.setForeground(TEXTE_DOUX);
            valeur.add(labelValeur);
            valeur.add(labelSuffixe);
            add(valeur, BorderLayout.CENTER);

            JPanel pied = new JPanel(new BorderLayout());
            pied.setOpaque(false);
            pied.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, BORDURE),
                    BorderFactory.createEmptyBorder(8, 0, 0, 0)));
            chip = new Chip("", chipFond, chipBordure, chipTexte, chipPoint, chipIcone);
            pied.add(chip, BorderLayout.WEST);
            labelDroite = new JLabel("");
            labelDroite.setFont(police(Font.PLAIN, 11));
            labelDroite.setForeground(TEXTE_DOUX);
            JPanel pd = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pd.setOpaque(false);
            pd.add(labelDroite);
            pied.add(pd, BorderLayout.EAST);
            add(pied, BorderLayout.SOUTH);
        }

        void setValeurs(String valeur, String suffixe, Color couleurSuffixe,
                String chipTexte, String droiteTexte) {
            labelValeur.setText(valeur);
            labelSuffixe.setText(suffixe);
            labelSuffixe.setForeground(couleurSuffixe);
            chip.setTexte(chipTexte);
            labelDroite.setText(droiteTexte);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(accent);
            g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            g2.dispose();
        }
    }

    /** Boite d'icone carree 32x32 (fond clair + bordure). */
    private static class BoiteIcone extends JPanel {

        private final Icone icone;

        BoiteIcone(Icone.Type type, int tailleIcone, Color couleur) {
            this.icone = new Icone(type, tailleIcone, couleur);
            setOpaque(false);
            setPreferredSize(new Dimension(32, 32));
            setMinimumSize(new Dimension(32, 32));
            setMaximumSize(new Dimension(32, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARTE_ENTETE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(BORDURE);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            icone.paintIcon(this, g2, (getWidth() - icone.getIconWidth()) / 2,
                    (getHeight() - icone.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    /** Etiquette d'etat (chip) : fond/bordure, point ou icone. */
    private static class Chip extends JPanel {

        private final Color fond;
        private final Color bordure;
        private final JLabel texte;

        Chip(String libelle, Color fond, Color bordure, Color couleurTexte,
                Color point, Icone.Type typeIcone) {
            this.fond = fond;
            this.bordure = bordure;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            if (point != null) {
                add(new PointColor(point, 6));
            } else if (typeIcone != null) {
                add(new JLabel(new Icone(typeIcone, 13, couleurTexte)));
            }
            texte = new JLabel(libelle);
            texte.setFont(police(Font.BOLD, 11));
            texte.setForeground(couleurTexte);
            add(texte);
        }

        void setTexte(String libelle) {
            texte.setText(libelle);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fond);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(bordure);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
        }
    }

    /** Carre plein (legende du graphique). */
    private static class CarrePlein extends JPanel {

        private final Color couleur;
        private final int taille;

        CarrePlein(Color couleur, int taille) {
            this.couleur = couleur;
            this.taille = taille;
            setOpaque(false);
            setPreferredSize(new Dimension(taille, taille));
            setMinimumSize(new Dimension(taille, taille));
            setMaximumSize(new Dimension(taille, taille));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(couleur);
            g2.fillRect(0, 0, taille, taille);
            g2.dispose();
        }
    }

    /** Pastille ronde pleine (etat en ligne, indicateurs). */
    private static class PointColor extends JPanel {

        private final Color couleur;
        private final int taille;

        PointColor(Color couleur, int taille) {
            this.couleur = couleur;
            this.taille = taille;
            setOpaque(false);
            setPreferredSize(new Dimension(taille, taille));
            setMinimumSize(new Dimension(taille, taille));
            setMaximumSize(new Dimension(taille, taille));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(couleur);
            g2.fillOval(0, 0, taille, taille);
            g2.dispose();
        }
    }

    /** Carre logo du menu (fond MENU_ACTIF + local_parking blanc). */
    private static class CarreLogo extends JPanel {

        CarreLogo() {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(MENU_ACTIF);
            g2.fillRect(0, 0, getWidth(), getHeight());
            new Icone(Icone.Type.PARKING, 22, MENU_TEXTE_ACTIF)
                    .paintIcon(this, g2, (getWidth() - 22) / 2, (getHeight() - 22) / 2);
            g2.dispose();
        }
    }

    /** Bouton notifications (w-9 h-9, rounded-lg, pastille bleue). */
    private static class BoutonNotifications extends JPanel {

        BoutonNotifications() {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARTE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(BORDURE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            new Icone(Icone.Type.CLOCHE, 20, TEXTE_DOUX)
                    .paintIcon(this, g2, (getWidth() - 20) / 2, (getHeight() - 20) / 2);
            g2.setColor(PRIMAIRE);
            g2.fillOval(getWidth() - 9, 3, 6, 6);
            g2.dispose();
        }
    }

    /** Pastille ronde contenant une icone vectorielle (avatar). */
    private static class PastilleIcone extends JPanel {

        private final Icone icone;
        private final Color fond;

        PastilleIcone(Icone.Type type, int tailleIcone, Color fond, Color couleur) {
            this.icone = new Icone(type, tailleIcone, couleur);
            this.fond = fond;
            setOpaque(false);
            setPreferredSize(new Dimension(tailleIcone + 12, tailleIcone + 12));
            setMinimumSize(new Dimension(tailleIcone + 12, tailleIcone + 12));
            setMaximumSize(new Dimension(tailleIcone + 12, tailleIcone + 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fond);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(BORDURE);
            g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            icone.paintIcon(this, g2, (getWidth() - icone.getIconWidth()) / 2,
                    (getHeight() - icone.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    /** Pastille utilisateur (initiale blanche dans un cercle MENU_ACTIF). */
    private static class PastilleUtilisateur extends JPanel {

        private String initiale;

        PastilleUtilisateur(String initiale) {
            this.initiale = initiale;
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
        }

        void setInitiale(String initiale) {
            this.initiale = initiale;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int c = getWidth() / 2;
            g2.setColor(MENU_ACTIF);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setFont(police(Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int yBase = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            int x = c - fm.stringWidth(initiale) / 2;
            g2.setColor(MENU_TEXTE_ACTIF);
            g2.drawString(initiale, x, yBase);
            g2.dispose();
        }
    }

    /** Bouton clair du journal (fond blanc, bordure, chevron). */
    private static class BoutonPlein extends JButton {

        BoutonPlein(String libelle) {
            super(libelle);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, 12));
            setPreferredSize(new Dimension(10, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? FOND : CARTE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(BORDURE);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String t = getText();
            int largeur = fm.stringWidth(t) + 6 + 16;
            int x = (getWidth() - largeur) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(TEXTE);
            g2.drawString(t, x, y);
            new Icone(Icone.Type.CHEVRON, 16, TEXTE)
                    .paintIcon(this, g2, x + fm.stringWidth(t) + 6, (getHeight() - 16) / 2);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // GRAPHIQUE (barres vectorielles, antialiasing)
    // ---------------------------------------------------------------------
    private static class PanneauGraphique extends JPanel {

        private String[] jours = new String[0];
        private double[] montants = new double[0];
        private double maximum = 1;
        private double somme;
        private double pic;
        private String picJour = "-";
        private double moyenne;

        PanneauGraphique() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 320));
        }

        void setDonnees(String[][] lignes) {
            int n = lignes != null ? lignes.length : 0;
            if (n > 7) {
                n = 7;
            }
            jours = new String[n];
            montants = new double[n];
            somme = 0;
            pic = 0;
            picJour = "-";
            for (int i = 0; i < n; i++) {
                jours[i] = lignes[i][0];
                double m = 0;
                try {
                    m = Double.parseDouble(lignes[i][1]);
                } catch (Exception e) {
                    m = 0;
                }
                montants[i] = m;
                somme += m;
                if (m > pic) {
                    pic = m;
                    picJour = jours[i];
                }
            }
            maximum = arrondiMax(pic);
            moyenne = n > 0 ? somme / n : 0;
            repaint();
        }

        double getSomme() {
            return somme;
        }

        double getMoyenne() {
            return moyenne;
        }

        double getPic() {
            return pic;
        }

        String getPicJour() {
            return picJour;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int axe = 112;
            int bas = h - 22;
            int gauche = axe + 4;
            int plotW = Math.max(10, w - gauche);

            // Grille horizontale (6 lignes).
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 5; i++) {
                int y = bas - bas * i / 5;
                g2.setColor(BORDURE);
                g2.drawLine(gauche, y, w, y);
            }

            // Etiquettes de l'axe Y.
            g2.setFont(police(Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i <= 5; i++) {
                int y = bas - bas * i / 5;
                String lab = nombre(maximum * i / 5.0) + " Ar";
                g2.setColor(TEXTE_DOUX);
                g2.drawString(lab, gauche - 12 - fm.stringWidth(lab),
                        y + fm.getAscent() / 2 - 1);
            }

            if (jours.length == 0) {
                g2.setColor(TEXTE_DOUX);
                g2.setFont(police(Font.PLAIN, 14));
                FontMetrics fm2 = g2.getFontMetrics();
                String msg = "Aucune recette enregistree";
                g2.drawString(msg, gauche + (plotW - fm2.stringWidth(msg)) / 2,
                        bas - bas / 2);
                g2.dispose();
                return;
            }

            int n = jours.length;
            double slot = plotW / (double) n;
            int largeurBarre = (int) Math.max(14, Math.min(46, slot * 0.5));
            for (int i = 0; i < n; i++) {
                int centre = (int) (gauche + slot * (i + 0.5));
                int x = centre - largeurBarre / 2;
                int hauteur = (int) Math.round(bas * (montants[i] / maximum));
                if (hauteur < 2) {
                    hauteur = 2;
                }
                int y = bas - hauteur;
                boolean aujourdhui = (i == n - 1);

                g2.setColor(PRIMAIRE);
                g2.fillRect(x, y, largeurBarre, hauteur);
                if (aujourdhui) {
                    g2.setColor(SUCCES);
                    g2.fillRect(x, y, largeurBarre, 4);
                }

                g2.setFont(police(Font.BOLD, 10));
                FontMetrics fc = g2.getFontMetrics();
                String val = compact(montants[i]);
                g2.setColor(PRIMAIRE_FONCE);
                g2.drawString(val, centre - fc.stringWidth(val) / 2, Math.max(11, y - 4));

                g2.setFont(police(aujourdhui ? Font.BOLD : Font.PLAIN, 12));
                FontMetrics fx = g2.getFontMetrics();
                String lab = aujourdhui ? jours[i] + " (J)" : jours[i];
                g2.setColor(aujourdhui ? SUCCES : TEXTE_DOUX);
                g2.drawString(lab, centre - fx.stringWidth(lab) / 2, h - 6);
            }
            g2.dispose();
        }

        private static double arrondiMax(double v) {
            if (v <= 0) {
                return 1;
            }
            double exp = Math.pow(10, Math.floor(Math.log10(v)));
            double f = v / exp;
            double nice;
            if (f <= 1) {
                nice = 1;
            } else if (f <= 2) {
                nice = 2;
            } else if (f <= 2.5) {
                nice = 2.5;
            } else if (f <= 5) {
                nice = 5;
            } else {
                nice = 10;
            }
            return nice * exp;
        }
    }

    // ---------------------------------------------------------------------
    // JOURNAL DES MOUVEMENTS (stream)
    // ---------------------------------------------------------------------
    private static class PanneauJournal extends JPanel {

        private final JPanel liste;

        PanneauJournal(Runnable versHistorique) {
            setOpaque(true);
            setBackground(CARTE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(BORDURE, 1));

            // En-tete (h-12 = 48).
            JPanel entete = new JPanel(new BorderLayout());
            entete.setOpaque(true);
            entete.setBackground(CARTE_ENTETE);
            entete.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDURE),
                    BorderFactory.createEmptyBorder(0, 16, 0, 16)));
            entete.setPreferredSize(new Dimension(10, 48));

            JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            gauche.setOpaque(false);
            gauche.add(new JLabel(new Icone(Icone.Type.LISTE, 18, PRIMAIRE)));
            JLabel titre = new JLabel("Derniers mouvements");
            titre.setFont(police(Font.BOLD, 16));
            titre.setForeground(TEXTE);
            gauche.add(titre);
            entete.add(gauche, BorderLayout.WEST);

            JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            droite.setOpaque(false);
            droite.add(new PointColor(SUCCES, 8));
            entete.add(droite, BorderLayout.EAST);
            add(entete, BorderLayout.NORTH);

            liste = new JPanel();
            liste.setOpaque(false);
            liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));
            liste.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JScrollPane scroll = new JScrollPane(liste);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.CENTER);

            JPanel pied = new JPanel(new BorderLayout());
            pied.setOpaque(true);
            pied.setBackground(FOND);
            pied.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, BORDURE),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
            BoutonPlein bouton = new BoutonPlein("Consulter le journal complet");
            bouton.addActionListener(e -> versHistorique.run());
            pied.add(bouton, BorderLayout.CENTER);
            add(pied, BorderLayout.SOUTH);
        }

        void setDonnees(List<Stationnement> donnees) {
            liste.removeAll();
            if (donnees == null || donnees.isEmpty()) {
                JLabel vide = new JLabel("Aucun mouvement recent");
                vide.setFont(police(Font.PLAIN, 12));
                vide.setForeground(TEXTE_DOUX);
                vide.setAlignmentX(Component.LEFT_ALIGNMENT);
                liste.add(vide);
            } else {
                int n = Math.min(4, donnees.size());
                for (int i = 0; i < n; i++) {
                    liste.add(creerEvenement(donnees.get(i)));
                    if (i < n - 1) {
                        liste.add(Box.createVerticalStrut(8));
                    }
                }
            }
            liste.revalidate();
            liste.repaint();
        }

        private static JPanel creerEvenement(Stationnement s) {
            boolean sortie = "TERMINE".equalsIgnoreCase(s.getStatut());
            Color accent = sortie ? SUCCES : PRIMAIRE;

            JPanel ev = new JPanel();
            ev.setOpaque(true);
            ev.setBackground(CARTE_ENTETE);
            ev.setLayout(new BoxLayout(ev, BoxLayout.Y_AXIS));
            ev.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 2, 0, 0, accent),
                    BorderFactory.createEmptyBorder(8, 10, 8, 8)));
            ev.setAlignmentX(Component.LEFT_ALIGNMENT);
            ev.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

            JPanel r1 = new JPanel(new BorderLayout());
            r1.setOpaque(false);
            r1.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel h = new JLabel(heure(s.getDateEntree()));
            h.setFont(police(Font.BOLD, 11));
            h.setForeground(TEXTE);
            JLabel statut = new JLabel(sortie ? "SORTIE VALIDEE" : "ENTREE ADMISE");
            statut.setFont(police(Font.BOLD, 11));
            statut.setForeground(accent);
            r1.add(h, BorderLayout.WEST);
            r1.add(statut, BorderLayout.EAST);
            ev.add(r1);
            ev.add(Box.createVerticalStrut(4));

            JPanel r2 = new JPanel(new BorderLayout());
            r2.setOpaque(false);
            r2.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel mat = new JLabel("Matr : " + valeurOu(s.getImmatriculation(), "-"));
            mat.setFont(police(Font.PLAIN, 12));
            mat.setForeground(TEXTE);
            JLabel val = new JLabel();
            val.setFont(police(Font.BOLD, 11));
            if (sortie) {
                val.setText(nombre(s.getMontant()) + " Ar");
                val.setForeground(PRIMAIRE);
            } else {
                val.setText("TKT #" + s.getIdStationnement());
                val.setForeground(TEXTE_DOUX);
            }
            r2.add(mat, BorderLayout.WEST);
            r2.add(val, BorderLayout.EAST);
            ev.add(r2);
            ev.add(Box.createVerticalStrut(4));

            JLabel detail = new JLabel();
            if (sortie) {
                detail.setText("Duree : " + duree(s.getDuree())
                        + " - Place " + valeurOu(s.getNumeroPlace(), "-"));
            } else {
                detail.setText("Attribue : Place " + valeurOu(s.getNumeroPlace(), "-"));
            }
            detail.setFont(police(Font.PLAIN, 11));
            detail.setForeground(TEXTE_DOUX);
            detail.setAlignmentX(Component.LEFT_ALIGNMENT);
            ev.add(detail);
            return ev;
        }
    }
}
