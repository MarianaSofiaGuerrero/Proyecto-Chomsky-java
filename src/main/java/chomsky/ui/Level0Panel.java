package chomsky.ui;

import chomsky.model.Automaton;
import chomsky.model.Catalog;
import chomsky.model.TuringMachine;

import javax.swing.*;

/** Nivel 0: Máquina de Turing. */
public class Level0Panel extends BaseModulePanel {
    private TapePanel tape;

    public Level0Panel(Runnable onBack) {
        super(Theme.L0, "Nivel 0 · Lenguajes recursivamente enumerables (Tipo 0)",
                "Máquina de Turing (MT): cinta infinita en ambos sentidos, cabezal de lectura/escritura y estado de control",
                Catalog.level0(), onBack);
        build();
    }

    @Override
    protected JComponent createVisualization() {
        tape = new TapePanel(TapePanel.Mode.INFINITE, accent);
        return tape;
    }

    @Override
    protected void onMachineChanged() {
        TuringMachine m = (TuringMachine) automaton;
        tape.setCaption("Cinta infinita  ·  Γ = " + Automaton.setOf(m.getTapeAlphabet())
                + (m.isFunction() ? "  ·  calcula una función" : "  ·  reconoce un lenguaje"));
    }

    @Override
    protected void refreshVisualization(boolean animate) {
        TuringMachine m = (TuringMachine) automaton;
        StringBuilder f = new StringBuilder("Celdas usadas: " + m.getTape().size());
        f.append("  ·  posición del cabezal: ").append(m.getHead() - m.getOrigin());
        if (m.getLastMove() == 'L') f.append("  ·  último movimiento: ← izquierda");
        else if (m.getLastMove() == 'R') f.append("  ·  último movimiento: → derecha");
        if (m.isFunction() && m.isHalted() && m.isAccepted()) {
            String out = m.getOutput();
            String note = m.getOutputNote();
            f.append("  ·  SALIDA: ").append(out.isEmpty() ? "ε" : out);
            if (note != null && !note.isEmpty()) f.append(" (").append(note).append(")");
        }
        tape.setContent(m.getTape(), m.getHead(), m.getOrigin(), m.getCurrentState(), m.isHalted(), m.isAccepted(),
                f.toString(), animate);
    }
}
