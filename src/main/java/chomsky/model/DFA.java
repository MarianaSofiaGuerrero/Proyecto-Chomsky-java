package chomsky.model;

import java.util.*;

/**
 * Autómata Finito Determinista (Nivel 3 – lenguajes regulares).
 * M = (Q, Σ, δ, q0, F) con δ : Q × Σ → Q.
 * Si falta una transición se asume un estado de rechazo implícito.
 */
public class DFA extends Automaton {

    private static final class T {
        final String from, to;
        final char symbol;
        T(String from, char symbol, String to) {
            this.from = from; this.symbol = symbol; this.to = to;
        }
    }

    private final List<T> rows = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();
    private int pos;

    public DFA(String name, String description) {
        super(name, description);
    }

    /** Agrega δ(from, c) = to para cada símbolo c de {@code symbols}. */
    public DFA t(String from, String symbols, String to) {
        addState(from);
        addState(to);
        for (char c : symbols.toCharArray()) {
            String key = from + "|" + c;
            if (index.containsKey(key)) {
                throw new IllegalArgumentException("Transición duplicada δ(" + from + ", " + c
                        + "): un AFD debe ser determinista.");
            }
            index.put(key, rows.size());
            rows.add(new T(from, c, to));
            edgeFor(from, to, true).addLabel(String.valueOf(c));
        }
        return this;
    }

    /** Alfabeto inferido a partir de las transiciones (para AFD personalizados). */
    public void inferAlphabet() {
        StringBuilder sb = new StringBuilder();
        for (T t : rows) sb.append(t.symbol);
        setInputAlphabet(sb.toString());
    }

    public int getPosition() { return pos; }

    @Override
    protected void onReset() {
        pos = 0;
        if (!halted && input.isEmpty()) finish();
    }

    private void finish() {
        if (acceptStates.contains(currentState)) {
            halt(true, "Se leyó toda la cadena y " + currentState + " es estado de aceptación.");
        } else {
            halt(false, "Se leyó toda la cadena, pero " + currentState + " no es estado de aceptación.");
        }
    }

    @Override
    protected void onStep() {
        char c = input.charAt(pos);
        Integer i = index.get(currentState + "|" + c);
        if (i == null) {
            halt(false, "No existe la transición δ(" + currentState + ", " + c + "): la cadena se rechaza.");
            lastAction = haltReason;
            return;
        }
        T t = rows.get(i);
        lastTransitionIndex = i;
        lastEdge = edgeFor(t.from, t.to, true);
        lastAction = "δ(" + t.from + ", " + c + ") = " + t.to
                + "    [símbolo " + (pos + 1) + " de " + input.length() + "]";
        currentState = t.to;
        pos++;
        if (pos == input.length()) finish();
    }

    @Override
    public List<String> getTransitionRows() {
        List<String> out = new ArrayList<>();
        for (T t : rows) out.add("δ(" + t.from + ", " + t.symbol + ") = " + t.to);
        return out;
    }
}
