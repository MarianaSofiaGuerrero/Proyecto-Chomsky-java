package chomsky;

import chomsky.model.*;

import java.util.List;

/**
 * Pruebas automáticas en consola: ejecuta todas las cadenas de prueba
 * incluidas en la interfaz y verifica el resultado esperado.
 *
 * Uso:  java -cp out chomsky.Pruebas
 */
public final class Pruebas {
    public static void main(String[] args) {
        int total = 0, fails = 0;
        String[] titles = {"NIVEL 3 · AFD", "NIVEL 2 · Autómata de pila", "NIVEL 1 · ALA", "NIVEL 0 · Máquina de Turing"};
        List<List<MachineOption>> levels = List.of(Catalog.level3(), Catalog.level2(), Catalog.level1(), Catalog.level0());
        for (int l = 0; l < levels.size(); l++) {
            System.out.println("\n=== " + titles[l] + " ===");
            for (MachineOption opt : levels.get(l)) {
                Automaton m = opt.create(null);
                System.out.println("-- " + m.getName());
                for (Example ex : m.getExamples()) {
                    m.reset(ex.input);
                    m.runToEnd();
                    boolean ok = ex.accept == null || ex.accept == m.isAccepted();
                    if (ex.expectedOutput != null) ok &= ex.expectedOutput.equals(m.getOutput());
                    total++;
                    if (!ok) fails++;
                    String w = ex.input.isEmpty() ? "ε" : ex.input;
                    String out = m.getOutput() != null ? "  salida=" + m.getOutput() : "";
                    System.out.printf("   %-4s %-12s %-9s pasos=%-4d%s%n", ok ? "OK" : "FALLA", w,
                            m.isAccepted() ? "ACEPTA" : "RECHAZA", m.getSteps(), out);
                }
            }
        }
        System.out.println("\nResultado: " + (total - fails) + "/" + total + " pruebas correctas.");
        System.exit(fails == 0 ? 0 : 1);
    }
}
