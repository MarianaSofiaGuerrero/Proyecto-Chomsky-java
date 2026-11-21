package chomsky.model;

/** Cadena de prueba incluida en la interfaz, con el resultado esperado. */
public final class Example {
    public final String input;
    /** true = se espera aceptación, false = rechazo, null = desconocido. */
    public final Boolean accept;
    public final String note;
    /** Para máquinas que calculan una función: contenido esperado de la cinta. */
    public final String expectedOutput;

    public Example(String input, Boolean accept, String note) {
        this(input, accept, note, null);
    }

    public Example(String input, Boolean accept, String note, String expectedOutput) {
        this.input = input == null ? "" : input;
        this.accept = accept;
        this.note = note == null ? "" : note;
        this.expectedOutput = expectedOutput;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(input.isEmpty() ? "ε (vacía)" : input);
        if (expectedOutput != null) {
            sb.append("  →  ").append(expectedOutput.isEmpty() ? "ε" : expectedOutput);
        } else if (accept != null) {
            sb.append(accept ? "   ✓ acepta" : "   ✗ rechaza");
        }
        if (!note.isEmpty()) sb.append("   (").append(note).append(")");
        return sb.toString();
    }
}
