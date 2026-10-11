package view;

import dao.UtilisateurDAO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Utilisateur;

/**
 * Page Parametres : profil, securite (mot de passe), tarif (gestion des prix)
 * et informations sur l'application.
 *
 * Coquille conforme a {@code reference/Parametre_model1/code.html}
 * (bandeau superieur : meta "CONFIGURATION // SYS-ENV", chips en lecture
 * seule, titre display-lg 32px, onglets horizontaux label-md avec indicateur
 * 2px ; contenu en grille 8/4 avec articles "boite icone 40px + headline-md
 * 16px + chips mono" et aside "Apercu Theme Swing" + "Controle d'acces &
 * Droits"). Toute la logique metier (profil, mot de passe, tarifs, a propos)
 * est conservee a l'identique.
 */
public class ParametreForm extends JPanel {

    // ---------------------------------------------------------------------
    // PALETTE DE COULEURS
    // ---------------------------------------------------------------------
    private static final Color FOND             = new Color(0xF1F5F9);
    private static final Color CARTE            = new Color(0xFFFFFF);
    private static final Color BORDURE          = new Color(0xE2E8F0);
    private static final Color BANDE            = new Color(0xF8FAFC);
    private static final Color CHIP_FOND        = new Color(0xE2E8F0);
    private static final Color OUTLINE          = new Color(0x94A3B8);
    private static final Color PRIMAIRE         = new Color(0x2563EB);
    private static final Color PRIMAIRE_HOVER   = new Color(0x1D4ED8);
    private static final Color PRIMAIRE_PRESSED = new Color(0x1E40AF);
    private static final Color TEXTE            = new Color(0x0F172A);
    private static final Color TEXTE_SECONDAIRE = new Color(0x64748B);
    private static final Color LABEL            = new Color(0x334155);
    private static final Color BORDURE_CHAMP    = new Color(0xCBD5E1);
    private static final Color SELECTION        = new Color(0xDBEAFE);
    private static final Color LIGNE_ALTERNEE   = new Color(0xF8FAFC);
    private static final Color VERT             = new Color(0x16A34A);
    private static final Color VERT_FOND        = new Color(0xDCFCE7);
    private static final Color BLEU_FOND        = new Color(0xEFF6FF);
    private static final Color BLEU_BORDURE     = new Color(0xBFDBFE);
    private static final Color PASTILLE_TEXTE   = new Color(0x1D4ED8);
    private static final Color PASTILLE_MODULE  = new Color(0xF1F5F9);
    private static final Color ROUGE            = new Color(0xDC2626);
    private static final Color ORANGE           = new Color(0xEA580C);

    private final Utilisateur utilisateur;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private final CardLayout cartes = new CardLayout();
    private final JPanel contenu = new JPanel(cartes);
    private final Map<String, Onglet> boutonsNav = new LinkedHashMap<>();

    // Profil
    private final Avatar avatar;
    private final JLabel lblNomAffiche = new JLabel();
    private final JTextField txtNom = new JTextField();

    // Securite
    private final JPasswordField pwActuel = new JPasswordField();
    private final JPasswordField pwNouveau = new JPasswordField();
    private final JPasswordField pwConfirmation = new JPasswordField();
    private final ForceMotDePasse force = new ForceMotDePasse();

    private Runnable surProfilModifie;

    public ParametreForm(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.avatar = new Avatar(utilisateur != null ? utilisateur.getNom() : null);

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(FOND);

        add(creerEnTete(), BorderLayout.NORTH);

        contenu.setOpaque(false);
        contenu.add(creerPageProfil(), "profil");
        contenu.add(creerPageSecurite(), "securite");
        contenu.add(creerPageTarifs(), "tarifs");
        contenu.add(creerPageApropos(), "apropos");

        JPanel corps = new JPanel(new BorderLayout(16, 0));
        corps.setOpaque(false);
        corps.add(contenu, BorderLayout.CENTER);
        corps.add(creerAside(), BorderLayout.EAST);
        add(corps, BorderLayout.CENTER);

        afficherSection("profil");
    }

    /** Appele apres une modification reussie du nom (ex. rafraichir le menu du Dashboard). */
    public void setSurProfilModifie(Runnable action) {
        this.surProfilModifie = action;
    }

    // ---------------------------------------------------------------------
    // EN-TETE : COQUILLE MODELE 1
    // ---------------------------------------------------------------------
    private JPanel creerEnTete() {
        JPanel zone = new JPanel(new BorderLayout());
        zone.setOpaque(false);
        zone.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel bande = new JPanel(new BorderLayout(16, 0));
        bande.setOpaque(true);
        bande.setBackground(BANDE);
        bande.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 4, 1),
                BorderFactory.createEmptyBorder(16, 24, 0, 24)));

        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JPanel ligneMeta = new JPanel(new BorderLayout(12, 0));
        ligneMeta.setOpaque(false);
        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        gauche.setOpaque(false);
        JLabel fil = new JLabel("CONFIGURATION // SYS-ENV");
        fil.setFont(police(Font.BOLD, 10));
        fil.setForeground(TEXTE_SECONDAIRE);
        gauche.add(fil);
        gauche.add(new PastilleDot(OUTLINE, 4));
        JLabel profil = new JLabel("NODE_PROFILE: PROD-NORTH");
        profil.setFont(policeMono(Font.BOLD, 10));
        profil.setForeground(VERT);
        gauche.add(profil);
        ligneMeta.add(gauche, BorderLayout.WEST);

        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setOpaque(true);
        chip.setBackground(CHIP_FOND);
        chip.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        chip.add(new JLabel(new Icone(Icone.Type.CADENAS, 14, PRIMAIRE)));
        JLabel lect = new JLabel("POLITIQUE GLOBALE EN LECTURE SEULE");
        lect.setFont(policeMono(Font.PLAIN, 9));
        lect.setForeground(LABEL);
        chip.add(lect);
        ligneMeta.add(chip, BorderLayout.EAST);

        g.gridy = 0;
        colonne.add(ligneMeta, g);

        JLabel titre = new JLabel("Paramètres");
        titre.setFont(police(Font.BOLD, 32));
        titre.setForeground(TEXTE);
        g.gridy = 1;
        g.insets = new Insets(8, 0, 0, 0);
        colonne.add(titre, g);

        JLabel sousTitre = new JLabel(
                "Consultez les informations de votre compte et les paramètres de votre espace SmartPark.");
        sousTitre.setFont(police(Font.PLAIN, 13));
        sousTitre.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 2;
        g.insets = new Insets(2, 0, 0, 0);
        colonne.add(sousTitre, g);

        bande.add(colonne, BorderLayout.CENTER);

        JPanel nav = creerNavigation();
        nav.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        bande.add(nav, BorderLayout.SOUTH);

        zone.add(bande, BorderLayout.CENTER);
        return zone;
    }

    /** Onglets horizontaux de section (coquille modele 1). */
    private JPanel creerNavigation() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        nav.setOpaque(false);
        ajouterNav(nav, "Mon compte", Icone.Type.UTILISATEUR, "profil");
        ajouterNav(nav, "Sécurité", Icone.Type.CADENAS, "securite");
        ajouterNav(nav, "Tarif", Icone.Type.TARIF, "tarifs");
        ajouterNav(nav, "Informations de l'application", Icone.Type.LISTE, "apropos");
        return nav;
    }

    private void ajouterNav(JPanel nav, String texte, Icone.Type icone, String cle) {
        Onglet b = new Onglet(texte, icone);
        b.addActionListener(e -> afficherSection(cle));
        boutonsNav.put(cle, b);
        nav.add(b);
    }

    private void afficherSection(String cle) {
        cartes.show(contenu, cle);
        for (Map.Entry<String, Onglet> e : boutonsNav.entrySet()) {
            e.getValue().setActif(e.getKey().equals(cle));
        }
    }

    // ---------------------------------------------------------------------
    // ASIDE : APERCU THEME SWING + CONTROLE D'ACCES
    // ---------------------------------------------------------------------
    private JComponent creerAside() {
        JPanel aside = new JPanel();
        aside.setLayout(new BoxLayout(aside, BoxLayout.Y_AXIS));
        aside.setOpaque(false);
        aside.setPreferredSize(new Dimension(348, 10));

        aside.add(carteApercuTheme());
        aside.add(Box.createVerticalStrut(16));
        aside.add(carteControleAcces());
        aside.add(Box.createVerticalGlue());
        return aside;
    }

    private JPanel carteApercuTheme() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(BANDE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 4, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JPanel enTete = new JPanel(new BorderLayout());
        enTete.setOpaque(false);
        JLabel t = new JLabel("APERÇU THÈME SWING");
        t.setFont(police(Font.BOLD, 10));
        t.setForeground(TEXTE);
        enTete.add(t, BorderLayout.WEST);
        JLabel r = new JLabel("RENDER_CANVAS");
        r.setFont(policeMono(Font.PLAIN, 9));
        r.setForeground(OUTLINE);
        enTete.add(r, BorderLayout.EAST);
        g.gridy = 0;
        carte.add(enTete, g);

        JPanel rendu = new JPanel(new GridBagLayout());
        rendu.setOpaque(true);
        rendu.setBackground(FOND);
        rendu.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        GridBagConstraints gr = new GridBagConstraints();
        gr.gridx = 0;
        gr.weightx = 1;
        gr.fill = GridBagConstraints.HORIZONTAL;
        gr.anchor = GridBagConstraints.WEST;

        JPanel barreTitre = new JPanel(new BorderLayout(8, 0));
        barreTitre.setOpaque(true);
        barreTitre.setBackground(CHIP_FOND);
        barreTitre.setPreferredSize(new Dimension(10, 24));
        JPanel points = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        points.setOpaque(false);
        points.add(new PastilleDot(ROUGE, 8));
        points.add(new PastilleDot(OUTLINE, 8));
        points.add(new PastilleDot(VERT, 8));
        barreTitre.add(points, BorderLayout.WEST);
        JLabel jar = new JLabel("SMARTPARK_CLIENT.JAR");
        jar.setFont(policeMono(Font.PLAIN, 9));
        jar.setForeground(OUTLINE);
        barreTitre.add(jar, BorderLayout.EAST);
        gr.gridy = 0;
        rendu.add(barreTitre, gr);

        JPanel carteBlanche = new JPanel(new GridBagLayout());
        carteBlanche.setOpaque(true);
        carteBlanche.setBackground(new Color(0xF4F6FA));
        carteBlanche.setBorder(new RoundedBorder(CHIP_FOND, 2, 1));
        carteBlanche.setPreferredSize(new Dimension(10, 92));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        JPanel rang1 = new JPanel(new BorderLayout(8, 0));
        rang1.setOpaque(false);
        JLabel jf = new JLabel("[JFrame] Entrée Nord - Voie 01");
        jf.setFont(police(Font.PLAIN, 11));
        jf.setForeground(LABEL);
        rang1.add(jf, BorderLayout.WEST);
        JLabel laf = new JLabel("FLATLAF_LIGHT");
        laf.setFont(policeMono(Font.PLAIN, 9));
        laf.setForeground(TEXTE_SECONDAIRE);
        laf.setOpaque(true);
        laf.setBackground(CHIP_FOND);
        laf.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        rang1.add(laf, BorderLayout.EAST);
        gc.gridy = 0;
        carteBlanche.add(rang1, gc);

        JPanel rang2 = new JPanel(new BorderLayout(8, 0));
        rang2.setOpaque(true);
        rang2.setBackground(CARTE);
        rang2.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        JPanel g2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        g2.setOpaque(false);
        g2.add(new JLabel(new Icone(Icone.Type.TARIF, 16, PRIMAIRE)));
        JLabel ts = new JLabel("Tarif Standard 2H");
        ts.setFont(police(Font.BOLD, 11));
        ts.setForeground(TEXTE);
        g2.add(ts);
        rang2.add(g2, BorderLayout.WEST);
        JLabel prix = new JLabel("3 000 Ar");
        prix.setFont(policeMono(Font.BOLD, 11));
        prix.setForeground(VERT);
        prix.setOpaque(true);
        prix.setBackground(VERT_FOND);
        prix.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        rang2.add(prix, BorderLayout.EAST);
        gc.gridy = 1;
        gc.insets = new Insets(6, 0, 0, 0);
        carteBlanche.add(rang2, gc);

        JPanel rang3 = new JPanel(new BorderLayout(8, 0));
        rang3.setOpaque(false);
        JLabel bar = new JLabel("Barrière : Fermée");
        bar.setFont(police(Font.PLAIN, 10));
        bar.setForeground(TEXTE_SECONDAIRE);
        rang3.add(bar, BorderLayout.WEST);
        JLabel pret = new JLabel("Pret pour impression");
        pret.setFont(police(Font.BOLD, 10));
        pret.setForeground(VERT);
        rang3.add(pret, BorderLayout.EAST);
        gc.gridy = 2;
        gc.insets = new Insets(8, 4, 0, 4);
        carteBlanche.add(rang3, gc);

        gr.gridy = 1;
        gr.insets = new Insets(8, 0, 0, 0);
        rendu.add(carteBlanche, gr);

        JPanel basRendu = new JPanel(new BorderLayout(8, 0));
        basRendu.setOpaque(false);
        JLabel dpi = new JLabel("DPI Scaling: 100%");
        dpi.setFont(policeMono(Font.PLAIN, 9));
        dpi.setForeground(OUTLINE);
        basRendu.add(dpi, BorderLayout.WEST);
        JLabel lafOk = new JLabel("Swing LaF: OK");
        lafOk.setFont(policeMono(Font.PLAIN, 9));
        lafOk.setForeground(OUTLINE);
        basRendu.add(lafOk, BorderLayout.EAST);
        gr.gridy = 2;
        gr.insets = new Insets(8, 0, 0, 0);
        rendu.add(basRendu, gr);

        g.gridy = 1;
        g.insets = new Insets(12, 0, 0, 0);
        carte.add(rendu, g);

        JLabel met = new JLabel("MÉTRIQUES D'AFFICHAGE POSTE");
        met.setFont(police(Font.BOLD, 10));
        met.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 2;
        g.insets = new Insets(12, 0, 0, 0);
        carte.add(met, g);

        JPanel metriques = new JPanel(new GridLayout(1, 2, 8, 0));
        metriques.setOpaque(false);
        metriques.add(tuileMetrique("RÉSOLUTION", "1440 × 900", false));
        metriques.add(tuileMetrique("TAUX RAFRAÎCH.", "60 Hz", true));
        g.gridy = 3;
        g.insets = new Insets(6, 0, 0, 0);
        carte.add(metriques, g);
        return carte;
    }

    private JPanel tuileMetrique(String libelle, String valeur, boolean vert) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(CARTE);
        p.setBorder(new RoundedBorder(BORDURE, 2, 1));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(libelle);
        l.setFont(police(Font.PLAIN, 9));
        l.setForeground(OUTLINE);
        g.gridy = 0;
        g.insets = new Insets(4, 8, 0, 8);
        p.add(l, g);
        JLabel v = new JLabel(valeur);
        v.setFont(policeMono(Font.BOLD, 12));
        v.setForeground(vert ? VERT : TEXTE);
        g.gridy = 1;
        g.insets = new Insets(2, 8, 4, 8);
        p.add(v, g);
        return p;
    }

    private JPanel carteControleAcces() {
        JPanel carte = new JPanel(new GridBagLayout());
        carte.setOpaque(true);
        carte.setBackground(BANDE);
        carte.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 4, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JPanel enTete = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        enTete.setOpaque(false);
        enTete.add(new JLabel(new Icone(Icone.Type.CADENAS, 18, PRIMAIRE)));
        JLabel t = new JLabel("CONTRÔLE D'ACCÈS & DROITS");
        t.setFont(police(Font.BOLD, 10));
        t.setForeground(TEXTE);
        enTete.add(t);
        g.gridy = 0;
        carte.add(enTete, g);

        JLabel texte = new JLabel("<html>Le profil actif est rattaché au rôle "
                + "<b>Opérateur Guichet</b>. Toute modification du thème FlatLaf, du symbole "
                + "monétaire (Ar) ou des dictionnaires régionaux nécessite des privilèges "
                + "<font color=\"#2563EB\">SYS_ADMIN</font> via le fichier de configuration "
                + "centralisé.</html>");
        texte.setFont(police(Font.PLAIN, 11));
        texte.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 1;
        g.insets = new Insets(10, 0, 0, 0);
        carte.add(texte, g);

        JPanel config = new JPanel(new BorderLayout(8, 0));
        config.setOpaque(true);
        config.setBackground(CARTE);
        config.setBorder(new RoundedBorder(BORDURE, 2, 1));
        JLabel id = new JLabel("CONFIG_ID: GCH-04-MG");
        id.setFont(policeMono(Font.PLAIN, 9));
        id.setForeground(OUTLINE);
        config.add(id, BorderLayout.WEST);
        JLabel verifie = new JLabel("VERIFIÉ");
        verifie.setFont(policeMono(Font.PLAIN, 9));
        verifie.setForeground(VERT);
        config.add(verifie, BorderLayout.EAST);
        g.gridy = 2;
        g.insets = new Insets(12, 0, 0, 0);
        carte.add(config, g);
        return carte;
    }

    // ---------------------------------------------------------------------
    // PAGE PROFIL (MON COMPTE)
    // ---------------------------------------------------------------------
    private JComponent creerPageProfil() {
        String nom = utilisateur != null ? valeur(utilisateur.getNom()) : "-";
        String role = utilisateur != null ? valeur(utilisateur.getRole()) : "-";
        String email = utilisateur != null ? valeur(utilisateur.getEmail()) : "-";

        Carte carte = new Carte(Icone.Type.UTILISATEUR,
                "Mon compte", "Identité et coordonnées du compte connecté", chipStatutSession());

        JPanel identite = new JPanel(new BorderLayout(16, 0));
        identite.setOpaque(false);
        identite.add(avatar, BorderLayout.WEST);

        JPanel infos = new JPanel();
        infos.setOpaque(false);
        infos.setLayout(new BoxLayout(infos, BoxLayout.Y_AXIS));
        lblNomAffiche.setText(nom);
        lblNomAffiche.setFont(police(Font.BOLD, 16));
        lblNomAffiche.setForeground(TEXTE);
        lblNomAffiche.setAlignmentX(Component.LEFT_ALIGNMENT);
        infos.add(Box.createVerticalGlue());
        infos.add(lblNomAffiche);
        infos.add(Box.createVerticalStrut(6));
        JPanel rang = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rang.setOpaque(false);
        if (!role.equals("-")) {
            rang.add(new Pastille(role));
        }
        infos.add(rang);
        infos.add(Box.createVerticalStrut(4));
        JLabel lblEmail = new JLabel(email);
        lblEmail.setFont(police(Font.PLAIN, 13));
        lblEmail.setForeground(TEXTE_SECONDAIRE);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        infos.add(lblEmail);
        infos.add(Box.createVerticalGlue());
        identite.add(infos, BorderLayout.CENTER);
        carte.ajouter(articleRang(identite), 18);

        carte.ajouter(section("INFORMATIONS DU COMPTE"), 18);
        JPanel grille = new JPanel(new GridLayout(0, 2, 10, 10));
        grille.setOpaque(false);
        grille.add(new TuileChamp("Nom & Prénom", nom, Icone.Type.UTILISATEUR));
        grille.add(new TuileChamp("Adresse e-mail", email, Icone.Type.EMAIL));
        grille.add(new TuileChamp("Rôle assigné", role, Icone.Type.CADENAS));
        carte.ajouter(grille, 10);

        carte.ajouter(bandeauInfo("Information de profil",
                "La gestion centralisée des accès s'effectue via la console de sécurité PostgreSQL."), 14);

        carte.ajouter(section("MODIFIER LE NOM AFFICHÉ"), 18);
        styleChamp(txtNom);
        txtNom.setText(utilisateur != null && utilisateur.getNom() != null ? utilisateur.getNom() : "");
        txtNom.addActionListener(e -> enregistrerNom());
        carte.ajouter(txtNom, 8);

        JButton btn = new BoutonStyle("Enregistrer", BoutonStyle.Type.PRIMAIRE);
        btn.addActionListener(e -> enregistrerNom());
        btn.setEnabled(utilisateur != null);
        carte.ajouter(alignerDroite(btn), 12);

        Carte pref = new Carte(Icone.Type.PARAMETRE,
                "Préférences d'affichage",
                "Aperçu des paramètres d'interface et d'affichage actuellement appliqués à votre session.",
                chipSession());
        pref.ajouter(articlePrefTheme(), 16);
        pref.ajouter(articlePrefLangue(), 14);
        pref.ajouter(articlePrefDevise(), 14);

        JPanel aide = new JPanel(new BorderLayout(10, 0));
        aide.setOpaque(true);
        aide.setBackground(FOND);
        aide.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        aide.add(new JLabel(new Icone(Icone.Type.LISTE, 20, TEXTE_SECONDAIRE)), BorderLayout.WEST);
        JLabel aideTexte = new JLabel("Ces préférences reflètent la configuration système standard "
                + "de l'application desktop SmartPark (non modifiables en session opérateur).");
        aideTexte.setFont(police(Font.PLAIN, 11));
        aideTexte.setForeground(TEXTE);
        aide.add(aideTexte, BorderLayout.CENTER);
        pref.ajouter(aide, 18);

        return page(carte, pref);
    }

    private void enregistrerNom() {
        if (utilisateur == null) {
            return;
        }
        String nouveau = txtNom.getText().trim();
        if (nouveau.length() < 2 || nouveau.length() > 50) {
            JOptionPane.showMessageDialog(this, "Le nom doit contenir entre 2 et 50 caracteres.");
            return;
        }
        if (utilisateurDAO.modifierNom(utilisateur.getIdUtilisateur(), nouveau)) {
            utilisateur.setNom(nouveau);
            lblNomAffiche.setText(nouveau);
            avatar.setNom(nouveau);
            JOptionPane.showMessageDialog(this, "Nom modifie avec succes.");
            if (surProfilModifie != null) {
                surProfilModifie.run();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de la modification du nom.");
        }
    }

    private JPanel articlePrefTheme() {
        JPanel gauche = new JPanel(new BorderLayout(12, 0));
        gauche.setOpaque(false);
        gauche.add(new BoiteIcone(Icone.Type.PARAMETRE), BorderLayout.WEST);
        JPanel infos = new JPanel(new GridBagLayout());
        infos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JPanel titres = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titres.setOpaque(false);
        JLabel t = new JLabel("Thème de l'interface");
        t.setFont(police(Font.BOLD, 16));
        t.setForeground(TEXTE);
        titres.add(t);
        titres.add(vignette("Standard Java Swing"));
        g.gridy = 0;
        infos.add(titres, g);
        JLabel d = new JLabel("Apparence visuelle de la console de gestion.");
        d.setFont(police(Font.PLAIN, 11));
        d.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 1;
        g.insets = new Insets(4, 0, 0, 0);
        infos.add(d, g);
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        meta.setOpaque(false);
        JLabel pf = new JLabel("Profil moteur :");
        pf.setFont(police(Font.PLAIN, 11));
        pf.setForeground(OUTLINE);
        meta.add(pf);
        JLabel flat = new JLabel("com.formdev.flatlaf.FlatLightLaf");
        flat.setFont(policeMono(Font.PLAIN, 9));
        flat.setForeground(TEXTE);
        flat.setOpaque(true);
        flat.setBackground(CHIP_FOND);
        flat.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        meta.add(flat);
        g.gridy = 2;
        g.insets = new Insets(6, 0, 0, 0);
        infos.add(meta, g);
        gauche.add(infos, BorderLayout.CENTER);

        JPanel droite = new JPanel(new GridBagLayout());
        droite.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chip.setOpaque(true);
        chip.setBackground(CHIP_FOND);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        chip.add(new PastilleDot(VERT, 10));
        JLabel clair = new JLabel("Thème Clair (FlatLaf Light)");
        clair.setFont(police(Font.BOLD, 12));
        clair.setForeground(TEXTE);
        chip.add(clair);
        chip.setAlignmentX(Component.RIGHT_ALIGNMENT);
        c.gridy = 0;
        c.insets = new Insets(0, 8, 0, 0);
        droite.add(chip, c);
        JPanel mini = new JPanel(new BorderLayout());
        mini.setOpaque(false);
        JLabel lblMini = new JLabel(nonModifiable());
        lblMini.setFont(police(Font.PLAIN, 11));
        lblMini.setForeground(OUTLINE);
        mini.add(lblMini, BorderLayout.EAST);
        c.gridy = 1;
        c.insets = new Insets(4, 0, 0, 0);
        droite.add(mini, c);

        JPanel article = new JPanel(new BorderLayout(12, 0));
        article.setOpaque(true);
        article.setBackground(CARTE);
        article.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        article.add(gauche, BorderLayout.CENTER);
        article.add(droite, BorderLayout.EAST);
        return article;
    }

    private JPanel articlePrefLangue() {
        JPanel gauche = new JPanel(new BorderLayout(12, 0));
        gauche.setOpaque(false);
        gauche.add(new BoiteIcone(Icone.Type.EMAIL), BorderLayout.WEST);
        JPanel infos = new JPanel(new GridBagLayout());
        infos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JPanel titres = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titres.setOpaque(false);
        JLabel t = new JLabel("Langue de l'interface");
        t.setFont(police(Font.BOLD, 16));
        t.setForeground(TEXTE);
        titres.add(t);
        titres.add(vignette("Locale i18n"));
        g.gridy = 0;
        infos.add(titres, g);
        JLabel d = new JLabel("Langue utilisée pour l'ensemble des modules, formulaires et alertes.");
        d.setFont(police(Font.PLAIN, 11));
        d.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 1;
        g.insets = new Insets(4, 0, 0, 0);
        infos.add(d, g);
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        meta.setOpaque(false);
        JLabel enc = new JLabel("Encodage :");
        enc.setFont(police(Font.PLAIN, 11));
        enc.setForeground(OUTLINE);
        meta.add(enc);
        JLabel utf = new JLabel("UTF-8");
        utf.setFont(policeMono(Font.PLAIN, 9));
        utf.setForeground(TEXTE);
        meta.add(utf);
        JLabel sep = new JLabel("•");
        sep.setFont(police(Font.PLAIN, 11));
        sep.setForeground(OUTLINE);
        meta.add(sep);
        JLabel res = new JLabel("Fichier de ressources :");
        res.setFont(police(Font.PLAIN, 11));
        res.setForeground(OUTLINE);
        meta.add(res);
        JLabel props = new JLabel("Messages_fr_FR.properties");
        props.setFont(policeMono(Font.PLAIN, 9));
        props.setForeground(TEXTE);
        meta.add(props);
        g.gridy = 2;
        g.insets = new Insets(6, 0, 0, 0);
        infos.add(meta, g);
        gauche.add(infos, BorderLayout.CENTER);

        JPanel droite = new JPanel(new GridBagLayout());
        droite.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chip.setOpaque(true);
        chip.setBackground(CHIP_FOND);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        chip.add(new PastilleDot(PRIMAIRE, 10));
        JLabel fr = new JLabel("Français (FR)");
        fr.setFont(police(Font.BOLD, 12));
        fr.setForeground(TEXTE);
        chip.add(fr);
        c.gridy = 0;
        c.insets = new Insets(0, 8, 0, 0);
        droite.add(chip, c);
        JPanel mini = new JPanel(new BorderLayout());
        mini.setOpaque(false);
        c.gridy = 1;
        c.insets = new Insets(4, 0, 0, 0);
        droite.add(mini, c);
        JPanel conf = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        conf.setOpaque(false);
        conf.add(new JLabel(new Icone(Icone.Type.COCHE, 14, VERT)));
        JLabel lsc = new JLabel("LANGUE SYSTÈME CONFIGURÉE");
        lsc.setFont(police(Font.BOLD, 9));
        lsc.setForeground(VERT);
        conf.add(lsc);
        c.gridy = 2;
        c.insets = new Insets(4, 0, 0, 0);
        droite.add(conf, c);

        JPanel article = new JPanel(new BorderLayout(12, 0));
        article.setOpaque(true);
        article.setBackground(CARTE);
        article.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        article.add(gauche, BorderLayout.CENTER);
        article.add(droite, BorderLayout.EAST);
        return article;
    }

    private JPanel articlePrefDevise() {
        JPanel gauche = new JPanel(new BorderLayout(12, 0));
        gauche.setOpaque(false);
        gauche.add(new BoiteIcone(Icone.Type.TARIF), BorderLayout.WEST);
        JPanel infos = new JPanel(new GridBagLayout());
        infos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        JPanel titres = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titres.setOpaque(false);
        JLabel t = new JLabel("Format de devise et montants");
        t.setFont(police(Font.BOLD, 16));
        t.setForeground(TEXTE);
        titres.add(t);
        titres.add(vignette("Comptabilité Borne"));
        g.gridy = 0;
        infos.add(titres, g);
        JLabel d = new JLabel("Unité monétaire appliquée aux calculs de stationnement et encaissements.");
        d.setFont(police(Font.PLAIN, 11));
        d.setForeground(TEXTE_SECONDAIRE);
        g.gridy = 1;
        g.insets = new Insets(4, 0, 0, 0);
        infos.add(d, g);
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        meta.setOpaque(false);
        JLabel sym = new JLabel("Symbole :");
        sym.setFont(police(Font.PLAIN, 11));
        sym.setForeground(OUTLINE);
        meta.add(sym);
        meta.add(monoSom("Ar"));
        JLabel iso = new JLabel("ISO :");
        iso.setFont(police(Font.PLAIN, 11));
        iso.setForeground(OUTLINE);
        meta.add(iso);
        meta.add(monoSom("MGA"));
        JLabel prec = new JLabel("Précision :");
        prec.setFont(police(Font.PLAIN, 11));
        prec.setForeground(OUTLINE);
        meta.add(prec);
        meta.add(monoSom("Entier"));
        g.gridy = 2;
        g.insets = new Insets(6, 0, 0, 0);
        infos.add(meta, g);
        gauche.add(infos, BorderLayout.CENTER);

        JPanel droite = new JPanel(new GridBagLayout());
        droite.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chip.setOpaque(true);
        chip.setBackground(CHIP_FOND);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel ar = new JLabel("Ar");
        ar.setFont(policeMono(Font.BOLD, 14));
        ar.setForeground(VERT);
        chip.add(ar);
        JLabel ariary = new JLabel("Ariary (Ar)");
        ariary.setFont(police(Font.BOLD, 12));
        ariary.setForeground(TEXTE);
        chip.add(ariary);
        c.gridy = 0;
        c.insets = new Insets(0, 8, 0, 0);
        droite.add(chip, c);
        JPanel ex = new JPanel(new BorderLayout());
        ex.setOpaque(false);
        JLabel exemple = new JLabel("Exemple : 4 500 Ar / ticket");
        exemple.setFont(policeMono(Font.PLAIN, 9));
        exemple.setForeground(OUTLINE);
        ex.add(exemple, BorderLayout.EAST);
        c.gridy = 1;
        c.insets = new Insets(4, 0, 0, 0);
        droite.add(ex, c);

        JPanel article = new JPanel(new BorderLayout(12, 0));
        article.setOpaque(true);
        article.setBackground(CARTE);
        article.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        article.add(gauche, BorderLayout.CENTER);
        article.add(droite, BorderLayout.EAST);
        return article;
    }

    private static String nonModifiable() {
        return "Actif par défaut du système";
    }

    private static JLabel monoSom(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(policeMono(Font.BOLD, 10));
        l.setForeground(TEXTE);
        return l;
    }

    // ---------------------------------------------------------------------
    // PAGE SECURITE
    // ---------------------------------------------------------------------
    private JComponent creerPageSecurite() {
        styleChamp(pwActuel);
        styleChamp(pwNouveau);
        styleChamp(pwConfirmation);

        Carte carte = new Carte(Icone.Type.CADENAS, "Sécurité du compte",
                "Modifiez le mot de passe d'accès à la console.", chipSession());

        carte.ajouter(articleRang(blocChamp("Mot de passe actuel", pwActuel)), 16);
        carte.ajouter(articleRang(blocChamp("Nouveau mot de passe", pwNouveau)), 14);
        JPanel forceZone = new JPanel(new BorderLayout());
        forceZone.setOpaque(false);
        forceZone.add(force, BorderLayout.CENTER);
        carte.ajouter(articleRang(forceZone), 14);

        pwNouveau.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                majForce();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                majForce();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                majForce();
            }
        });

        carte.ajouter(articleRang(blocChamp("Confirmer le nouveau mot de passe", pwConfirmation)), 14);
        JLabel aide = hint("Au moins 6 caractères, différent du mot de passe actuel.");
        carte.ajouter(aide, 8);

        JButton btn = new BoutonStyle("Changer le mot de passe", BoutonStyle.Type.PRIMAIRE);
        btn.addActionListener(e -> changerMotDePasse());
        btn.setEnabled(utilisateur != null);
        carte.ajouter(alignerDroite(btn), 16);

        return page(carte);
    }

    private void majForce() {
        char[] c = pwNouveau.getPassword();
        force.setNiveau(niveau(c));
        Arrays.fill(c, '\0');
    }

    private static int niveau(char[] mdp) {
        if (mdp.length == 0) {
            return 0;
        }
        boolean maj = false, min = false, chiffre = false, symbole = false;
        for (char ch : mdp) {
            if (Character.isUpperCase(ch)) {
                maj = true;
            } else if (Character.isLowerCase(ch)) {
                min = true;
            } else if (Character.isDigit(ch)) {
                chiffre = true;
            } else {
                symbole = true;
            }
        }
        int score = (mdp.length >= 8 ? 1 : 0) + (maj && min ? 1 : 0)
                + (chiffre ? 1 : 0) + (symbole ? 1 : 0);
        return score <= 1 ? 1 : (score == 2 ? 2 : 3);
    }

    private void changerMotDePasse() {
        if (utilisateur == null) {
            return;
        }
        char[] actuel = pwActuel.getPassword();
        char[] nouveau = pwNouveau.getPassword();
        char[] confirmation = pwConfirmation.getPassword();
        try {
            if (actuel.length == 0 || nouveau.length == 0 || confirmation.length == 0) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs !");
                return;
            }
            if (!Arrays.equals(nouveau, confirmation)) {
                JOptionPane.showMessageDialog(this,
                        "Le nouveau mot de passe et sa confirmation sont differents.");
                return;
            }
            if (nouveau.length < 6) {
                JOptionPane.showMessageDialog(this,
                        "Le nouveau mot de passe doit contenir au moins 6 caracteres.");
                return;
            }
            if (Arrays.equals(nouveau, actuel)) {
                JOptionPane.showMessageDialog(this,
                        "Le nouveau mot de passe doit etre different de l'actuel.");
                return;
            }
            String ancienTexte = new String(actuel);
            String nouveauTexte = new String(nouveau);
            if (utilisateurDAO.changerMotDePasse(utilisateur.getIdUtilisateur(),
                    ancienTexte, nouveauTexte)) {
                utilisateur.setMotDePasse(nouveauTexte);
                JOptionPane.showMessageDialog(this, "Mot de passe modifie avec succes.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Echec : mot de passe actuel incorrect ou base inaccessible.");
            }
        } finally {
            Arrays.fill(actuel, '\0');
            Arrays.fill(nouveau, '\0');
            Arrays.fill(confirmation, '\0');
            pwActuel.setText("");
            pwNouveau.setText("");
            pwConfirmation.setText("");
        }
    }

    // ---------------------------------------------------------------------
    // PAGE TARIFS (onglet "Tarif")
    // ---------------------------------------------------------------------
    /**
     * Reutilise l'interface existante de gestion des tarifs ({@link TarifForm})
     * au lieu d'en dupliquer une seconde. {@code TarifForm} assure la
     * consultation et la modification des prix via {@code TarifDAO} et
     * PostgreSQL ; seul son en-tete de page est masque car l'onglet de
     * Parametres fournit deja le sien.
     */
    private JComponent creerPageTarifs() {
        return new TarifForm(false);
    }

    // ---------------------------------------------------------------------
    // PAGE A PROPOS (INFORMATIONS DE L'APPLICATION)
    // ---------------------------------------------------------------------
    private JComponent creerPageApropos() {
        Carte app = new Carte(Icone.Type.UTILISATEUR, "Application");
        ligne(app, "Nom", "SmartPark", true);
        ligne(app, "Description", "Gestion de parking", true);
        ligne(app, "Technologies", "Java Swing, JDBC, PostgreSQL", true);
        ligne(app, "Version Java", System.getProperty("java.version"), true);
        String date = capitaliser(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)));
        ligne(app, "Date du jour", date, false);

        Carte base = new Carte(Icone.Type.STATIONNEMENT, "Base de données");
        ligne(base, "Nom", "smartpark", true);
        ligne(base, "Hôte", "localhost", true);
        ligne(base, "Port", "5432", true);
        ligne(base, "Type", "PostgreSQL", false);

        JPanel grille = new JPanel(new GridLayout(0, 3, 8, 8));
        grille.setOpaque(false);
        String[] modules = {"Propriétaires", "Véhicules", "Entrée", "Sortie",
                "Historique", "Tarifs", "Statistiques"};
        for (String m : modules) {
            grille.add(new PastilleModule(m));
        }
        Carte mod = new Carte(Icone.Type.LISTE, "Modules disponibles");
        mod.ajouter(grille, 14);

        return page(app, base, mod);
    }

    private void ligne(Carte carte, String libelle, String valeur, boolean sep) {
        JPanel l = new JPanel(new BorderLayout(12, 0));
        l.setOpaque(false);
        JLabel g = new JLabel(libelle);
        g.setFont(police(Font.PLAIN, 13));
        g.setForeground(TEXTE_SECONDAIRE);
        JLabel d = new JLabel(valeur);
        d.setFont(police(Font.BOLD, 13));
        d.setForeground(TEXTE);
        d.setHorizontalAlignment(SwingConstants.RIGHT);
        l.add(g, BorderLayout.WEST);
        l.add(d, BorderLayout.CENTER);
        carte.ajouter(l, 10);
        if (sep) {
            carte.ajouter(separateur(), 10);
        }
    }

    // ---------------------------------------------------------------------
    // OUTILS DE MISE EN PAGE
    // ---------------------------------------------------------------------
    /** Page defilante : cartes pleine largeur, empilees en haut, hauteur naturelle. */
    private JComponent page(JPanel... cartesPage) {
        JPanel colonne = new JPanel(new GridBagLayout());
        colonne.setOpaque(false);
        colonne.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        for (int i = 0; i < cartesPage.length; i++) {
            g.gridy = i;
            g.insets = new Insets(i == 0 ? 0 : 16, 0, 0, 0);
            colonne.add(cartesPage[i], g);
        }

        PanneauDefilant fond = new PanneauDefilant(new GridBagLayout());
        fond.setOpaque(true);
        fond.setBackground(FOND);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTH;
        fond.add(colonne, c);

        JScrollPane defilement = new JScrollPane(fond,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        defilement.setBorder(null);
        defilement.getViewport().setBackground(FOND);
        defilement.getVerticalScrollBar().setUnitIncrement(16);
        return defilement;
    }

    private static JPanel alignerDroite(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private static JPanel separateur() {
        JPanel sep = new JPanel();
        sep.setOpaque(true);
        sep.setBackground(BORDURE);
        sep.setPreferredSize(new Dimension(1, 1));
        return sep;
    }

    private static String valeur(String texte) {
        return (texte == null || texte.trim().isEmpty()) ? "-" : texte.trim();
    }

    private static String capitaliser(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }
        return Character.toUpperCase(texte.charAt(0)) + texte.substring(1);
    }

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

    private static Font policeMono(int style, int taille) {
        return new Font(Font.MONOSPACED, style, taille);
    }

    private static JLabel libelle(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 12));
        label.setForeground(LABEL);
        return label;
    }

    /** Libelle de section : console (petit, majuscule). */
    private static JLabel section(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.BOLD, 10));
        label.setForeground(TEXTE_SECONDAIRE);
        return label;
    }

    /** Texte d'aide gris. */
    private static JLabel hint(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(police(Font.PLAIN, 11));
        label.setForeground(TEXTE_SECONDAIRE);
        return label;
    }

    private static void styleChamp(JTextField champ) {
        champ.setOpaque(true);
        champ.setBackground(CARTE);
        champ.setForeground(TEXTE);
        champ.setCaretColor(PRIMAIRE);
        champ.setFont(police(Font.PLAIN, 13));
        champ.setPreferredSize(new Dimension(120, 36));
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

    private static javax.swing.border.Border bordureChamp(int epaisseur, Color couleur) {
        int p = 6 - (epaisseur - 1);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(couleur, epaisseur),
                BorderFactory.createEmptyBorder(p, 10, p, 10));
    }

    private static JPanel blocChamp(String texte, JComponent champ) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel lab = libelle(texte);
        lab.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lab);
        p.add(Box.createVerticalStrut(6));
        champ.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(champ);
        return p;
    }

    /** Bandeau d'information bleu (lecture seule). */
    private JPanel bandeauInfo(String titre, String texte) {
        JPanel pan = new JPanel(new BorderLayout(10, 0));
        pan.setOpaque(true);
        pan.setBackground(BLEU_FOND);
        pan.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BLEU_BORDURE, 4, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        pan.add(new JLabel(new Icone(Icone.Type.CADENAS, 16, PRIMAIRE)), BorderLayout.WEST);

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titre);
        t.setFont(police(Font.BOLD, 12));
        t.setForeground(TEXTE);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(t);
        txt.add(Box.createVerticalStrut(2));
        JLabel x = new JLabel(texte);
        x.setFont(police(Font.PLAIN, 11));
        x.setForeground(TEXTE_SECONDAIRE);
        x.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(x);
        txt.add(Box.createVerticalGlue());
        pan.add(txt, BorderLayout.CENTER);
        return pan;
    }

    private Chip chipStatutSession() {
        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        contenu.setOpaque(false);
        contenu.add(new PastilleDot(VERT, 8));
        JLabel lbl = new JLabel("SESSION ACTIVE");
        lbl.setFont(police(Font.BOLD, 11));
        lbl.setForeground(VERT);
        contenu.add(lbl);
        return new Chip(CHIP_FOND, contenu);
    }

    private Chip chipSession() {
        JPanel contenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        contenu.setOpaque(false);
        JLabel lbl = new JLabel("SESSION OPÉRATEUR");
        lbl.setFont(police(Font.BOLD, 10));
        lbl.setForeground(TEXTE);
        contenu.add(lbl);
        return new Chip(CHIP_FOND, contenu);
    }

    /** Ancienne pastille de regie, conservee (signature). */
    private JPanel pastilleRegie() {
        JPanel pastille = new JPanel();
        pastille.setOpaque(true);
        pastille.setBackground(CARTE);
        pastille.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 4, 1),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        pastille.setLayout(new BoxLayout(pastille, BoxLayout.Y_AXIS));

        JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ligne1.setOpaque(false);
        ligne1.add(new PastilleDot(VERT, 8));
        JLabel titre = new JLabel("Régie sécurisée");
        titre.setFont(police(Font.BOLD, 12));
        titre.setForeground(TEXTE);
        ligne1.add(titre);
        ligne1.setAlignmentX(Component.LEFT_ALIGNMENT);
        pastille.add(ligne1);

        JLabel statut = new JLabel("SESSION ACTIVE");
        statut.setFont(police(Font.BOLD, 10));
        statut.setForeground(VERT);
        statut.setAlignmentX(Component.LEFT_ALIGNMENT);
        pastille.add(statut);
        return pastille;
    }

    // ---------------------------------------------------------------------
    // COMPOSANTS
    // ---------------------------------------------------------------------
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

    /** Carte blanche avec bande de titre headline-lg + option droite. */
    private static class Carte extends JPanel {
        private final JPanel corps = new JPanel(new GridBagLayout());
        private int ligne = 0;

        Carte(Icone.Type icone, String titre, String sousTitre, JComponent est) {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(CARTE);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDURE, 4, 1),
                    BorderFactory.createEmptyBorder(20, 24, 22, 24)));

            JPanel enTete = new JPanel(new BorderLayout(12, 0));
            enTete.setOpaque(false);
            JPanel gauche = new JPanel(new GridBagLayout());
            gauche.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = 0;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST;
            JPanel rang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            rang.setOpaque(false);
            if (icone != null) {
                rang.add(new JLabel(new Icone(icone, 20, TEXTE_SECONDAIRE)));
            }
            JLabel t = new JLabel(titre);
            t.setFont(police(Font.BOLD, 20));
            t.setForeground(TEXTE);
            rang.add(t);
            gauche.add(rang, g);
            if (sousTitre != null) {
                JLabel st = new JLabel(sousTitre);
                st.setFont(police(Font.PLAIN, 13));
                st.setForeground(TEXTE_SECONDAIRE);
                g.gridy = 1;
                g.insets = new Insets(4, 0, 0, 0);
                gauche.add(st, g);
            }
            enTete.add(gauche, BorderLayout.CENTER);
            if (est != null) {
                enTete.add(est, BorderLayout.EAST);
            }
            add(enTete, BorderLayout.NORTH);

            corps.setOpaque(false);
            add(corps, BorderLayout.CENTER);
        }

        Carte(Icone.Type icone, String titre, String sousTitre) {
            this(icone, titre, sousTitre, null);
        }

        Carte(Icone.Type icone, String titre) {
            this(icone, titre, null, null);
        }

        /** Ajoute un composant pleine largeur sous les precedents. */
        void ajouter(JComponent c, int margeHaut) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = ligne++;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(margeHaut, 0, 0, 0);
            corps.add(c, g);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width, getPreferredSize().height);
        }
    }

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
            g2.fillRoundRect(0, 0, cote, cote, 4, 4);
            icone.paintIcon(this, g2, (cote - icone.getIconWidth()) / 2,
                    (cote - icone.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color couleur;
        private final int rayon;
        private final int epaisseur;

        RoundedBorder(Color couleur, int rayon, int epaisseur) {
            this.couleur = couleur;
            this.rayon = rayon;
            this.epaisseur = epaisseur;
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
            return new Insets(0, 0, 0, 0);
        }
    }

    /** Bouton plat arrondi (primaire ou neutre). */
    private static class BoutonStyle extends JButton {
        enum Type { PRIMAIRE, NEUTRE }

        private final Type type;

        BoutonStyle(String texte, Type type) {
            super(texte);
            this.type = type;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, 13));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(fm.stringWidth(texte) + 48, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            ButtonModel m = getModel();
            Color fond;
            Color texte = type == Type.NEUTRE ? LABEL : Color.WHITE;
            if (!m.isEnabled()) {
                fond = new Color(0xE2E8F0);
                texte = new Color(0x94A3B8);
            } else if (m.isPressed() || m.isArmed()) {
                fond = type == Type.PRIMAIRE ? PRIMAIRE_PRESSED : BORDURE;
            } else if (m.isRollover()) {
                fond = type == Type.PRIMAIRE ? PRIMAIRE_HOVER : FOND;
            } else {
                fond = type == Type.PRIMAIRE ? PRIMAIRE : CARTE;
            }
            g2.setColor(fond);
            g2.fillRoundRect(0, 0, w, h, 4, 4);
            if (type == Type.NEUTRE && m.isEnabled()) {
                g2.setColor(BORDURE_CHAMP);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);
            }
            g2.setFont(police(Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(texte);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Onglet horizontal label-md avec indicateur bas 2px (coquille modele 1). */
    private static class Onglet extends JButton {
        private boolean actif;
        private final Icone icone;

        Onglet(String texte, Icone.Type type) {
            super(texte);
            this.icone = type != null ? new Icone(type, 16, LABEL) : null;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(police(Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 2));
            FontMetrics fm = getFontMetrics(getFont());
            int wTexte = fm.stringWidth(texte);
            setPreferredSize(new Dimension(
                    wTexte + (icone != null ? icone.getIconWidth() + 8 : 0) + 4, 30));
        }

        void setActif(boolean actif) {
            this.actif = actif;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color couleurTexte = actif ? PRIMAIRE : (getModel().isRollover() ? TEXTE : LABEL);
            g2.setFont(police(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int yBase = (h - 10 + fm.getAscent() - fm.getDescent()) / 2;
            if (icone != null) {
                icone.setCouleur(couleurTexte);
                icone.paintIcon(this, g2, 0, yBase - fm.getAscent() + 2);
            }
            g2.setColor(couleurTexte);
            int x = icone != null ? icone.getIconWidth() + 8 : 0;
            g2.drawString(getText(), x, yBase);
            if (actif) {
                g2.setColor(PRIMAIRE);
                g2.fillRect(0, h - 2, w, 2);
            }
            g2.dispose();
        }
    }

    /** Avatar rond : initiale du nom sur fond bleu. */
    private static class Avatar extends JComponent {
        private String initiale;

        Avatar(String nom) {
            setNom(nom);
            Dimension d = new Dimension(64, 64);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        void setNom(String nom) {
            String t = (nom == null || nom.trim().isEmpty()) ? "-" : nom.trim();
            this.initiale = t.substring(0, 1).toUpperCase(Locale.FRENCH);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            g2.setColor(PRIMAIRE);
            g2.fillOval(0, 0, d, d);
            g2.setFont(police(Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(initiale, (d - fm.stringWidth(initiale)) / 2,
                    (d + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Pastille arrondie pour le role. */
    private static class Pastille extends JLabel {
        Pastille(String texte) {
            super(texte);
            setOpaque(false);
            setFont(police(Font.BOLD, 12));
            setForeground(PASTILLE_TEXTE);
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
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
            g2.setColor(SELECTION);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Pastille de module : puce bleue + libelle. */
    private static class PastilleModule extends JLabel {
        PastilleModule(String texte) {
            super(texte);
            setOpaque(false);
            setFont(police(Font.PLAIN, 12));
            setForeground(TEXTE);
            setBorder(BorderFactory.createEmptyBorder(8, 26, 8, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g2.setColor(PASTILLE_MODULE);
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            g2.setColor(PRIMAIRE);
            g2.fillOval(12, (h - 8) / 2, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Indicateur de force du mot de passe : 3 segments + libelle. */
    private static class ForceMotDePasse extends JComponent {
        private int niveau;

        ForceMotDePasse() {
            setPreferredSize(new Dimension(100, 20));
        }

        void setNiveau(int niveau) {
            this.niveau = niveau;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color actif = niveau == 1 ? ROUGE : (niveau == 2 ? ORANGE : VERT);
            int largeurTexte = 70;
            int segment = (getWidth() - largeurTexte - 16) / 3;
            for (int i = 0; i < 3; i++) {
                g2.setColor(i < niveau ? actif : BORDURE);
                g2.fillRoundRect(i * (segment + 8), 7, segment, 6, 6, 6);
            }
            String texte = niveau == 0 ? "" : (niveau == 1 ? "Faible" : (niveau == 2 ? "Moyen" : "Fort"));
            g2.setFont(police(Font.BOLD, 12));
            g2.setColor(niveau == 0 ? TEXTE_SECONDAIRE : actif);
            g2.drawString(texte, 3 * (segment + 8) + 4, 14);
            g2.dispose();
        }
    }

    /** Tuile de valeur en lecture seule (modele Paramatre). */
    private static class TuileChamp extends JPanel {
        TuileChamp(String libelle, String valeur, Icone.Type icone) {
            super(new GridBagLayout());
            setOpaque(true);
            setBackground(FOND);
            setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDURE, 4, 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = 0;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST;
            JLabel lab = new JLabel(libelle.toUpperCase(Locale.FRENCH));
            lab.setFont(police(Font.BOLD, 9));
            lab.setForeground(TEXTE_SECONDAIRE);
            add(lab, g);
            if (icone != null) {
                JLabel ic = new JLabel(new Icone(icone, 14, TEXTE_SECONDAIRE));
                g.gridx = 1;
                g.weightx = 0;
                add(ic, g);
            }
            JLabel val = new JLabel(valeur);
            val.setFont(police(Font.BOLD, 14));
            val.setForeground(TEXTE);
            g.gridx = 0;
            g.gridy = 1;
            g.gridwidth = 2;
            g.weightx = 1;
            g.insets = new Insets(6, 0, 0, 0);
            add(val, g);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /** Boite icone 40x40 (modele 1 : w-10 h-10, icone 22px). */
    private static class BoiteIcone extends JComponent {
        private final Icone icone;

        BoiteIcone(Icone.Type type) {
            this.icone = new Icone(type, 22, PRIMAIRE);
            Dimension d = new Dimension(40, 40);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(LIGNE_ALTERNEE);
            g2.fillRoundRect(0, 0, 40, 40, 2, 2);
            icone.paintIcon(this, g2, (40 - icone.getIconWidth()) / 2,
                    (40 - icone.getIconHeight()) / 2);
            g2.dispose();
        }
    }

    /** Rangée d'article : carte claire à un contenu (modele 1). */
    private static JPanel articleRang(JComponent contenu) {
        JPanel article = new JPanel(new BorderLayout(12, 0));
        article.setOpaque(true);
        article.setBackground(CARTE);
        article.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDURE, 2, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        article.add(contenu, BorderLayout.CENTER);
        return article;
    }

    /** Vignette de libelle (label-sm) a fond leger. */
    private static JLabel vignette(String texte) {
        JLabel v = new JLabel(texte);
        v.setFont(police(Font.BOLD, 10));
        v.setForeground(VERT);
        v.setOpaque(true);
        v.setBackground(FOND);
        v.setBorder(BorderFactory.createEmptyBorder(1, 8, 1, 8));
        return v;
    }

}