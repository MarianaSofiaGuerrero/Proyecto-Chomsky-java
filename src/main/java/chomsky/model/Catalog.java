package chomsky.model;

import java.util.List;

import static chomsky.model.TuringMachine.BLANK;
import static chomsky.model.TuringMachine.SAME;

/** Máquinas predefinidas (y plantillas personalizables) de cada nivel. */
public final class Catalog {
    private Catalog() {}

    // =====================================================================
    // NIVEL 3 – Autómatas finitos deterministas
    // =====================================================================
    public static List<MachineOption> level3() {
        return List.of(
                MachineOption.of("Número par de ceros  (Σ = {0,1})", Catalog::evenZeros),
                MachineOption.of("Termina en \"ab\"  (Σ = {a,b})", Catalog::endsWithAb),
                MachineOption.of("Binarios múltiplos de 3", Catalog::multipleOf3),
                MachineOption.of("Contiene la subcadena \"aba\"", Catalog::containsAba),
                MachineOption.custom("✎ AFD personalizado (parametrizable)", DFA_TEMPLATE, Parser::parseDFA));
    }

    static DFA evenZeros() {
        DFA d = new DFA("Número par de ceros",
                "L = { w ∈ {0,1}* | w contiene un número par de ceros }. "
                        + "q0 recuerda \"llevo un número par de ceros\" y q1 \"llevo un número impar\".");
        d.setStart("q0");
        d.addAccept("q0");
        d.setInputAlphabet("01");
        d.t("q0", "0", "q1").t("q0", "1", "q0").t("q1", "0", "q0").t("q1", "1", "q1");
        d.addExample(new Example("1001", true, "2 ceros"));
        d.addExample(new Example("0110", true, "2 ceros"));
        d.addExample(new Example("111", true, "0 ceros"));
        d.addExample(new Example("", true, "0 ceros"));
        d.addExample(new Example("0", false, "1 cero"));
        d.addExample(new Example("1000", false, "3 ceros"));
        d.addExample(new Example("1021", false, "'2' ∉ Σ"));
        return d;
    }

    static DFA endsWithAb() {
        DFA d = new DFA("Cadenas que terminan en \"ab\"",
                "L = { w ∈ {a,b}* | w termina en ab }. Equivale a la expresión regular (a|b)*ab.");
        d.setStart("q0");
        d.addState("q1");
        d.addAccept("q2");
        d.setInputAlphabet("ab");
        d.t("q0", "a", "q1").t("q0", "b", "q0")
                .t("q1", "a", "q1").t("q1", "b", "q2")
                .t("q2", "a", "q1").t("q2", "b", "q0");
        d.addExample(new Example("ab", true, ""));
        d.addExample(new Example("aab", true, ""));
        d.addExample(new Example("babab", true, ""));
        d.addExample(new Example("ba", false, ""));
        d.addExample(new Example("abba", false, ""));
        d.addExample(new Example("", false, ""));
        return d;
    }

    static DFA multipleOf3() {
        DFA d = new DFA("Números binarios múltiplos de 3",
                "L = { w ∈ {0,1}* | el valor binario de w es divisible entre 3 }. "
                        + "El estado qi representa el residuo i del prefijo leído: δ(qi, b) = q((2i + b) mod 3).");
        d.setStart("q0");
        d.addAccept("q0");
        d.addState("q1");
        d.addState("q2");
        d.setInputAlphabet("01");
        d.t("q0", "0", "q0").t("q0", "1", "q1")
                .t("q1", "0", "q2").t("q1", "1", "q0")
                .t("q2", "0", "q1").t("q2", "1", "q2");
        d.addExample(new Example("11", true, "3"));
        d.addExample(new Example("110", true, "6"));
        d.addExample(new Example("1001", true, "9"));
        d.addExample(new Example("1111", true, "15"));
        d.addExample(new Example("10", false, "2"));
        d.addExample(new Example("111", false, "7"));
        d.addExample(new Example("1010", false, "10"));
        return d;
    }

    static DFA containsAba() {
        DFA d = new DFA("Contiene la subcadena \"aba\"",
                "L = { w ∈ {a,b}* | aba es subcadena de w }. Expresión regular: (a|b)*aba(a|b)*.");
        d.setStart("q0");
        d.addState("q1");
        d.addState("q2");
        d.addAccept("q3");
        d.setInputAlphabet("ab");
        d.t("q0", "a", "q1").t("q0", "b", "q0")
                .t("q1", "a", "q1").t("q1", "b", "q2")
                .t("q2", "a", "q3").t("q2", "b", "q0")
                .t("q3", "ab", "q3");
        d.addExample(new Example("aba", true, ""));
        d.addExample(new Example("bbabab", true, ""));
        d.addExample(new Example("aabaa", true, ""));
        d.addExample(new Example("abba", false, ""));
        d.addExample(new Example("bbb", false, ""));
        d.addExample(new Example("ab", false, ""));
        return d;
    }

    public static final String DFA_TEMPLATE = String.join("\n",
            "# AFD personalizado. Edite esta definición y presione Aceptar.",
            "# Formato de transición:   estado,símbolo -> estado",
            "# Las transiciones que falten van a un estado de rechazo implícito.",
            "nombre: Número impar de b",
            "descripcion: L = { w ∈ {a,b}* | w tiene un número impar de b }",
            "inicial: p",
            "finales: i",
            "alfabeto: a,b",
            "p,a -> p",
            "p,b -> i",
            "i,a -> i",
            "i,b -> p",
            "prueba: ab",
            "prueba: babb",
            "prueba: abab",
            "prueba: aaa");

    // =====================================================================
    // NIVEL 2 – Autómatas de pila
    // =====================================================================
    public static List<MachineOption> level2() {
        return List.of(
                MachineOption.of("aⁿbⁿ  (n ≥ 0)", Catalog::anbn),
                MachineOption.of("Paréntesis y corchetes balanceados", Catalog::balanced),
                MachineOption.of("Palíndromos con marca central  w c wᴿ", Catalog::wcwr),
                MachineOption.of("Igual número de a y de b", Catalog::equalAB));
    }

    static PDA anbn() {
        PDA p = new PDA("aⁿbⁿ (n ≥ 0)",
                "L = { aⁿbⁿ | n ≥ 0 }. Cada a se apila; cada b desapila una a. "
                        + "No es regular: un AF no puede contar sin límite, la pila sí.");
        p.setStart("q0");
        p.addState("q1");
        p.addAccept("q2");
        p.setInputAlphabet("ab");
        p.t("q0", 'a', PDA.ANY, "q0", "a*")
                .t("q0", 'b', 'a', "q1", "")
                .t("q0", PDA.EPS, PDA.BOTTOM, "q2", "Z")
                .t("q1", 'b', 'a', "q1", "")
                .t("q1", PDA.EPS, PDA.BOTTOM, "q2", "Z");
        p.addExample(new Example("aabb", true, ""));
        p.addExample(new Example("ab", true, ""));
        p.addExample(new Example("aaabbb", true, ""));
        p.addExample(new Example("", true, "n = 0"));
        p.addExample(new Example("aab", false, "sobran a"));
        p.addExample(new Example("abb", false, "sobran b"));
        p.addExample(new Example("abab", false, ""));
        return p;
    }

    static PDA balanced() {
        PDA p = new PDA("Paréntesis y corchetes balanceados",
                "L = { w ∈ {(, ), [, ]}* | w está correctamente anidada }. "
                        + "Cada símbolo de apertura se apila y cada cierre debe coincidir con el tope.");
        p.setStart("q0");
        p.addAccept("q1");
        p.setInputAlphabet("()[]");
        p.t("q0", '(', PDA.ANY, "q0", "(*")
                .t("q0", '[', PDA.ANY, "q0", "[*")
                .t("q0", ')', '(', "q0", "")
                .t("q0", ']', '[', "q0", "")
                .t("q0", PDA.EPS, PDA.BOTTOM, "q1", "Z");
        p.addExample(new Example("([])", true, ""));
        p.addExample(new Example("()", true, ""));
        p.addExample(new Example("[()()]", true, ""));
        p.addExample(new Example("(()[])", true, ""));
        p.addExample(new Example("(]", false, "no coinciden"));
        p.addExample(new Example("(()", false, "falta cerrar"));
        p.addExample(new Example("())(", false, ""));
        return p;
    }

    static PDA wcwr() {
        PDA p = new PDA("Palíndromos con marca central  w c wᴿ",
                "L = { w c wᴿ | w ∈ {a,b}* }. Antes de la c se apila w; después se compara wᴿ desapilando.");
        p.setStart("q0");
        p.addState("q1");
        p.addAccept("q2");
        p.setInputAlphabet("abc");
        p.t("q0", 'a', PDA.ANY, "q0", "a*")
                .t("q0", 'b', PDA.ANY, "q0", "b*")
                .t("q0", 'c', PDA.ANY, "q1", "*")
                .t("q1", 'a', 'a', "q1", "")
                .t("q1", 'b', 'b', "q1", "")
                .t("q1", PDA.EPS, PDA.BOTTOM, "q2", "Z");
        p.addExample(new Example("abcba", true, ""));
        p.addExample(new Example("c", true, "w = ε"));
        p.addExample(new Example("abbcbba", true, ""));
        p.addExample(new Example("aacaa", true, ""));
        p.addExample(new Example("abcab", false, ""));
        p.addExample(new Example("abba", false, "sin marca c"));
        p.addExample(new Example("acaa", false, ""));
        return p;
    }

    static PDA equalAB() {
        PDA p = new PDA("Igual número de a y de b",
                "L = { w ∈ {a,b}* | #a(w) = #b(w) }. La pila guarda el exceso: "
                        + "si llega una a con b en el tope se cancelan (y viceversa).");
        p.setStart("q0");
        p.addAccept("q1");
        p.setInputAlphabet("ab");
        p.t("q0", 'a', PDA.BOTTOM, "q0", "aZ")
                .t("q0", 'a', 'a', "q0", "aa")
                .t("q0", 'a', 'b', "q0", "")
                .t("q0", 'b', PDA.BOTTOM, "q0", "bZ")
                .t("q0", 'b', 'b', "q0", "bb")
                .t("q0", 'b', 'a', "q0", "")
                .t("q0", PDA.EPS, PDA.BOTTOM, "q1", "Z");
        p.addExample(new Example("abba", true, ""));
        p.addExample(new Example("ba", true, ""));
        p.addExample(new Example("aabbba", true, ""));
        p.addExample(new Example("", true, ""));
        p.addExample(new Example("aab", false, ""));
        p.addExample(new Example("bbbaa", false, ""));
        return p;
    }

    // =====================================================================
    // NIVEL 1 – Autómatas linealmente acotados
    // =====================================================================
    public static List<MachineOption> level1() {
        return List.of(
                MachineOption.of("aⁿbⁿcⁿ  (n ≥ 1)", Catalog::anbncn),
                MachineOption.of("Lenguaje copia  w#w", Catalog::copyLanguage));
    }

    static TuringMachine anbncn() {
        TuringMachine m = new TuringMachine("aⁿbⁿcⁿ (n ≥ 1)",
                "L = { aⁿbⁿcⁿ | n ≥ 1 } no es libre de contexto (lema de bombeo). En cada pasada el ALA "
                        + "marca una a→X, una b→Y y una c→Z, y regresa; al final verifica que solo queden marcas.",
                true);
        m.setStart("q0");
        for (String s : new String[]{"q1", "q2", "q3", "q4", "q5"}) m.addState(s);
        m.addAccept("qA");
        m.setInputAlphabet("abc");
        m.t("q0", "a", "q1", 'X', 'R');
        m.t("q0", "Y", "q4", 'Y', 'R');
        m.t("q1", "aY", "q1", SAME, 'R');
        m.t("q1", "b", "q2", 'Y', 'R');
        m.t("q2", "bZ", "q2", SAME, 'R');
        m.t("q2", "c", "q3", 'Z', 'L');
        m.t("q3", "abYZ", "q3", SAME, 'L');
        m.t("q3", "X", "q0", 'X', 'R');
        m.t("q4", "Y", "q4", 'Y', 'R');
        m.t("q4", "Z", "q5", 'Z', 'R');
        m.t("q5", "Z", "q5", 'Z', 'R');
        m.t("q5", ">", "qA", '>', 'L');
        m.place("q0", 0, 0.45); m.place("q1", 0.33, 0); m.place("q2", 0.66, 0); m.place("q3", 1, 0.45);
        m.place("q4", 0.33, 1); m.place("q5", 0.66, 1); m.place("qA", 1, 1);
        m.addExample(new Example("aabbcc", true, ""));
        m.addExample(new Example("abc", true, ""));
        m.addExample(new Example("aaabbbccc", true, ""));
        m.addExample(new Example("aabbc", false, "falta una c"));
        m.addExample(new Example("abcabc", false, ""));
        m.addExample(new Example("aabcc", false, ""));
        m.addExample(new Example("acb", false, "orden incorrecto"));
        return m;
    }

    static TuringMachine copyLanguage() {
        TuringMachine m = new TuringMachine("Lenguaje copia  w#w",
                "L = { w#w | w ∈ {a,b}* } no es libre de contexto. El ALA toma el primer símbolo no marcado "
                        + "a la izquierda de #, lo marca con X, viaja a la derecha y comprueba que el primer "
                        + "símbolo no marcado después de # sea igual; repite hasta agotar w.",
                true);
        m.setStart("q0");
        for (String s : new String[]{"q1", "q2", "q3", "q4", "q5", "q6", "q7"}) m.addState(s);
        m.addAccept("qA");
        m.setInputAlphabet("ab#");
        m.t("q0", "a", "q1", 'X', 'R');
        m.t("q0", "b", "q2", 'X', 'R');
        m.t("q0", "#", "q6", '#', 'R');
        m.t("q1", "ab", "q1", SAME, 'R');
        m.t("q1", "#", "q3", '#', 'R');
        m.t("q2", "ab", "q2", SAME, 'R');
        m.t("q2", "#", "q4", '#', 'R');
        m.t("q3", "X", "q3", 'X', 'R');
        m.t("q3", "a", "q5", 'X', 'L');
        m.t("q4", "X", "q4", 'X', 'R');
        m.t("q4", "b", "q5", 'X', 'L');
        m.t("q5", "X", "q5", 'X', 'L');
        m.t("q5", "#", "q7", '#', 'L');
        m.t("q7", "ab", "q7", SAME, 'L');
        m.t("q7", "X", "q0", 'X', 'R');
        m.t("q6", "X", "q6", 'X', 'R');
        m.t("q6", ">", "qA", '>', 'L');
        m.place("q0", 0, 0.5); m.place("q1", 0.32, 0); m.place("q3", 0.64, 0); m.place("q2", 0.32, 1);
        m.place("q4", 0.64, 1); m.place("q5", 0.95, 0.5); m.place("q7", 0.55, 0.5);
        m.place("q6", 0, 0); m.place("qA", 0.15, 0);
        m.addExample(new Example("ab#ab", true, ""));
        m.addExample(new Example("#", true, "w = ε"));
        m.addExample(new Example("abb#abb", true, ""));
        m.addExample(new Example("ba#ab", false, ""));
        m.addExample(new Example("ab#abb", false, ""));
        m.addExample(new Example("aab", false, "sin #"));
        return m;
    }

    // =====================================================================
    // NIVEL 0 – Máquinas de Turing
    // =====================================================================
    public static List<MachineOption> level0() {
        return List.of(
                MachineOption.of("Aceptación: palíndromos sobre {a,b}", Catalog::palindromes),
                MachineOption.of("Función: suma unaria  1ⁿ+1ᵐ → 1ⁿ⁺ᵐ", Catalog::unaryAddition),
                MachineOption.of("Función: incremento binario  n → n+1", Catalog::binaryIncrement),
                MachineOption.custom("✎ MT personalizada (parametrizable)", TM_TEMPLATE, Parser::parseTM));
    }

    static TuringMachine palindromes() {
        TuringMachine m = new TuringMachine("Palíndromos sobre {a,b}",
                "L = { w ∈ {a,b}* | w = wᴿ }. La MT borra el primer símbolo, recuerda cuál era (q1 = a, q2 = b), "
                        + "viaja al extremo derecho, verifica y borra el último, y regresa. Acepta cuando la cinta queda en blanco.",
                false);
        m.setStart("q0");
        for (String s : new String[]{"q1", "q2", "q3", "q4", "q5"}) m.addState(s);
        m.addAccept("qA");
        m.setInputAlphabet("ab");
        m.t("q0", "a", "q1", BLANK, 'R');
        m.t("q0", "b", "q2", BLANK, 'R');
        m.t("q0", "_", "qA", BLANK, 'R');
        m.t("q1", "ab", "q1", SAME, 'R');
        m.t("q1", "_", "q3", BLANK, 'L');
        m.t("q2", "ab", "q2", SAME, 'R');
        m.t("q2", "_", "q4", BLANK, 'L');
        m.t("q3", "a", "q5", BLANK, 'L');
        m.t("q3", "_", "qA", BLANK, 'R');
        m.t("q4", "b", "q5", BLANK, 'L');
        m.t("q4", "_", "qA", BLANK, 'R');
        m.t("q5", "ab", "q5", SAME, 'L');
        m.t("q5", "_", "q0", BLANK, 'R');
        m.place("q0", 0, 0.5); m.place("q1", 0.3, 0); m.place("q3", 0.65, 0); m.place("q2", 0.3, 1);
        m.place("q4", 0.65, 1); m.place("q5", 1, 0.5); m.place("qA", 0.5, 0.33);
        m.addExample(new Example("abba", true, ""));
        m.addExample(new Example("aba", true, ""));
        m.addExample(new Example("babab", true, ""));
        m.addExample(new Example("a", true, ""));
        m.addExample(new Example("", true, ""));
        m.addExample(new Example("ab", false, ""));
        m.addExample(new Example("abab", false, ""));
        m.addExample(new Example("aab", false, ""));
        return m;
    }

    static TuringMachine unaryAddition() {
        TuringMachine m = new TuringMachine("Suma en notación unaria",
                "f(1ⁿ+1ᵐ) = 1ⁿ⁺ᵐ. La MT reemplaza el + por un 1, avanza hasta el final y borra el último 1. "
                        + "Ejemplo: 11+111 (2+3) produce 11111 (5).",
                false);
        m.setStart("q0");
        for (String s : new String[]{"q1", "q2", "q3"}) m.addState(s);
        m.addAccept("qF");
        m.setInputAlphabet("1+");
        m.t("q0", "1", "q0", '1', 'R');
        m.t("q0", "+", "q1", '1', 'R');
        m.t("q1", "1", "q1", '1', 'R');
        m.t("q1", "_", "q2", BLANK, 'L');
        m.t("q2", "1", "q3", BLANK, 'L');
        m.t("q3", "1", "q3", '1', 'L');
        m.t("q3", "_", "qF", BLANK, 'R');
        m.asFunction(out -> "= " + out.length() + " en unario");
        m.addExample(new Example("11+111", true, "2 + 3", "11111"));
        m.addExample(new Example("1+1", true, "1 + 1", "11"));
        m.addExample(new Example("1111+11", true, "4 + 2", "111111"));
        m.addExample(new Example("111+", true, "3 + 0", "111"));
        m.addExample(new Example("+11", true, "0 + 2", "11"));
        m.addExample(new Example("1+1+1", false, "formato inválido"));
        return m;
    }

    static TuringMachine binaryIncrement() {
        TuringMachine m = new TuringMachine("Incremento binario",
                "f(n) = n + 1 en binario. La MT va al extremo derecho y propaga el acarreo hacia la izquierda "
                        + "(1→0); si se acaban los dígitos, la cinta se extiende y escribe un 1 nuevo.",
                false);
        m.setStart("q0");
        for (String s : new String[]{"q1", "q2"}) m.addState(s);
        m.addAccept("qF");
        m.setInputAlphabet("01");
        m.t("q0", "01", "q0", SAME, 'R');
        m.t("q0", "_", "q1", BLANK, 'L');
        m.t("q1", "1", "q1", '0', 'L');
        m.t("q1", "0", "q2", '1', 'L');
        m.t("q1", "_", "q2", '1', 'L');
        m.t("q2", "01", "q2", SAME, 'L');
        m.t("q2", "_", "qF", BLANK, 'R');
        m.asFunction(out -> {
            try {
                return "= " + Long.parseLong(out, 2) + " en decimal";
            } catch (NumberFormatException e) {
                return "";
            }
        });
        m.addExample(new Example("1011", true, "11 → 12", "1100"));
        m.addExample(new Example("111", true, "7 → 8, la cinta crece", "1000"));
        m.addExample(new Example("0", true, "0 → 1", "1"));
        m.addExample(new Example("1001", true, "9 → 10", "1010"));
        m.addExample(new Example("10011", true, "19 → 20", "10100"));
        return m;
    }

    public static final String TM_TEMPLATE = String.join("\n",
            "# Máquina de Turing personalizada. Edite y presione Aceptar.",
            "# Formato:  estado,lee -> estado,escribe,movimiento   (L = izq., R = der., S = quieto)",
            "# El símbolo blanco se escribe como _",
            "nombre: Complemento binario",
            "descripcion: Invierte cada bit (0↔1) y se detiene al encontrar el blanco.",
            "inicial: q0",
            "finales: qF",
            "alfabeto: 0,1",
            "funcion: si",
            "q0,0 -> q0,1,R",
            "q0,1 -> q0,0,R",
            "q0,_ -> qF,_,L",
            "prueba: 10110",
            "prueba: 000",
            "prueba: 1");
}
