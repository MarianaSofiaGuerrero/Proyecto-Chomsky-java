package chomsky.model;

import java.util.*;

/**
 * Autómata de Pila (Nivel 2 – lenguajes libres de contexto).
 * M = (Q, Σ, Γ, δ, q0, Z0, F). Acepta por estado final con la entrada consumida.
 *
 * Convenciones:
 *  - EPS   : transición ε (no consume entrada).
 *  - ANY   : comodín "X" para el tope de la pila; en la cadena a apilar, X
 *            se reemplaza por el símbolo que estaba en el tope.
 *  - BOTTOM: símbolo inicial de la pila Z0.
 * La cadena a apilar se escribe con el nuevo tope a la izquierda ("aZ" deja a en el tope).
 * Si hay una transición que consume el símbolo actual, tiene prioridad sobre una ε.
 */
public class PDA extends Automaton {
    public static final char EPS = '\0';
    public static final char ANY = '*';
    public static final char BOTTOM = 'Z';

    private static final class T {
        final String from, to, push;
        final char in, top;
        T(String from, char in, char top, String to, String push) {
            this.from = from; this.in = in; this.top = top; this.to = to; this.push = push;
        }
    }

    private final List<T> rows = new ArrayList<>();
    private final ArrayList<Character> stack = new ArrayList<>();
    private int pos;
    private String lastOperation = "";

    public PDA(String name, String description) {
        super(name, description);
    }

    public PDA t(String from, char in, char top, String to, String push) {
        T t = new T(from, in, top, to, push);
        rows.add(t);
        addState(from);
        addState(to);
        edgeFor(from, to, false).addLabel(sym(in) + ", " + sym(top) + " / " + str(push));
        return this;
    }

    public static String sym(char c) {
        if (c == BOTTOM) return "Z₀";
        if (c == ANY) return "X";
        if (c == EPS) return "ε";
        return String.valueOf(c);
    }

    public static String str(String s) {
        if (s.isEmpty()) return "ε";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) sb.append(sym(c));
        return sb.toString();
    }

    public int getPosition() { return pos; }

    /** Pila de abajo (índice 0) hacia arriba (último = tope). */
    public List<Character> getStack() { return new ArrayList<>(stack); }

    public String getLastOperation() { return lastOperation; }

    @Override
    protected void onReset() {
        stack.clear();
        stack.add(BOTTOM);
        pos = 0;
        lastOperation = "Pila inicial: Z₀";
        if (!halted) checkHalt();
    }

    private int find() {
        if (stack.isEmpty()) return -1;
        char top = stack.get(stack.size() - 1);
        if (pos < input.length()) {
            char c = input.charAt(pos);
            for (int i = 0; i < rows.size(); i++) {
                T t = rows.get(i);
                if (t.from.equals(currentState) && t.in == c && (t.top == top || t.top == ANY)) return i;
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            T t = rows.get(i);
            if (t.from.equals(currentState) && t.in == EPS && (t.top == top || t.top == ANY)) return i;
        }
        return -1;
    }

    private void checkHalt() {
        if (find() >= 0) return;
        boolean consumed = pos == input.length();
        String topSym = stack.isEmpty() ? "(pila vacía)" : sym(stack.get(stack.size() - 1));
        if (consumed && acceptStates.contains(currentState)) {
            halt(true, "Se consumió toda la cadena y " + currentState + " es estado de aceptación.");
        } else if (!consumed) {
            halt(false, "No hay transición para (" + currentState + ", '" + input.charAt(pos)
                    + "', tope " + topSym + "): quedan símbolos sin leer.");
        } else {
            halt(false, "Se consumió la cadena, pero " + currentState
                    + " no es estado de aceptación (tope " + topSym + ").");
        }
    }

    @Override
    protected void onStep() {
        int i = find();
        if (i < 0) {
            checkHalt();
            lastAction = haltReason;
            return;
        }
        T t = rows.get(i);
        char top = stack.remove(stack.size() - 1);
        String push = t.push.replace(ANY, top);
        for (int k = push.length() - 1; k >= 0; k--) stack.add(push.charAt(k));
        if (t.in != EPS) pos++;

        if (push.isEmpty()) lastOperation = "Desapila " + sym(top);
        else if (push.equals(String.valueOf(top))) lastOperation = "Sin cambios en la pila";
        else if (push.length() == 2 && push.charAt(1) == top) lastOperation = "Apila " + sym(push.charAt(0));
        else lastOperation = "Reemplaza " + sym(top) + " por " + str(push);

        lastAction = "δ(" + t.from + ", " + sym(t.in) + ", " + sym(top) + ") = (" + t.to + ", "
                + str(push) + ")   → " + lastOperation.toLowerCase();
        currentState = t.to;
        lastEdge = edgeFor(t.from, t.to, false);
        lastTransitionIndex = i;
        checkHalt();
    }

    @Override
    public List<String> getTransitionRows() {
        List<String> out = new ArrayList<>();
        for (T t : rows) {
            out.add("δ(" + t.from + ", " + sym(t.in) + ", " + sym(t.top) + ") = (" + t.to + ", " + str(t.push) + ")");
        }
        return out;
    }

    @Override
    public String formalDefinition() {
        Set<Character> gamma = new LinkedHashSet<>();
        gamma.add(BOTTOM);
        for (T t : rows) {
            if (t.top != ANY) gamma.add(t.top);
            for (char c : t.push.toCharArray()) if (c != ANY) gamma.add(c);
        }
        StringJoiner j = new StringJoiner(", ", "{", "}");
        for (char c : gamma) j.add(sym(c));
        return super.formalDefinition() + "    Γ (pila) = " + j + "    (X = cualquier símbolo de Γ)";
    }
}
