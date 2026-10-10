package view;

import dao.PlaceDAO;
import dao.StationnementDAO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Statistiques : vehicules gares, places libres, stationnements et
 * recettes du jour, avec un graphique simple des recettes des 7 derniers jours.
 */
public class StatistiqueForm extends JPanel {

    private final JLabel lblGares = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblLibres = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblAujourdhui = new JLabel("-", SwingConstants.CENTER);
    private final JLabel lblRecettes = new JLabel("-", SwingConstants.CENTER);

    private final GraphiqueRecettes graphique = new GraphiqueRecettes();
    private final StationnementDAO stationnementDAO = new StationnementDAO();
    private final PlaceDAO placeDAO = new PlaceDAO();

    public StatistiqueForm() {
        setLayout(new BorderLayout());
        add(creerIndicateurs(), BorderLayout.NORTH);
        add(creerGraphique(), BorderLayout.CENTER);
        actualiser();
    }

    private JPanel creerIndicateurs() {
        JPanel grille = new JPanel(new GridLayout(1, 4, 10, 10));
        grille.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        grille.add(creerCarte("Vehicules actuellement gares", lblGares));
        grille.add(creerCarte("Places disponibles", lblLibres));
        grille.add(creerCarte("Stationnements aujourd'hui", lblAujourdhui));
        grille.add(creerCarte("Recettes aujourd'hui (Ar)", lblRecettes));
        return grille;
    }

    private JPanel creerCarte(String titre, JLabel valeur) {
        JPanel carte = new JPanel(new BorderLayout());
        carte.setBackground(new Color(240, 244, 248));
        carte.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 12));
        carte.add(lblTitre, BorderLayout.NORTH);

        valeur.setFont(new Font("Arial", Font.BOLD, 22));
        carte.add(valeur, BorderLayout.CENTER);
        return carte;
    }

    private JPanel creerGraphique() {
        JPanel pan = new JPanel(new BorderLayout());
        pan.setBorder(BorderFactory.createTitledBorder(
                "Recettes par jour (7 derniers jours)"));
        graphique.setPreferredSize(new Dimension(600, 300));
        pan.add(graphique, BorderLayout.CENTER);
        return pan;
    }

    /** Recharge toutes les statistiques depuis la base. */
    public void actualiser() {
        lblGares.setText(String.valueOf(stationnementDAO.vehiculesActuellementGares()));
        lblLibres.setText(String.valueOf(placeDAO.compterLibres()));
        lblAujourdhui.setText(String.valueOf(stationnementDAO.stationnementsAujourdhui()));
        lblRecettes.setText(String.valueOf(stationnementDAO.recettesAujourdhui()));
        graphique.setDonnees(stationnementDAO.recettesParJour());
        repaint();
    }

    /**
     * Graphique a barres dessine avec Graphics2D.
     * Les donnees = tableau [n][2] : { "jj/mm", montant }.
     */
    private static class GraphiqueRecettes extends JPanel {

        private String[][] donnees = new String[0][];

        public void setDonnees(String[][] donnees) {
            this.donnees = donnees != null ? donnees : new String[0][];
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));

            int largeur = getWidth();
            int hauteur = getHeight();
            int margeGauche = 50;
            int margeBas = 40;
            int margeHaut = 20;

            double maxMontant = 1;
            for (String[] ligne : donnees) {
                maxMontant = Math.max(maxMontant, Double.parseDouble(ligne[1]));
            }

            // Axes
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(margeGauche, margeHaut, margeGauche, hauteur - margeBas);
            g2.drawLine(margeGauche, hauteur - margeBas, largeur - 10, hauteur - margeBas);

            int zone = largeur - margeGauche - 40;
            double largeurBarre = (int) (zone / (double) Math.max(donnees.length, 1) * 0.6);

            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < donnees.length; i++) {
                double montant = Double.parseDouble(donnees[i][1]);

                int symboleHauteur = (int) (montant / maxMontant
                        * (hauteur - margeBas - margeHaut - 20));
                int x = margeGauche + 20 + (int) (i * (zone / (double) Math.max(donnees.length, 1)));
                int yBarre = hauteur - margeBas - symboleHauteur;

                // Barre
                g2.setColor(new Color(37, 99, 235));
                g2.fillRect(x, yBarre, (int) largeurBarre, symboleHauteur);

                // Pourcentage du montant au-dessus de la barre
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf((int) montant),
                        x, yBarre - 5);

                // Jour sous la barre
                int xJour = x + (int) (largeurBarre / 2) - fm.stringWidth(donnees[i][0]) / 2;
                g2.drawString(donnees[i][0], xJour, hauteur - margeBas + 18);
            }
        }
    }
}