package chomsky.ui;

import chomsky.model.Automaton;
import chomsky.model.Example;
import chomsky.model.MachineOption;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estructura común de los cuatro módulos: selector de máquina, entrada,
 * cadenas de prueba, controles de ejecución (cargar / paso / ejecutar /
 * reiniciar), diagrama de estados, tabla δ, traza y barra de estado.
 * Cada nivel aporta su propia visualización (cinta, pila, …).
 */
public abstract class BaseModulePanel extends JPanel {
    protected final Color accent;
    private final String title;
    private final String subtitle;
    private final List<MachineOption> options;
    private final Runnable onBack;
    private final Map<MachineOption, String> customTexts = new HashMap<>();

    protected Automaton automaton;
    protected final StateDiagramPanel diagram;

    private final DefaultListModel<String> tableModel = new DefaultListModel<>();
    private final JList<String> table = new JList<>(tableModel);
    private final JTextArea logArea = new JTextArea();
    private final JTextArea descArea = new JTextArea();
    private final JComboBox<MachineOption> machineCombo;
    private final JComboBox<Example> exampleCombo = new JComboBox<>();
    private final JTextField inputField = new JTextField(14);
    private final JSlider speed = new JSlider(1, 10, 6);
    private final Timer timer;
    private FlatButton editBtn, stepBtn, runBtn;
    private final JLabel stateValue = new JLabel("-");
    private final JLabel stepsValue = new JLabel("0");
    private final JLabel resultPill = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel reasonLabel = new JLabel(" ");
    private boolean updatingExamples;

    protected BaseModulePanel(Color accent, String title, String subtitle,
                              List<MachineOption> options, Runnable onBack) {
        this.accent = accent;
        this.title = title;
        this.subtitle = subtitle;
        this.options = options;
        this.onBack = onBack;
        this.diagram = new StateDiagramPanel(accent);
        this.machineCombo = new JComboBox<>(options.toArray(new MachineOption[0]));
        this.timer = new Timer(400, e -> doStep());
    }

    // ------------------------------------------------------------------
    // Ganchos para cada nivel
    // ------------------------------------------------------------------
    /** Visualización específica del nivel (cinta, cinta acotada, …). */
    protected abstract JComponent createVisualization();

    /** Componente opcional a la derecha del diagrama (p. ej. la pila). */
    protected JComponent createSidePanel() { return null; }

    /** Se invoca al cambiar de máquina. */
    protected void onMachineChanged() {}

    /** Se invoca tras cargar una cadena (configuración inicial). */
    protected void onLoaded() {}

    /** Se invoca después de cada paso. */
    protected void afterStep() {}

    /** Actualiza la visualización específica. */
    protected abstract void refreshVisualization(boolean animate);

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------
    protected final void build() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(buildHeader(), BorderLayout.NORTH);
        north.add(buildControls(), BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BODY_BOLD);
        tabs.addTab("Diagrama de estados", diagram);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        table.setFixedCellHeight(24);
        table.setSelectionBackground(Theme.mix(accent, Color.WHITE, 0.75));
        table.setSelectionForeground(Theme.TEXT);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new EmptyBorder(6, 6, 6, 6));
        tabs.addTab("Tabla de transiciones δ", tableScroll);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        top.add(card(tabs, null), BorderLayout.CENTER);
        JComponent side = createSidePanel();
        if (side != null) top.add(card(side, null), BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setFont(Theme.MONO);
        logArea.setLineWrap(false);
        logArea.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logScroll.setPreferredSize(new Dimension(380, 180));

        JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                card(createVisualization(), null), card(logScroll, "Traza de ejecución"));
        bottom.setResizeWeight(0.62);
        bottom.setBorder(null);
        bottom.setOpaque(false);
        bottom.setDividerSize(8);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        main.setResizeWeight(0.62);
        main.setBorder(new EmptyBorder(0, 12, 0, 12));
        main.setOpaque(false);
        main.setDividerSize(8);
        add(main, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        machineCombo.addActionListener(e -> selectMachine());
        exampleCombo.addActionListener(e -> {
            if (updatingExamples) return;
            Example ex = (Example) exampleCombo.getSelectedItem();
            if (ex != null) {
                inputField.setText(ex.input);
                load();
            }
        });
        selectMachine();
    }

    private JComponent buildHeader() {
        JPanel h = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setPaint(new GradientPaint(0, 0, accent, getWidth(), 0, Theme.mix(accent, Color.BLACK, 0.3)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        h.setBorder(new EmptyBorder(10, 14, 10, 18));
        FlatButton back = new FlatButton("←  Menú principal", Theme.alpha(Color.WHITE, 60));
        back.addActionListener(e -> {
            stopRun();
            onBack.run();
        });
        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.add(back);
        h.add(left, BorderLayout.WEST);
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font(Theme.SANS, Font.BOLD, 20));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.BODY);
        s.setForeground(new Color(255, 255, 255, 215));
        titles.add(t);
        titles.add(s);
        h.add(titles, BorderLayout.CENTER);
        return h;
    }

    private JComponent buildControls() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(8, 12, 8, 12)));

        // fila 1: selección de máquina
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.setOpaque(false);
        row1.add(label("Lenguaje / máquina:"));
        machineCombo.setFont(Theme.BODY);
        machineCombo.setPreferredSize(new Dimension(360, 30));
        row1.add(machineCombo);
        editBtn = new FlatButton("✎  Editar definición…", accent, true);
        editBtn.addActionListener(e -> editDefinition());
        row1.add(editBtn);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        box.add(row1);

        // descripción
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(Theme.BODY);
        descArea.setForeground(Theme.TEXT);
        descArea.setBackground(Theme.mix(accent, Color.WHITE, 0.93));
        descArea.setBorder(new EmptyBorder(6, 10, 6, 10));
        descArea.setRows(3);
        descArea.setAlignmentX(LEFT_ALIGNMENT);
        JPanel descWrap = new JPanel(new BorderLayout());
        descWrap.setOpaque(false);
        descWrap.setBorder(new EmptyBorder(4, 8, 6, 8));
        descWrap.add(descArea);
        descWrap.setAlignmentX(LEFT_ALIGNMENT);
        box.add(descWrap);

        // fila 2: entrada y controles
        JPanel row2 = new JPanel(new GridBagLayout());
        row2.setOpaque(false);
        row2.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 8, 2, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        row2.add(label("Cadena w:"), c);
        inputField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        inputField.setPreferredSize(new Dimension(150, 30));
        inputField.setMinimumSize(new Dimension(110, 30));
        inputField.addActionListener(e -> load());
        inputField.setToolTipText("Escriba la cadena y presione Enter o Cargar");
        c.weightx = 0.35;
        row2.add(inputField, c);
        c.weightx = 0;
        c.insets = new Insets(2, 14, 2, 0);
        row2.add(label("Cadenas de prueba:"), c);
        exampleCombo.setFont(Theme.MONO);
        exampleCombo.setPreferredSize(new Dimension(220, 30));
        exampleCombo.setMinimumSize(new Dimension(150, 30));
        exampleCombo.setMaximumRowCount(12);
        c.insets = new Insets(2, 8, 2, 0);
        c.weightx = 0.65;
        row2.add(exampleCombo, c);
        c.weightx = 0;
        c.insets = new Insets(2, 14, 2, 0);
        FlatButton loadBtn = new FlatButton("Cargar", Theme.STROKE);
        loadBtn.setToolTipText("Prepara la configuración inicial con la cadena escrita");
        loadBtn.addActionListener(e -> load());
        row2.add(loadBtn, c);
        c.insets = new Insets(2, 6, 2, 0);
        stepBtn = new FlatButton("Paso  ▸", accent);
        stepBtn.setToolTipText("Ejecuta una sola transición");
        stepBtn.addActionListener(e -> {
            stopRun();
            doStep();
        });
        row2.add(stepBtn, c);
        runBtn = new FlatButton("Ejecutar  ▸▸", accent);
        runBtn.setToolTipText("Ejecuta automáticamente hasta que la máquina se detenga");
        runBtn.addActionListener(e -> toggleRun());
        runBtn.setPreferredSize(new Dimension(124, runBtn.getPreferredSize().height));
        row2.add(runBtn, c);
        FlatButton resetBtn = new FlatButton("Reiniciar", accent, true);
        resetBtn.setToolTipText("Vuelve a la configuración inicial");
        resetBtn.addActionListener(e -> load());
        row2.add(resetBtn, c);
        c.insets = new Insets(2, 14, 2, 0);
        row2.add(label("Velocidad"), c);
        speed.setOpaque(false);
        speed.setPreferredSize(new Dimension(110, 28));
        speed.setMinimumSize(new Dimension(80, 28));
        speed.addChangeListener(e -> timer.setDelay(delay()));
        c.insets = new Insets(2, 4, 2, 0);
        row2.add(speed, c);
        box.add(row2);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 12, 10, 12));
        wrap.add(box);
        return wrap;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER), new EmptyBorder(8, 16, 8, 16)));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(label("Estado actual:"));
        stateValue.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        stateValue.setForeground(accent);
        left.add(stateValue);
        left.add(Box.createHorizontalStrut(10));
        left.add(label("Pasos:"));
        stepsValue.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        left.add(stepsValue);
        left.add(Box.createHorizontalStrut(10));
        left.add(label("Resultado:"));
        resultPill.setOpaque(true);
        resultPill.setFont(Theme.BODY_BOLD);
        resultPill.setForeground(Color.WHITE);
        resultPill.setBorder(new EmptyBorder(3, 12, 3, 12));
        left.add(resultPill);
        bar.add(left, BorderLayout.WEST);
        reasonLabel.setFont(Theme.BODY);
        reasonLabel.setForeground(Theme.MUTED);
        bar.add(reasonLabel, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(10, 12, 12, 12));
        wrap.add(bar);
        return wrap;
    }

    protected static JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setFont(Theme.BODY_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }

    /** Envuelve un componente en una tarjeta blanca con borde y título opcional. */
    protected static JComponent card(JComponent content, String heading) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(Theme.BORDER));
        if (heading != null) {
            JLabel h = new JLabel(heading);
            h.setFont(Theme.BODY_BOLD);
            h.setForeground(Theme.TEXT);
            h.setBorder(new EmptyBorder(8, 10, 4, 10));
            p.add(h, BorderLayout.NORTH);
        }
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------
    // Lógica de control
    // ------------------------------------------------------------------
    private void selectMachine() {
        stopRun();
        MachineOption opt = (MachineOption) machineCombo.getSelectedItem();
        if (opt == null) return;
        editBtn.setVisible(opt.isCustom());
        try {
            setAutomaton(opt.create(customTexts.get(opt)));
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Definición inválida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editDefinition() {
        MachineOption opt = (MachineOption) machineCombo.getSelectedItem();
        if (opt == null || !opt.isCustom()) return;
        String current = customTexts.getOrDefault(opt, opt.template);
        JTextArea ta = new JTextArea(current, 20, 56);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        ta.setTabSize(4);
        while (true) {
            int r = JOptionPane.showConfirmDialog(this, new JScrollPane(ta), "Definir: " + opt.title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            try {
                Automaton a = opt.create(ta.getText());
                customTexts.put(opt, ta.getText());
                setAutomaton(a);
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Definición inválida", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setAutomaton(Automaton a) {
        automaton = a;
        diagram.setAutomaton(a);
        tableModel.clear();
        for (String row : a.getTransitionRows()) tableModel.addElement("  " + row);
        descArea.setText(a.getName() + ".  " + a.getDescription() + "\n" + a.formalDefinition());
        onMachineChanged();

        updatingExamples = true;
        exampleCombo.removeAllItems();
        for (Example ex : a.getExamples()) exampleCombo.addItem(ex);
        if (!a.getExamples().isEmpty()) {
            exampleCombo.setSelectedIndex(0);
            inputField.setText(a.getExamples().get(0).input);
        }
        updatingExamples = false;
        load();
    }

    protected final void load() {
        stopRun();
        String w = inputField.getText().trim();
        inputField.setText(w);
        automaton.reset(w);
        logArea.setText("");
        log("Máquina: " + automaton.getName());
        log("Entrada: w = " + (w.isEmpty() ? "ε" : w) + "    |w| = " + w.length());
        log("Configuración inicial: estado " + automaton.getStartState());
        onLoaded();
        if (automaton.isHalted()) logResult();
        refresh(false);
    }

    private void doStep() {
        if (automaton.isHalted()) {
            stopRun();
            return;
        }
        automaton.step();
        log(String.format("Paso %3d:  %s", automaton.getSteps(), automaton.getLastAction()));
        afterStep();
        if (automaton.isHalted()) {
            logResult();
            stopRun();
        }
        refresh(true);
    }

    private void logResult() {
        log("─".repeat(60));
        log((automaton.isAccepted() ? "✓ CADENA ACEPTADA.  " : "✗ CADENA RECHAZADA.  ") + automaton.getHaltReason());
        String out = automaton.getOutput();
        if (out != null && automaton.isAccepted()) {
            String note = automaton.getOutputNote();
            log("Salida en la cinta: " + (out.isEmpty() ? "ε" : out) + (note == null || note.isEmpty() ? "" : "  (" + note + ")"));
        }
    }

    private void toggleRun() {
        if (timer.isRunning()) {
            stopRun();
            return;
        }
        if (automaton.isHalted()) load();
        timer.setDelay(delay());
        timer.start();
        runBtn.setText("Pausar  ❚❚");
    }

    private void stopRun() {
        timer.stop();
        if (runBtn != null) runBtn.setText("Ejecutar  ▸▸");
    }

    private int delay() {
        return 1650 - speed.getValue() * 155;
    }

    private void log(String s) {
        logArea.append(s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void refresh(boolean animate) {
        stateValue.setText(automaton.getCurrentState());
        stepsValue.setText(String.valueOf(automaton.getSteps()));
        if (!automaton.isHalted()) {
            resultPill.setText("EN EJECUCIÓN");
            resultPill.setBackground(Theme.RUNNING);
            reasonLabel.setText("Use «Paso» o «Ejecutar» para procesar la cadena.");
        } else if (automaton.isAccepted()) {
            resultPill.setText("ACEPTADA ✓");
            resultPill.setBackground(Theme.ACCEPT);
            String out = automaton.getOutput();
            String extra = "";
            if (out != null) {
                String note = automaton.getOutputNote();
                extra = "   Salida: " + (out.isEmpty() ? "ε" : out) + (note == null || note.isEmpty() ? "" : " (" + note + ")");
            }
            reasonLabel.setText(automaton.getHaltReason() + extra);
        } else {
            resultPill.setText("RECHAZADA ✗");
            resultPill.setBackground(Theme.REJECT);
            reasonLabel.setText(automaton.getHaltReason());
        }
        reasonLabel.setToolTipText(reasonLabel.getText());
        stepBtn.setEnabled(!automaton.isHalted());
        int idx = automaton.getLastTransitionIndex();
        if (idx >= 0) {
            table.setSelectedIndex(idx);
            table.ensureIndexIsVisible(idx);
        } else {
            table.clearSelection();
        }
        diagram.repaint();
        refreshVisualization(animate);
    }
}
