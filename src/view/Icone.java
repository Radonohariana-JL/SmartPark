package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Bibliotheque d'icones vectorielles de SmartPark.
 * Tout est dessine avec Graphics2D (aucune image, aucune bibliotheque externe),
 * dans un repere de reference de 24 x 24 px, mis a l'echelle vers la taille
 * demandee. Trait arrondi, antialiasing active.
 *
 * Les glyphes reprennent les icones "Material Symbols" du modele de reference :
 * dashboard (ACCUEIL), badge (PROPRIETAIRE), directions_car (VEHICULE),
 * login (ENTREE), logout (SORTIE), history (HISTORIQUE), payments (TARIF),
 * settings (PARAMETRE), power_settings_new (DECONNEXION), person (UTILISATEUR),
 * lock (CADENAS), visibility (OEIL / OEIL_BARRE), local_parking (PARKING),
 * mail (EMAIL), check_circle (COCHE), calendar_today (CALENDRIER).
 */
public final class Icone implements Icon {

    /** Les differentes glyphes disponibles dans l'application. */
    public enum Type {
        ACCUEIL,
        PROPRIETAIRE,
        VEHICULE,
        ENTREE,
        SORTIE,
        HISTORIQUE,
        TARIF,
        STATISTIQUE,
        PARAMETRE,
        DECONNEXION,
        UTILISATEUR,
        CADENAS,
        OEIL,
        OEIL_BARRE,
        PARKING,
        EMAIL,
        COCHE,
        CALENDRIER,
        CLOCHE,
        LISTE,
        CHEVRON,
        CHEVRON_BAS,
        CHEVRON_HAUT,
        TENDANCE,
        STATIONNEMENT,
        MODIFIER,
        SUPPRIMER,
        RECHERCHE,
        AJOUTER
    }

    /** Taille du carre de reference dans lequel les glyphes sont dessines. */
    private static final float ESPACE = 24f;

    /** Epaisseur du trait dans le repere de reference (~1.8 px pour 20 px). */
    private static final float TRAIT = 2.1f;

    private final Type type;
    private int taille;
    private Color couleur;

    public Icone(Type type, int taille, Color couleur) {
        if (type == null) {
            throw new IllegalArgumentException("Type d'icone obligatoire");
        }
        this.type = type;
        this.taille = Math.max(1, taille);
        this.couleur = couleur != null ? couleur : Color.BLACK;
    }

    /** Permet de changer la couleur au vol (survol, etat actif, desactivation). */
    public void setCouleur(Color couleur) {
        this.couleur = couleur != null ? couleur : Color.BLACK;
    }

    public Color getCouleur() {
        return couleur;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int getIconWidth() {
        return taille;
    }

    @Override
    public int getIconHeight() {
        return taille;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            float echelle = taille / ESPACE;
            g2.translate(x, y);
            g2.scale(echelle, echelle);

            g2.setStroke(new BasicStroke(TRAIT,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(couleur);

            dessiner(g2);
        } finally {
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // DESSIN DES GLYPHES (repere 24 x 24)
    // ------------------------------------------------------------------
    private void dessiner(Graphics2D g2) {
        switch (type) {
            case ACCUEIL:
                dessinerAccueil(g2);
                break;
            case PROPRIETAIRE:
                dessinerProprietaire(g2);
                break;
            case VEHICULE:
                dessinerVehicule(g2);
                break;
            case ENTREE:
                dessinerEntree(g2);
                break;
            case SORTIE:
                dessinerSortie(g2);
                break;
            case HISTORIQUE:
                dessinerHistorique(g2);
                break;
            case TARIF:
                dessinerTarif(g2);
                break;
            case STATISTIQUE:
                dessinerStatistique(g2);
                break;
            case PARAMETRE:
                dessinerParametre(g2);
                break;
            case DECONNEXION:
                dessinerDeconnexion(g2);
                break;
            case UTILISATEUR:
                dessinerUtilisateur(g2);
                break;
            case CADENAS:
                dessinerCadenas(g2);
                break;
            case OEIL:
                dessinerOeil(g2, false);
                break;
            case OEIL_BARRE:
                dessinerOeil(g2, true);
                break;
            case PARKING:
                dessinerParking(g2);
                break;
            case EMAIL:
                dessinerEmail(g2);
                break;
            case COCHE:
                dessinerCoche(g2);
                break;
            case CALENDRIER:
                dessinerCalendrier(g2);
                break;
            case CLOCHE:
                dessinerCloche(g2);
                break;
            case LISTE:
                dessinerListe(g2);
                break;
            case CHEVRON:
                dessinerChevron(g2);
                break;
            case CHEVRON_BAS:
                dessinerChevronBas(g2);
                break;
            case CHEVRON_HAUT:
                dessinerChevronHaut(g2);
                break;
            case TENDANCE:
                dessinerTendance(g2);
                break;
            case STATIONNEMENT:
                dessinerStationnement(g2);
                break;
            case MODIFIER:
                dessinerModifier(g2);
                break;
            case SUPPRIMER:
                dessinerSupprimer(g2);
                break;
            case RECHERCHE:
                dessinerRecherche(g2);
                break;
            case AJOUTER:
                dessinerAjouter(g2);
                break;
            default:
                break;
        }
    }

    /** Glyphe "dashboard" : quatre dalles carrees. */
    private void dessinerAccueil(Graphics2D g2) {
        double cote = 7.2;
        double ecart = 2.6;
        double x0 = 3.2;
        double y0 = 3.2;
        g2.draw(new RoundRectangle2D.Double(x0, y0, cote, cote, 2, 2));
        g2.draw(new RoundRectangle2D.Double(x0 + cote + ecart, y0, cote, cote, 2, 2));
        g2.draw(new RoundRectangle2D.Double(x0, y0 + cote + ecart, cote, cote, 2, 2));
        g2.draw(new RoundRectangle2D.Double(x0 + cote + ecart, y0 + cote + ecart,
                cote, cote, 2, 2));
    }

    private void dessinerProprietaire(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(3.5, 4.5, 17, 15, 3, 3));
        g2.draw(new Ellipse2D.Double(9, 7.5, 6, 6));

        Path2D epaules = new Path2D.Double();
        epaules.moveTo(7.8, 17.6);
        epaules.curveTo(8.4, 14.4, 15.6, 14.4, 16.2, 17.6);
        g2.draw(epaules);
    }

    private void dessinerVehicule(Graphics2D g2) {
        Path2D corps = new Path2D.Double();
        corps.moveTo(3.8, 14.6);
        corps.lineTo(5.4, 10.4);
        corps.curveTo(5.8, 9.3, 6.8, 8.6, 8, 8.6);
        corps.lineTo(16, 8.6);
        corps.curveTo(17.2, 8.6, 18.2, 9.3, 18.6, 10.4);
        corps.lineTo(20.2, 14.6);
        corps.closePath();
        g2.draw(corps);

        g2.draw(new Line2D.Double(5.9, 12.4, 18.1, 12.4));
        g2.draw(new Ellipse2D.Double(6.6, 14.4, 3.6, 3.6));
        g2.draw(new Ellipse2D.Double(13.8, 14.4, 3.6, 3.6));
    }

    private void dessinerEntree(Graphics2D g2) {
        Path2D cadre = new Path2D.Double();
        cadre.moveTo(15.5, 4.5);
        cadre.lineTo(20.5, 4.5);
        cadre.lineTo(20.5, 19.5);
        cadre.lineTo(15.5, 19.5);
        g2.draw(cadre);

        g2.draw(new Line2D.Double(3.5, 12, 16.5, 12));
        g2.draw(new Line2D.Double(13.4, 8.9, 16.5, 12));
        g2.draw(new Line2D.Double(13.4, 15.1, 16.5, 12));
    }

    private void dessinerSortie(Graphics2D g2) {
        Path2D cadre = new Path2D.Double();
        cadre.moveTo(3.5, 4.5);
        cadre.lineTo(8.5, 4.5);
        cadre.lineTo(8.5, 19.5);
        cadre.lineTo(3.5, 19.5);
        g2.draw(cadre);

        g2.draw(new Line2D.Double(7.5, 12, 20.5, 12));
        g2.draw(new Line2D.Double(17.4, 8.9, 20.5, 12));
        g2.draw(new Line2D.Double(17.4, 15.1, 20.5, 12));
    }

    private void dessinerHistorique(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
        g2.draw(new Line2D.Double(12, 7, 12, 12.3));
        g2.draw(new Line2D.Double(12, 12.3, 15.6, 14.4));
        g2.draw(new Line2D.Double(12, 12.3, 8.6, 10.4));
    }

    private void dessinerTarif(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(2.5, 6, 19, 12, 3, 3));
        g2.draw(new Ellipse2D.Double(9, 9, 6, 6));
        g2.draw(new Line2D.Double(5.5, 12, 7.2, 12));
        g2.draw(new Line2D.Double(16.8, 12, 18.5, 12));
    }

    private void dessinerStatistique(Graphics2D g2) {
        g2.draw(new Line2D.Double(3.5, 20, 20.5, 20));
        g2.draw(new Line2D.Double(7, 20, 7, 13.2));
        g2.draw(new Line2D.Double(12, 20, 12, 8.4));
        g2.draw(new Line2D.Double(17, 20, 17, 15.4));
    }

    private void dessinerParametre(Graphics2D g2) {
        final int dents = 8;
        final double centre = 12;
        final double rExt = 8.6;
        final double rInt = 6.3;
        final double pas = 2 * Math.PI / dents;

        Path2D engrenage = new Path2D.Double();
        for (int i = 0; i < dents; i++) {
            double a0 = i * pas;
            ajouterPoint(engrenage, i == 0, centre, rInt, a0 - pas * 0.5);
            ajouterPoint(engrenage, false, centre, rExt, a0 - pas * 0.22);
            ajouterPoint(engrenage, false, centre, rExt, a0 + pas * 0.22);
            ajouterPoint(engrenage, false, centre, rInt, a0 + pas * 0.5);
        }
        engrenage.closePath();
        g2.draw(engrenage);
        g2.draw(new Ellipse2D.Double(centre - 3.2, centre - 3.2, 6.4, 6.4));
    }

    private static void ajouterPoint(Path2D path, boolean premier,
            double centre, double rayon, double angle) {
        double x = centre + rayon * Math.cos(angle);
        double y = centre + rayon * Math.sin(angle);
        if (premier) {
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
    }

    /** Glyphe "logout" norme Material : cadre de porte ouvert + fleche sortante. */
    private void dessinerDeconnexion(Graphics2D g2) {
        Path2D porte = new Path2D.Double();
        porte.moveTo(12.4, 3.6);
        porte.lineTo(4.4, 3.6);
        porte.lineTo(4.4, 20.4);
        porte.lineTo(12.4, 20.4);
        g2.draw(porte);

        g2.draw(new Line2D.Double(8.4, 12, 19.4, 12));
        g2.draw(new Line2D.Double(16.3, 8.9, 19.4, 12));
        g2.draw(new Line2D.Double(16.3, 15.1, 19.4, 12));
    }

    /** Glyphe "stationnement" : deux fleches horizontales opposees (transfert). */
    private void dessinerStationnement(Graphics2D g2) {
        g2.draw(new Line2D.Double(4.4, 9, 18.2, 9));
        g2.draw(new Line2D.Double(15.1, 5.9, 18.2, 9));
        g2.draw(new Line2D.Double(15.1, 12.1, 18.2, 9));

        g2.draw(new Line2D.Double(19.6, 15, 5.8, 15));
        g2.draw(new Line2D.Double(8.9, 11.9, 5.8, 15));
        g2.draw(new Line2D.Double(8.9, 18.1, 5.8, 15));
    }

    private void dessinerUtilisateur(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(8.4, 4, 7.2, 7.2));

        Path2D buste = new Path2D.Double();
        buste.moveTo(4.6, 20.4);
        buste.curveTo(5.6, 15.2, 18.4, 15.2, 19.4, 20.4);
        g2.draw(buste);
    }

    private void dessinerCadenas(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(4.5, 10.5, 15, 10, 3, 3));

        Path2D anse = new Path2D.Double();
        anse.moveTo(8, 10.5);
        anse.lineTo(8, 7.6);
        anse.curveTo(8, 4.3, 16, 4.3, 16, 7.6);
        anse.lineTo(16, 10.5);
        g2.draw(anse);

        g2.fill(new Ellipse2D.Double(11.1, 13.6, 1.8, 1.8));
        g2.draw(new Line2D.Double(12, 15.8, 12, 17.6));
    }

    private void dessinerOeil(Graphics2D g2, boolean barre) {
        Path2D oeil = new Path2D.Double();
        oeil.moveTo(2.6, 12);
        oeil.curveTo(6.6, 6.2, 17.4, 6.2, 21.4, 12);
        oeil.curveTo(17.4, 17.8, 6.6, 17.8, 2.6, 12);
        oeil.closePath();
        g2.draw(oeil);
        g2.draw(new Ellipse2D.Double(9, 9, 6, 6));

        if (barre) {
            g2.draw(new Line2D.Double(4.4, 4.4, 19.6, 19.6));
        }
    }

    private void dessinerParking(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(3.5, 3.5, 17, 17, 4, 4));

        g2.draw(new Line2D.Double(9.6, 17.6, 9.6, 6.4));

        Path2D panne = new Path2D.Double();
        panne.moveTo(9.6, 6.4);
        panne.lineTo(13, 6.4);
        panne.quadTo(16.4, 6.4, 16.4, 9.3);
        panne.quadTo(16.4, 12.2, 13, 12.2);
        panne.lineTo(9.6, 12.2);
        g2.draw(panne);
    }

    private void dessinerEmail(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(3, 5.5, 18, 13, 3, 3));

        Path2D rabat = new Path2D.Double();
        rabat.moveTo(4.2, 7.2);
        rabat.lineTo(12, 12.9);
        rabat.lineTo(19.8, 7.2);
        g2.draw(rabat);
    }

    /** Glyphe "check_circle" : coche dans un cercle (stationnements termines). */
    private void dessinerCoche(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));

        Path2D coche = new Path2D.Double();
        coche.moveTo(8, 12.3);
        coche.lineTo(11, 15.3);
        coche.lineTo(16.2, 9);
        g2.draw(coche);
    }

    /** Glyphe "calendar_today" : cadre + deux attaches + ligne d'en-tete. */
    private void dessinerCalendrier(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15, 2.5, 2.5));
        g2.draw(new Line2D.Double(3.5, 9.5, 20.5, 9.5));
        g2.draw(new Line2D.Double(8, 3.2, 8, 6.4));
        g2.draw(new Line2D.Double(16, 3.2, 16, 6.4));
    }

    /** Glyphe "notifications" : cloche + battant. */
    private void dessinerCloche(Graphics2D g2) {
        Path2D cloche = new Path2D.Double();
        cloche.moveTo(6.3, 16.6);
        cloche.curveTo(6.3, 14.9, 5.3, 14.0, 5.3, 11.4);
        cloche.curveTo(5.3, 7.5, 8.2, 5.2, 12, 5.2);
        cloche.curveTo(15.8, 5.2, 18.7, 7.5, 18.7, 11.4);
        cloche.curveTo(18.7, 14.0, 17.7, 14.9, 17.7, 16.6);
        cloche.closePath();
        g2.draw(cloche);
        g2.draw(new Line2D.Double(4.4, 16.6, 19.6, 16.6));
        g2.draw(new Arc2D.Double(9.8, 16.6, 4.4, 4.4, 180, 180, Arc2D.OPEN));
    }

    /** Glyphe "stream" : puces + lignes (journal des mouvements). */
    private void dessinerListe(Graphics2D g2) {
        double[] ys = {7.0, 12.0, 17.0};
        for (double y : ys) {
            g2.fill(new Ellipse2D.Double(4.4, y - 1.05, 2.1, 2.1));
            g2.draw(new Line2D.Double(9.2, y, 19.6, y));
        }
    }

    /** Glyphe "chevron_right". */
    private void dessinerChevron(Graphics2D g2) {
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(9.4, 6.2);
        chevron.lineTo(15.2, 12);
        chevron.lineTo(9.4, 17.8);
        g2.draw(chevron);
    }

    /** Glyphe "expand_more" (chevron vers le bas, sous-menu ferme). */
    private void dessinerChevronBas(Graphics2D g2) {
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(6.2, 9.4);
        chevron.lineTo(12, 15.2);
        chevron.lineTo(17.8, 9.4);
        g2.draw(chevron);
    }

    /** Glyphe "expand_less" (chevron vers le haut, sous-menu ouvert). */
    private void dessinerChevronHaut(Graphics2D g2) {
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(6.2, 14.6);
        chevron.lineTo(12, 8.8);
        chevron.lineTo(17.8, 14.6);
        g2.draw(chevron);
    }

    /** Glyphe "trending_up" : courbe montante + fleche. */
    private void dessinerTendance(Graphics2D g2) {
        Path2D courbe = new Path2D.Double();
        courbe.moveTo(3.8, 16.4);
        courbe.lineTo(9.2, 11.0);
        courbe.lineTo(12.2, 14.0);
        courbe.lineTo(19.2, 7.0);
        g2.draw(courbe);
        g2.draw(new Line2D.Double(14.6, 7.0, 19.2, 7.0));
        g2.draw(new Line2D.Double(19.2, 7.0, 19.2, 11.6));
    }

    /** Glyphe "edit" : crayon incline (bouton Modifier). */
    private void dessinerModifier(Graphics2D g2) {
        Path2D crayon = new Path2D.Double();
        crayon.moveTo(4.6, 19.4);
        crayon.lineTo(5.8, 15.2);
        crayon.lineTo(15.6, 5.4);
        crayon.lineTo(18.6, 8.4);
        crayon.lineTo(8.8, 18.2);
        crayon.closePath();
        g2.draw(crayon);
        g2.draw(new Line2D.Double(5.8, 15.2, 8.8, 18.2));
    }

    /** Glyphe "delete" : corbeille (bouton Supprimer). */
    private void dessinerSupprimer(Graphics2D g2) {
        g2.draw(new Line2D.Double(4.4, 7.0, 19.6, 7.0));

        Path2D anse = new Path2D.Double();
        anse.moveTo(9.4, 7.0);
        anse.lineTo(9.7, 4.4);
        anse.lineTo(14.3, 4.4);
        anse.lineTo(14.6, 7.0);
        g2.draw(anse);

        Path2D corbeille = new Path2D.Double();
        corbeille.moveTo(6.4, 7.0);
        corbeille.lineTo(17.6, 7.0);
        corbeille.lineTo(16.6, 19.6);
        corbeille.lineTo(7.4, 19.6);
        corbeille.closePath();
        g2.draw(corbeille);
        g2.draw(new Line2D.Double(10.2, 10.2, 10.2, 16.6));
        g2.draw(new Line2D.Double(13.8, 10.2, 13.8, 16.6));
    }

    /** Glyphe "search" : loupe (barre de recherche). */
    private void dessinerRecherche(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(4.2, 4.2, 12.0, 12.0));
        g2.draw(new Line2D.Double(14.6, 14.6, 19.6, 19.6));
    }

    /** Glyphe "add" : croix (bouton Ajouter). */
    private void dessinerAjouter(Graphics2D g2) {
        g2.draw(new Line2D.Double(12, 5.0, 12, 19.0));
        g2.draw(new Line2D.Double(5.0, 12, 19.0, 12));
    }
}
