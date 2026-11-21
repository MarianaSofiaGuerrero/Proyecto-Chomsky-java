package chomsky.model;

import java.util.*;
import java.util.function.Function;

/**
 * Máquina de Turing determinista de una cinta.
 *
 *  - bounded = false: Máquina de Turing estándar (Nivel 0). La cinta es
 *    infinita en ambos sentidos: se extiende con blancos (□) cuando el
 *    cabezal sale del contenido actual.
 *  - bounded = true: Autómata Linealmente Acotado (Nivel 1). La cinta
 *    contiene  &lt; w &gt;  y el cabezal nunca puede salir de los
 *    delimitadores ni sobrescribirlos.
 *
 * Se detiene y acepta al entrar en un estado de aceptación; se detiene y
 * rechaza cuando no existe transición definida.
 */
public class TuringMachine extends Automaton {
    public static final char BLANK = '_';
    public static final char LEFT = '<';
    public static final char RIGHT = '>';
    /** En t(): escribir el mismo símbolo que se leyó. */
    public static final char SAME = '\u0001';

    private static final class T {
        final String from, to;
        final char read, write, move;
        T(String from, char read, String to, char write, char move) {
            this.from = from; this.read = read; this.to = to; this.write = write; this.move = move;
        }
    }

    private final List<T> rows = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();
    private final boolean bounded;
    private boolean function;
    private Function<String, String> outputNote;

    private final ArrayList<Character> tape = new ArrayList<>();
    private int head;
    private int origin;
    private char lastMove = ' ';

    public TuringMachine(String name, String description, boolean bounded) {
        super(name, description);
        this.bounded = bounded;
    }

    /** Agrega δ(from, r) = (to, write, move) para cada símbolo r de {@code reads}. */
    public TuringMachine t(String from, String reads, String to, char write, char move) {
        move = normalizeMove(move);
        addState(from);
        addState(to);
        for (char r : reads.toCharArray()) {
            String key = from + "|" + r;
            if (index.containsKey(key)) {
                throw new IllegalArgumentException("Transición duplicada δ(" + from + ", " + show(r)
                        + "): la máquina debe ser determinista.");
            }
            char w = write == SAME ? r : write;
            index.put(key, rows.size());
            rows.add(new T(from, r, to, w, move));
            edgeFor(from, to, false).addLabel(show(r) + "/" + show(w) + "," + move);
        }
        return this;
    }

    static char normalizeMove(char m) {
        switch (Character.toUpperCase(m)) {
            case 'L': case 'I': case '<': return 'L';
            case 'R': case 'D': case '>': return 'R';
            case 'S': case 'N': case '-': return 'S';
            default: throw new IllegalArgumentException("Movimiento inválido '" + m + "' (use L, R o S).");
        }
    }

    public TuringMachine asFunction(Function<String, String> note) {
        this.function = true;
        this.outputNote = note;
        return this;
    }

    public boolean isBounded() { return bounded; }
    public boolean isFunction() { return function; }
    public List<Character> getTape() { return new ArrayList<>(tape); }
    public int getHead() { return head; }
    /** Índice de la cinta donde comenzaba la entrada (posición 0). */
    public int getOrigin() { return origin; }
    public char getLastMove() { return lastMove; }

    @Override
    protected void onReset() {
        tape.clear();
        lastMove = ' ';
        if (bounded) {
            tape.add(LEFT);
            for (char c : input.toCharArray()) tape.add(c);
            tape.add(RIGHT);
            head = 1;
            origin = 1;
        } else {
            for (char c : input.toCharArray()) tape.add(c);
            if (tape.isEmpty()) tape.add(BLANK);
            head = 0;
            origin = 0;
        }
        if (!halted) checkHalt();
    }

    private void checkHalt() {
        if (acceptStates.contains(currentState)) {
            halt(true, "La máquina alcanzó el estado de aceptación " + currentState + ".");
        } else if (!index.containsKey(currentState + "|" + tape.get(head))) {
            halt(false, "No existe δ(" + currentState + ", " + show(tape.get(head))
                    + "): la máquina se detiene en un estado que no es de aceptación.");
        }
    }

    @Override
    protected void onStep() {
        char r = tape.get(head);
        Integer i = index.get(currentState + "|" + r);
        if (i == null) {
            checkHalt();
            lastAction = haltReason;
            return;
        }
        T t = rows.get(i);
        if (bounded) {
            if ((r == LEFT || r == RIGHT) && t.write != r) {
                halt(false, "Violación de la cinta acotada: no se puede sobrescribir el delimitador " + r + ".");
                lastAction = haltReason;
                return;
            }
            if ((head == 0 && t.move == 'L') || (head == tape.size() - 1 && t.move == 'R')) {
                halt(false, "Violación de la cinta acotada: el cabezal intentó salir de los delimitadores.");
                lastAction = haltReason;
                return;
            }
        }
        tape.set(head, t.write);
        switch (t.move) {
            case 'R':
                head++;
                if (!bounded && head == tape.size()) tape.add(BLANK);
                break;
            case 'L':
                if (!bounded && head == 0) {
                    tape.add(0, BLANK);
                    origin++;
                } else {
                    head--;
                }
                break;
            default:
                break;
        }
        lastMove = t.move;
        String mv = t.move == 'R' ? "mueve →" : t.move == 'L' ? "mueve ←" : "no se mueve";
        lastAction = "δ(" + t.from + ", " + show(r) + ") = (" + t.to + ", " + show(t.write) + ", " + t.move
                + ")   escribe " + show(t.write) + ", " + mv;
        currentState = t.to;
        lastEdge = edgeFor(t.from, t.to, false);
        lastTransitionIndex = i;
        checkHalt();
    }

    @Override
    public String getOutput() {
        if (!function) return null;
        int a = 0, b = tape.size() - 1;
        while (a <= b && tape.get(a) == BLANK) a++;
        while (b >= a && tape.get(b) == BLANK) b--;
        StringBuilder sb = new StringBuilder();
        for (int k = a; k <= b; k++) sb.append(show(tape.get(k)));
        return sb.toString();
    }

    @Override
    public String getOutputNote() {
        if (!function || outputNote == null) return null;
        return outputNote.apply(getOutput());
    }

    public Set<Character> getTapeAlphabet() {
        Set<Character> g = new LinkedHashSet<>(inputAlphabet);
        for (T t : rows) {
            g.add(t.read);
            g.add(t.write);
        }
        if (bounded) {
            g.remove(LEFT);
            g.remove(RIGHT);
            g.add(LEFT);
            g.add(RIGHT);
        } else {
            g.remove(BLANK);
            g.add(BLANK);
        }
        return g;
    }

    @Override
    public List<String> getTransitionRows() {
        List<String> out = new ArrayList<>();
        for (T t : rows) {
            out.add("δ(" + t.from + ", " + show(t.read) + ") = (" + t.to + ", " + show(t.write) + ", " + t.move + ")");
        }
        return out;
    }

    @Override
    public String formalDefinition() {
        return super.formalDefinition() + "    Γ (cinta) = " + setOf(getTapeAlphabet())
                + (bounded ? "    Delimitadores: < y >" : "    Blanco: □");
    }
}
