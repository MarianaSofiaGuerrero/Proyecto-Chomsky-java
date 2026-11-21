package chomsky.ui;

import chomsky.model.PDA;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Visualización animada de la pila (LIFO) del autómata de pila.
 * Los símbolos apilados "caen" sobre el tope y los desapilados se
 * elevan y desvanecen.
 */
public class StackPanel extends JPanel {
    private static final int BW = 96, BH = 34;

    private final Color accent;
    private List<Character> prev = new ArrayList<>();
    private List<Character> cur = new ArrayList<>();
    private String operation = "";
    private double t = 1;
    private final Timer timer;

    public StackPanel(Color accent) {
        this.accent = accent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(230, 320));
        timer = new Timer(15, e -> {
            t = Math.min(1, t + 0.055);
            if (t >= 1) ((Timer) e.getSource()).stop();
            repaint();
        });
    }

    public void setStack(List<Character> stack, String operation, boolean animate) {
        prev = cur;
        cur = new ArrayList<>(stack);
        this.operation = operation == null ? "" : operation;
        if (animate) {
            t = 0;
            timer.restart();
        } else {
            prev = cur;
            t = 1;
            timer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = Theme.smooth(g);
        int w = getWidth(), h = getHeight();
        double cx = w / 2.0;

        g2.setFont(Theme.H2);
        g2.setColor(Theme.TEXT);
        Theme.centerText(g2, "Pila (LIFO)", cx, 20);
        g2.setFont(Theme.BODY);
        g2.setColor(accent);
        Theme.centerText(g2, operation, cx, 42);

        int baseY = h - 34;
        int topLimit = 62;
        // contenedor en forma de U
        g2.setColor(new Color(0x94A3B8));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D u = new Path2D.Double();
        u.moveTo(cx - BW / 2.0 - 8, topLimit);
        u.lineTo(cx - BW / 2.0 - 8, baseY + 4);
        u.lineTo(cx + BW / 2.0 + 8, baseY + 4);
        u.lineTo(cx + BW / 2.0 + 8, topLimit);
        g2.draw(u);
        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        Theme.centerText(g2, "fondo", cx, baseY + 18);

        int cap = Math.max(2, (baseY - topLimit - 6) / BH);
        int common = 0;
        while (common < prev.size() && common < cur.size() && prev.get(common).equals(cur.get(common))) common++;
        boolean overflow = cur.size() > cap;
        int start = overflow ? cur.size() - cap + 1 : 0;
        double ease = 1 - Math.pow(1 - t, 3);

        if (overflow) {
            drawBox(g2, cx, slotY(baseY, 0), "⋮  " + start + " más", 1f, new Color(0xF1F5F9), Theme.MUTED, false);
        }
        for (int i = start; i < cur.size(); i++) {
            int slot = i - start + (overflow ? 1 : 0);
            double y = slotY(baseY, slot);
            float alpha = 1f;
            if (i >= common && t < 1) {
                y -= (1 - ease) * 70;
                alpha = (float) ease;
            }
            boolean top = i == cur.size() - 1;
            Color fill = top ? Theme.mix(accent, Color.WHITE, 0.15) : Theme.mix(accent, Color.WHITE, 0.85);
            Color text = top ? Color.WHITE : Theme.TEXT;
            drawBox(g2, cx, y, PDA.sym(cur.get(i)), alpha, fill, text, top);
            if (top && t >= 1) {
                g2.setFont(Theme.SMALL);
                g2.setColor(accent);
                g2.drawString("← tope", (float) (cx + BW / 2.0 + 14), (float) (y + BH / 2.0 + 4));
            }
        }
        if (t < 1) {
            for (int i = Math.max(common, start); i < prev.size(); i++) {
                int slot = i - start + (overflow ? 1 : 0);
                double y = slotY(baseY, slot) - ease * 60;
                drawBox(g2, cx, y, PDA.sym(prev.get(i)), (float) (1 - ease),
                        new Color(0xFEE2E2), Theme.REJECT, false);
            }
        }
        if (cur.isEmpty()) {
            g2.setFont(Theme.BODY);
            g2.setColor(Theme.MUTED);
            Theme.centerText(g2, "(pila vacía)", cx, baseY - 20);
        }
        g2.dispose();
    }

    private static double slotY(int baseY, int slot) {
        return baseY - (slot + 1) * (double) BH;
    }

    private void drawBox(Graphics2D g2, double cx, double y, String s, float alpha, Color fill, Color text, boolean top) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(1, alpha))));
        RoundRectangle2D r = new RoundRectangle2D.Double(cx - BW / 2.0, y + 2, BW, BH - 4, 8, 8);
        g2.setColor(fill);
        g2.fill(r);
        g2.setColor(top ? Theme.mix(accent, Color.BLACK, 0.2) : Theme.mix(accent, Color.WHITE, 0.4));
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(r);
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        g2.setColor(text);
        Theme.centerText(g2, s, cx, y + BH / 2.0);
        g2.setComposite(old);
    }
}
