package chomsky.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta las definiciones textuales que el usuario escribe para
 * los autómatas personalizados (AFD del Nivel 3 y MT del Nivel 0).
 *
 * Formato común (una instrucción por línea, # = comentario):
 * <pre>
 *   nombre: Mi autómata
 *   inicial: q0
 *   finales: q1, q2
 *   alfabeto: a,b          (opcional)
 *   prueba: abba           (opcional, aparece en "Cadenas de prueba")
 *   AFD:  q0,a -> q1
 *   MT :  q0,a -> q1,X,R   (movimiento L, R o S; blanco = _ )
 *   funcion: si            (solo MT: muestra la salida de la cinta)
 * </pre>
 */
public final class Parser {
    private static final Pattern DFA_T = Pattern.compile("^(\\S+?)\\s*,\\s*(\\S)\\s*->\\s*(\\S+)$");
    private static final Pattern TM_T = Pattern.compile("^(\\S+?)\\s*,\\s*(\\S)\\s*->\\s*(\\S+?)\\s*,\\s*(\\S)\\s*,\\s*(\\S)$");

    private Parser() {}

    public static Automaton parseDFA(String text) {
        DFA d = new DFA("AFD personalizado", "Autómata definido por el usuario.");
        String start = null;
        String finals = "";
        String alphabet = null;
        int n = 0;
        java.util.List<String[]> trans = new java.util.ArrayList<>();
        java.util.List<String> tests = new java.util.ArrayList<>();
        String[] lines = text.split("\\R");
        for (int ln = 0; ln < lines.length; ln++) {
            String line = lines[ln].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String low = line.toLowerCase();
            if (low.startsWith("nombre:")) d.setName(value(line));
            else if (low.startsWith("descripcion:") || low.startsWith("descripción:")) d.setDescription(value(line));
            else if (low.startsWith("inicial:")) start = value(line);
            else if (low.startsWith("finales:")) finals = value(line);
            else if (low.startsWith("alfabeto:")) alphabet = value(line);
            else if (low.startsWith("prueba:")) tests.add(value(line));
            else {
                Matcher m = DFA_T.matcher(line);
                if (!m.matches()) {
                    throw new IllegalArgumentException("Línea " + (ln + 1) + " no reconocida: \"" + line
                            + "\"\nFormato de transición esperado:  estado,símbolo -> estado");
                }
                trans.add(new String[]{m.group(1), m.group(2), m.group(3)});
                n++;
            }
        }
        if (start == null || start.isEmpty()) throw new IllegalArgumentException("Falta la línea  inicial: <estado>");
        if (n == 0) throw new IllegalArgumentException("Debe definir al menos una transición.");
        d.setStart(start);
        for (String f : finals.split(",")) if (!f.trim().isEmpty()) d.addAccept(f.trim());
        for (String[] t : trans) d.t(t[0], t[1], t[2]);
        if (alphabet != null && !alphabet.isEmpty()) d.setInputAlphabet(alphabet);
        else d.inferAlphabet();
        for (String s : tests) d.addExample(new Example(s, null, ""));
        return d;
    }

    public static Automaton parseTM(String text) {
        TuringMachine tm = new TuringMachine("MT personalizada", "Máquina de Turing definida por el usuario.", false);
        String start = null;
        String finals = "";
        String alphabet = null;
        boolean function = false;
        java.util.List<String[]> trans = new java.util.ArrayList<>();
        java.util.List<String> tests = new java.util.ArrayList<>();
        String[] lines = text.split("\\R");
        for (int ln = 0; ln < lines.length; ln++) {
            String line = lines[ln].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String low = line.toLowerCase();
            if (low.startsWith("nombre:")) tm.setName(value(line));
            else if (low.startsWith("descripcion:") || low.startsWith("descripción:")) tm.setDescription(value(line));
            else if (low.startsWith("inicial:")) start = value(line);
            else if (low.startsWith("finales:")) finals = value(line);
            else if (low.startsWith("alfabeto:") || low.startsWith("entrada:")) alphabet = value(line);
            else if (low.startsWith("funcion:") || low.startsWith("función:")) {
                String v = value(line).toLowerCase();
                function = v.startsWith("s") || v.startsWith("y") || v.equals("true");
            } else if (low.startsWith("prueba:")) tests.add(value(line));
            else {
                Matcher m = TM_T.matcher(line);
                if (!m.matches()) {
                    throw new IllegalArgumentException("Línea " + (ln + 1) + " no reconocida: \"" + line
                            + "\"\nFormato de transición esperado:  estado,lee -> estado,escribe,movimiento");
                }
                trans.add(new String[]{m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)});
            }
        }
        if (start == null || start.isEmpty()) throw new IllegalArgumentException("Falta la línea  inicial: <estado>");
        if (trans.isEmpty()) throw new IllegalArgumentException("Debe definir al menos una transición.");
        tm.setStart(start);
        for (String f : finals.split(",")) if (!f.trim().isEmpty()) tm.addAccept(f.trim());
        for (String[] t : trans) {
            tm.t(t[0], String.valueOf(blank(t[1].charAt(0))), t[2], blank(t[3].charAt(0)), t[4].charAt(0));
        }
        if (alphabet != null && !alphabet.isEmpty()) tm.setInputAlphabet(alphabet);
        if (function) tm.asFunction(null);
        for (String s : tests) tm.addExample(new Example(s, null, ""));
        return tm;
    }

    private static char blank(char c) {
        return c == '□' ? TuringMachine.BLANK : c;
    }

    private static String value(String line) {
        int i = line.indexOf(':');
        return line.substring(i + 1).trim();
    }
}
