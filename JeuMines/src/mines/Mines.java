package mines;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class Mines extends JFrame {
	private static final long serialVersionUID = 4772165125287256837L;
	
	private static final int width = 250;
    private static final int height = 290;

    private JLabel statusbar;
    
    /**
     * Constructeur de la classe Mines.
     * Initialise la fenêtre principale du jeu.
     */
    public Mines() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
        setTitle("Minesweeper");

        statusbar = new JLabel("");
        add(statusbar, BorderLayout.SOUTH);

        add(new Board(statusbar));

        setResizable(false);
        setVisible(true);
    }
    
    /**
     * Point d'entrée de l'application.
     * Crée une instance de la classe Mines pour démarrer le jeu.
     */
    public static void main(String[] args) {
        new Mines();
    }
}
