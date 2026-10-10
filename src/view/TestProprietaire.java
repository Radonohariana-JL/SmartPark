package view;

import javax.swing.JFrame;

/**
 * Petit programme de test pour afficher seul le panneau Proprietaire.
 */
public class TestProprietaire {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Test Proprietaire");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ProprietaireFormPanel());
        frame.setVisible(true);
    }
}