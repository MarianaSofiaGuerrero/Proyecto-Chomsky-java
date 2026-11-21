package chomsky.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Arista del diagrama de estados. Agrupa todas las transiciones que van
 * de un mismo estado origen a un mismo estado destino.
 */
public final class Edge {
    public final String from;
    public final String to;
    /** true: las etiquetas se muestran en una línea separadas por coma (AFD). */
    public final boolean inline;
    private final List<String> labels = new ArrayList<>();

    Edge(String from, String to, boolean inline) {
        this.from = from;
        this.to = to;
        this.inline = inline;
    }

    void addLabel(String label) {
        if (!labels.contains(label)) labels.add(label);
    }

    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public boolean isLoop() {
        return from.equals(to);
    }
}
