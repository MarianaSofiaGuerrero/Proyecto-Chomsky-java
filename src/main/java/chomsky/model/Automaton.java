package chomsky.model;

import java.util.*;

/**
 * Clase base de todos los modelos computacionales del simulador.
 * Contiene la definición formal (estados, alfabeto, estado inicial,
 * estados de aceptación) y el estado de la ejecución paso a paso.
 */
public abstract class Automaton {
    /** Límite de seguridad para detectar posibles ciclos infinitos. */
    public static final int MAX_STEPS = 10000;

    protected String name;
    protected String description;
    protected final List<String> states = new ArrayList<>();
    protected String startState;
    protected final Set<String> acceptStates = new LinkedHashSet<>();
    protected final Set<Character> inputAlphabet = new LinkedHashSet<>();
    protected final List<Example> examples = new ArrayList<>();
    protected final LinkedHashMap<String, Edge> edges = new LinkedHashMap<>();
    /** Posiciones sugeridas (normalizadas 0..1) para el diagrama; opcional. */
    protected final Map<String, double[]> layout = new HashMap<>();

    // ---- estado de la ejecución ----
    protected String input = "";
    protected String currentState;
    protected boolean halted;
    protected boolean accepted;
    protected int steps;
    protected String lastAction = "";
    protected String haltReason = "";
    protected Edge lastEdge;
    protected int lastTransitionIndex = -1;

    protected Automaton(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ------------------------------------------------------------------
    // Construcción
    // ------------------------------------------------------------------
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }

    public void addState(String s) {
        if (s != null && !s.isEmpty() && !states.contains(s)) states.add(s);
    }

    public void setStart(String s) {
        startState = s;
        addState(s);
    }

    public void addAccept(String... ss) {
        for (String s : ss) {
            acceptStates.add(s);
            addState(s);
        }
    }

    public void setInputAlphabet(String symbols) {
        inputAlphabet.clear();
        for (char c : symbols.toCharArray()) {
            if (c != ',' && !Character.isWhitespace(c)) inputAlphabet.add(c);
        }
    }

    public void addExample(Example e) { examples.add(e); }

    /** Sugiere la posición (x, y en 0..1) de un estado en el diagrama. */
    public void place(String state, double x, double y) { layout.put(state, new double[]{x, y}); }

    public Map<String, double[]> getLayout() { return Collections.unmodifiableMap(layout); }

    protected Edge edgeFor(String from, String to, boolean inline) {
        return edges.computeIfAbsent(from + "\u0000" + to, k -> new Edge(from, to, inline));
    }

    // ------------------------------------------------------------------
    // Simulación
    // ------------------------------------------------------------------
    /** Prepara la configuración inicial para la cadena dada. */
    public final void reset(String in) {
        input = in == null ? "" : in;
        currentState = startState;
        halted = false;
        accepted = false;
        steps = 0;
        lastAction = "Configuración inicial";
        haltReason = "";
        lastEdge = null;
        lastTransitionIndex = -1;
        if (!inputAlphabet.isEmpty()) {
            for (char c : input.toCharArray()) {
                if (!inputAlphabet.contains(c)) {
                    onReset();
                    halt(false, "El símbolo '" + c + "' no pertenece al alfabeto de entrada Σ = "
                            + setOf(inputAlphabet) + ".");
                    lastAction = haltReason;
                    return;
                }
            }
        }
        onReset();
    }

    protected abstract void onReset();

    /** Ejecuta un paso. Devuelve false si la máquina ya estaba detenida. */
    public final boolean step() {
        if (halted) return false;
        if (steps >= MAX_STEPS) {
            halt(false, "Se alcanzó el límite de " + MAX_STEPS + " pasos (posible ciclo infinito).");
            return false;
        }
        steps++;
        lastAction = null;
        lastEdge = null;
        lastTransitionIndex = -1;
        onStep();
        if (lastAction == null) lastAction = "Sin transición aplicable: la máquina se detiene.";
        return true;
    }

    protected abstract void onStep();

    protected void halt(boolean acc, String reason) {
        halted = true;
        accepted = acc;
        haltReason = reason;
    }

    /** Ejecuta hasta que la máquina se detenga. */
    public void runToEnd() {
        while (step()) { /* nada */ }
    }

    // ------------------------------------------------------------------
    // Información
    // ------------------------------------------------------------------
    public abstract List<String> getTransitionRows();

    public String formalDefinition() {
        return "Q = " + setOf(states)
                + "    Σ = " + (inputAlphabet.isEmpty() ? "(cualquier símbolo)" : setOf(inputAlphabet))
                + "    Estado inicial = " + startState
                + "    F = " + setOf(acceptStates);
    }

    public static String setOf(Collection<?> items) {
        StringJoiner j = new StringJoiner(", ", "{", "}");
        for (Object o : items) {
            if (o instanceof Character) j.add(show((Character) o));
            else j.add(String.valueOf(o));
        }
        return j.toString();
    }

    /** Representación visible de un símbolo (el blanco se dibuja como □). */
    public static String show(char c) {
        if (c == TuringMachine.BLANK) return "□";
        return String.valueOf(c);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getStates() { return Collections.unmodifiableList(states); }
    public String getStartState() { return startState; }
    public Set<String> getAcceptStates() { return Collections.unmodifiableSet(acceptStates); }
    public Set<Character> getInputAlphabet() { return Collections.unmodifiableSet(inputAlphabet); }
    public List<Example> getExamples() { return Collections.unmodifiableList(examples); }
    public Collection<Edge> getEdges() { return Collections.unmodifiableCollection(edges.values()); }
    public String getInput() { return input; }
    public String getCurrentState() { return currentState; }
    public boolean isHalted() { return halted; }
    public boolean isAccepted() { return accepted; }
    public int getSteps() { return steps; }
    public String getLastAction() { return lastAction; }
    public String getHaltReason() { return haltReason; }
    public Edge getLastEdge() { return lastEdge; }
    public int getLastTransitionIndex() { return lastTransitionIndex; }

    /** Solo para máquinas que calculan funciones. */
    public String getOutput() { return null; }
    public String getOutputNote() { return null; }
}
