package chomsky.ui;

import chomsky.model.Catalog;
import chomsky.model.DFA;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Nivel 3: Autómata Finito Determinista. */
public class Level3Panel extends BaseModulePanel {
    private TapePanel tape;
    private JLabel pathLabel;
    private final List<String> path = new ArrayList<>();

    public Level3Panel(Runnable onBack) {
        super(Theme.L3, "Nivel 3 · Lenguajes regulares (Tipo 3)",
                "Autómata Finito Determinista (AFD): reconoce la cadena símbolo a símbolo, sin memoria auxiliar",
                Catalog.level3(), onBack);
        build();
    }

    @Override
    protected JComponent createVisualization() {
        tape = new TapePanel(TapePanel.Mode.INPUT, accent);
        tape.setCaption("Cinta de entrada (solo lectura · el cabezal avanza una celda por paso)");
        pathLabel = new JLabel();
        pathLabel.setFont(Theme.MONO);
        pathLabel.setForeground(Theme.TEXT);
        pathLabel.setBorder(new EmptyBorder(0, 14, 10, 14));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(tape, BorderLayout.CENTER);
        p.add(pathLabel, BorderLayout.SOUTH);
        return p;
    }

    @Override
    protected void onLoaded() {
        path.clear();
        path.add(automaton.getStartState());
    }

    @Override
    protected void afterStep() {
        DFA d = (DFA) automaton;
        if (automaton.getLastEdge() != null) {
            char c = d.getInput().charAt(d.getPosition() - 1);
            path.add("─" + c + "→ " + d.getCurrentState());
        }
    }

    @Override
    protected void refreshVisualization(boolean animate) {
        DFA d = (DFA) automaton;
        tape.setContent(TapePanel.chars(d.getInput()), d.getPosition(), 0, d.getCurrentState(),
                d.isHalted(), d.isAccepted(), "", animate);
        String route = String.join("  ", path);
        pathLabel.setText("<html><b>Recorrido:</b>&nbsp; " + route.replace("<", "&lt;") + "</html>");
    }
}
