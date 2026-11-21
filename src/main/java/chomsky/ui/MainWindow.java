package chomsky.ui;

import javax.swing.*;
import java.awt.*;

/** Ventana principal: alterna entre el menú y los cuatro módulos. */
public class MainWindow extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public MainWindow() {
        super("Simulador · Jerarquía de Chomsky");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Runnable home = () -> show("home");
        root.add(new HomePanel(level -> show("L" + level)), "home");
        root.add(new Level3Panel(home), "L3");
        root.add(new Level2Panel(home), "L2");
        root.add(new Level1Panel(home), "L1");
        root.add(new Level0Panel(home), "L0");
        setContentPane(root);
        setMinimumSize(new Dimension(1150, 740));
        setSize(1320, 860);
        setLocationRelativeTo(null);
    }

    public void show(String card) {
        cards.show(root, card);
    }
}
