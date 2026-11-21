package chomsky.model;

import java.util.function.Function;
import java.util.function.Supplier;

/** Opción del selector de máquinas de un módulo (predefinida o personalizada). */
public final class MachineOption {
    public final String title;
    public final String template;
    private final Supplier<Automaton> factory;
    private final Function<String, Automaton> parser;

    private MachineOption(String title, Supplier<Automaton> factory, String template,
                          Function<String, Automaton> parser) {
        this.title = title;
        this.factory = factory;
        this.template = template;
        this.parser = parser;
    }

    public static MachineOption of(String title, Supplier<Automaton> factory) {
        return new MachineOption(title, factory, null, null);
    }

    public static MachineOption custom(String title, String template, Function<String, Automaton> parser) {
        return new MachineOption(title, null, template, parser);
    }

    public boolean isCustom() { return parser != null; }

    /** Crea una instancia nueva. Para opciones personalizadas interpreta {@code definition}. */
    public Automaton create(String definition) {
        if (isCustom()) return parser.apply(definition == null ? template : definition);
        return factory.get();
    }

    @Override
    public String toString() { return title; }
}
