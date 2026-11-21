package chomsky.ui;

import chomsky.model.Catalog;
import chomsky.model.PDA;

import javax.swing.*;

/** Nivel 2: Autómata de Pila. */
public class Level2Panel extends BaseModulePanel {
    private TapePanel tape;
    private StackPanel stack;

    public Level2Panel(Runnable onBack) {
        super(Theme.L2, "Nivel 2 · Lenguajes libres de contexto (Tipo 2)",
                "Autómata de Pila (AP): memoria LIFO ilimitada; se observa cada push / pop mientras se consume la cadena",
                Catalog.level2(), onBack);
        build();
    }

    @Override
    protected JComponent createSidePanel() {
        stack = new StackPanel(accent);
        return stack;
    }

    @Override
    protected JComponent createVisualization() {
        tape = new TapePanel(TapePanel.Mode.INPUT, accent);
        tape.setCaption("Cinta de entrada (las transiciones ε no mueven el cabezal)");
        return tape;
    }

    @Override
    protected void refreshVisualization(boolean animate) {
        PDA p = (PDA) automaton;
        tape.setContent(TapePanel.chars(p.getInput()), p.getPosition(), 0, p.getCurrentState(),
                p.isHalted(), p.isAccepted(), "Contenido de la pila (tope a la izquierda): "
                        + stackString(p), animate);
        stack.setStack(p.getStack(), p.getLastOperation(), animate);
    }

    private static String stackString(PDA p) {
        StringBuilder sb = new StringBuilder();
        java.util.List<Character> s = p.getStack();
        for (int i = s.size() - 1; i >= 0; i--) sb.append(PDA.sym(s.get(i)));
        return sb.length() == 0 ? "(vacía)" : sb.toString();
    }
}
