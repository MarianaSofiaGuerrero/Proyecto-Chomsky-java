package chomsky.ui;

import chomsky.model.Automaton;
import chomsky.model.TuringMachine;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Representación gráfica de una cinta con su cabezal.
 * <ul>
 *   <li>INPUT: cinta de entrada de solo lectura (AFD y AP); el cabezal solo avanza.</li>
 *   <li>BOUNDED: cinta acotada por los delimitadores &lt; y &gt; (ALA).</li>
 *   <li>INFINITE: cinta extensible en ambos sentidos con blancos (MT).</li>
 * </ul>
 * El cabezal se desliza suavemente entre celdas.
 */
public class TapePanel extends JPanel {
    public enum Mode { INPUT, BOUNDED, INFINITE }

    private static final int CW = 46, CH = 50, TAPE_Y = 74;

    private final Mode mode;
    private final Color accent;
    private List<Character> cells = new ArrayList<>();
    private int head, origin;
    private String state = "";
    private String caption = "";
    private String footer = "";
    private boolean halted, accepted;
    private double animHead;
    private final Timer anim;

    public TapePanel(Mode mode, Color accent) {
        this.mode = mode;
        this.accent = accent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(620, 175));
        anim = new Timer(15, e -> {
            double d = head - animHead;
            if (Math.abs(d) < 0.01) {
                animHead = head;
                ((Timer) e.getSource()).stop();
            } else {
                animHead += d * 0.2;
            }
            repaint();
        });
    }

    public void setCaption(String caption) {
        this.caption = caption;
        repaint();
    }

    public void setContent(List<Character> cells, int head, int origin, String state,
                           boolean halted, boolean accepted, String footer, boolean animate) {
        this.cells = new ArrayList<>(cells);
        this.head = head;
        this.origin = origin;
        this.state = state == null ? "" : state;
        this.halted = halted;
        this.accepted = accepted;
        this.footer = footer == null ? "" : footer;
        if (animate) {
            anim.start();
        } else {
            anim.stop();
            animHead = head;
        }
        repaint();
    }

    public static List<Character> chars(String s) {
        List<Character> l = new ArrayList<>();
        for (char c : s.toCharArray()) l.add(c);
        return l;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth();

        g2.setFont(Theme.BODY_BOLD);
        g2.setColor(Theme.TEXT);
        g2.drawString(caption, 14, 22);

        int n = cells.size();
        int slots = mode == Mode.INPUT ? n + 1 : n;
        double total = slots * (double) CW;
        boolean fit = mode != Mode.INFINITE && total <= w - 40;
        double baseX = fit ? (w - total) / 2.0 : w / 2.0 - CW / 2.0 - animHead * CW;

        int first, last;
        if (mode == Mode.INFINITE) {
            first = (int) Math.floor(-baseX / CW) - 1;
            last = (int) Math.ceil((w - baseX) / CW) + 1;
        } else {
            first = 0;
            last = slots - 1;
        }

        for (int i = first; i <= last; i++) {
            double x = baseX + i * (double) CW;
            if (x + CW < 0 || x > w) continue;
            if (mode == Mode.INPUT && i == n) {
                drawEndSlot(g2, x);
                continue;
            }
            boolean real = i >= 0 && i < n;
            char c = real ? cells.get(i) : TuringMachine.BLANK;
            drawCell(g2, x, i, c, real);
        }

        // indicadores de cinta infinita
        if (mode == Mode.INFINITE) {
            GradientPaint gl = new GradientPaint(0, 0, Color.WHITE, 60, 0, new Color(255, 255, 255, 0));
            g2.setPaint(gl);
            g2.fillRect(0, TAPE_Y - 2, 60, CH + 4);
            GradientPaint gr = new GradientPaint(w - 60, 0, new Color(255, 255, 255, 0), w, 0, Color.WHITE);
            g2.setPaint(gr);
            g2.fillRect(w - 60, TAPE_Y - 2, 60, CH + 4);
            g2.setColor(Theme.MUTED);
            g2.setFont(new Font(Theme.SANS, Font.BOLD, 18));
            Theme.centerText(g2, "⋯", 16, TAPE_Y + CH / 2.0);
            Theme.centerText(g2, "⋯", w - 16, TAPE_Y + CH / 2.0);
        }

        drawHead(g2, baseX + animHead * CW + CW / 2.0);

        g2.setFont(Theme.BODY);
        g2.setColor(Theme.MUTED);
        g2.drawString(footer, 14, TAPE_Y + CH + 46);
        g2.dispose();
    }

    private void drawCell(Graphics2D g2, double x, int i, char c, boolean real) {
        Color fill = Color.WHITE, border = new Color(0x94A3B8), text = Theme.TEXT;
        boolean marker = mode == Mode.BOUNDED && (c == TuringMachine.LEFT || c == TuringMachine.RIGHT);
        if (mode == Mode.INPUT) {
            if (i < head) {
                fill = new Color(0xEEF2F7);
                text = new Color(0xA0AEC0);
            }
        }
        if (marker) {
            fill = Theme.STROKE;
            text = Color.WHITE;
            border = Theme.STROKE;
        }
        if (!real) {
            fill = new Color(0xFAFBFC);
            border = new Color(0xE2E8F0);
        }
        if (i == head) {
            fill = Theme.mix(accent, Color.WHITE, 0.82);
            border = accent;
            if (marker) fill = Theme.mix(accent, Theme.STROKE, 0.4);
        }
        RoundRectangle2D r = new RoundRectangle2D.Double(x + 2, TAPE_Y, CW - 4, CH, 6, 6);
        g2.setColor(fill);
        g2.fill(r);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(i == head ? 2.4f : 1.2f));
        g2.draw(r);

        String s = Automaton.show(c);
        if (c == TuringMachine.BLANK) text = real ? new Color(0x94A3B8) : new Color(0xCBD5E1);
        g2.setFont(Theme.CELL);
        g2.setColor(text);
        Theme.centerText(g2, s, x + CW / 2.0, TAPE_Y + CH / 2.0);

        // índice de la celda
        String idx;
        if (mode == Mode.INPUT) idx = String.valueOf(i + 1);
        else if (marker) idx = c == TuringMachine.LEFT ? "inicio" : "fin";
        else idx = String.valueOf(i - origin);
        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        Theme.centerText(g2, idx, x + CW / 2.0, TAPE_Y + CH + 14);
    }

    private void drawEndSlot(Graphics2D g2, double x) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x + 2, TAPE_Y, CW - 4, CH, 6, 6);
        g2.setColor(head == cells.size() ? Theme.mix(accent, Color.WHITE, 0.88) : Color.WHITE);
        g2.fill(r);
        g2.setColor(head == cells.size() ? accent : new Color(0xCBD5E1));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4, 4}, 0));
        g2.draw(r);
        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        Theme.centerText(g2, "fin", x + CW / 2.0, TAPE_Y + CH / 2.0);
    }

    private void drawHead(Graphics2D g2, double hx) {
        Color c = halted ? (accepted ? Theme.ACCEPT : Theme.REJECT) : accent;
        g2.setFont(Theme.BODY_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int bw = Math.max(44, fm.stringWidth(state) + 18);
        double bx = hx - bw / 2.0, by = 32;
        g2.setColor(c);
        g2.fill(new RoundRectangle2D.Double(bx, by, bw, 24, 10, 10));
        Path2D tri = new Path2D.Double();
        tri.moveTo(hx - 8, by + 23);
        tri.lineTo(hx + 8, by + 23);
        tri.lineTo(hx, TAPE_Y - 3);
        tri.closePath();
        g2.fill(tri);
        g2.setColor(Color.WHITE);
        Theme.centerText(g2, state, hx, by + 12);
    }
}
