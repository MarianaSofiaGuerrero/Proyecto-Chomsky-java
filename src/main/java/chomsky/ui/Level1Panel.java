package chomsky.ui;

import chomsky.model.Automaton;
import chomsky.model.Catalog;
import chomsky.model.TuringMachine;

import javax.swing.*;

/** Nivel 1: Autómata Linealmente Acotado. */
public class Level1Panel extends BaseModulePanel {
    private TapePanel tape;

    public Level1Panel(Runnable onBack) {
        super(Theme.L1, "Nivel 1 · Lenguajes sensibles al contexto (Tipo 1)",
                "Autómata Linealmente Acotado (ALA): MT cuya cinta se limita a la longitud de la entrada entre < y >",
                Catalog.level1(), onBack);
        build();
    }

    @Override
    protected JComponent createVisualization() {
        tape = new TapePanel(TapePanel.Mode.BOUNDED, accent);
        return tape;
    }

    @Override
    protected void onMachineChanged() {
        TuringMachine m = (TuringMachine) automaton;
        tape.setCaption("Cinta acotada  ·  Γ = " + Automaton.setOf(m.getTapeAlphabet())
                + "  ·  el cabezal lee/escribe y se mueve en ambos sentidos");
    }

    @Override
    protected void refreshVisualization(boolean animate) {
        TuringMachine m = (TuringMachine) automaton;
        int cells = m.getTape().size();
        String move = m.getLastMove() == 'L' ? "  ·  último movimiento: ← izquierda"
                : m.getLastMove() == 'R' ? "  ·  último movimiento: → derecha" : "";
        tape.setContent(m.getTape(), m.getHead(), m.getOrigin(), m.getCurrentState(), m.isHalted(), m.isAccepted(),
                "Tamaño fijo: |w| + 2 = " + cells + " celdas (incluye los delimitadores)" + move, animate);
    }
}
