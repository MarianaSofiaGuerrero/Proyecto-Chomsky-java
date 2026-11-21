package chomsky.ui;

import chomsky.model.Automaton;
import chomsky.model.Edge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Diagrama de estados interactivo, común a los cuatro niveles.
 * Resalta el estado actual y la última transición usada.
 * Los estados se pueden arrastrar con el ratón.
 */
public class StateDiagramPanel extends JPanel {
    private static final int R = 24;
    private static final int MAX_LABEL_LINES = 5;
    private static final Font NODE_FONT = new Font(Theme.SANS, Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font(Theme.SANS, Font.PLAIN, 12);

    private final Color accent;
    private Automaton automaton;
    /** Posiciones normalizadas (0..1) de cada estado. */
    private final Map<String, Point2D.Double> norm = new HashMap<>();
    private String dragging;
    private String hover;

    public StateDiagramPanel(Color accent) {
        this.accent = accent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(760, 340));
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragging = nodeAt(e.getPoint()); }
            @Override public void mouseReleased(MouseEvent e) { dragging = null; }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragging == null) return;
                norm.put(dragging, toNorm(e.getX(), e.getY()));
                repaint();
            }
            @Override public void mouseMoved(MouseEvent e) {
                String h = nodeAt(e.getPoint());
                if (!Objects.equals(h, hover)) {
                    hover = h;
                    setCursor(Cursor.getPredefinedCursor(h != null ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setAutomaton(Automaton a) {
        automaton = a;
        norm.clear();
        List<String> st = a.getStates();
        int n = st.size();
        if (a.getLayout().keySet().containsAll(st)) {
            for (String s : st) {
                double[] xy = a.getLayout().get(s);
                norm.put(s, new Point2D.Double(xy[0], xy[1]));
            }
        } else if (n == 1) {
            norm.put(st.get(0), new Point2D.Double(0.5, 0.5));
        } else if (n <= 3) {
            // cadena horizontal: los bucles quedan hacia afuera y las aristas largas se curvan solas
            for (int i = 0; i < n; i++) {
                double x = n == 2 ? 0.15 + 0.7 * i : i / 2.0;
                norm.put(st.get(i), new Point2D.Double(x, 0.55));
            }
        } else {
            for (int i = 0; i < n; i++) {
                double th = Math.PI + 2 * Math.PI * i / n;
                norm.put(st.get(i), new Point2D.Double(0.5 + 0.5 * Math.cos(th), 0.5 + 0.5 * Math.sin(th)));
            }
        }
        repaint();
    }

    // ---------------------------------------------------------------
    // Coordenadas
    // ---------------------------------------------------------------
    private double mx() { return Math.min(150, getWidth() * 0.15); }
    private double my() { return Math.min(105, getHeight() * 0.28); }

    private Point2D.Double screen(String s) {
        Point2D.Double p = norm.get(s);
        double w = Math.max(1, getWidth() - 2 * mx());
        double h = Math.max(1, getHeight() - 2 * my());
        return new Point2D.Double(mx() + p.x * w, my() + p.y * h);
    }

    private Point2D.Double toNorm(int x, int y) {
        double w = Math.max(1, getWidth() - 2 * mx());
        double h = Math.max(1, getHeight() - 2 * my());
        double nx = (x - mx()) / w, ny = (y - my()) / h;
        double ex = (mx() - R - 4) / w, ey = (my() - R - 4) / h;
        return new Point2D.Double(Math.max(-ex, Math.min(1 + ex, nx)), Math.max(-ey, Math.min(1 + ey, ny)));
    }

    private String nodeAt(Point p) {
        if (automaton == null) return null;
        for (String s : automaton.getStates()) {
            if (screen(s).distance(p) <= R + 3) return s;
        }
        return null;
    }

    /** Dirección "hacia afuera" del nodo respecto al centro del panel. */
    private Point2D.Double outward(Point2D.Double c) {
        double dx = c.x - getWidth() / 2.0, dy = c.y - getHeight() / 2.0;
        double len = Math.hypot(dx, dy);
        if (len < 1) return new Point2D.Double(0, -1);
        return new Point2D.Double(dx / len, dy / len);
    }

    // ---------------------------------------------------------------
    // Dibujo
    // ---------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (automaton == null) return;
        Graphics2D g2 = Theme.smooth(g);

        // fondo punteado
        g2.setColor(new Color(0xEEF1F6));
        for (int x = 10; x < getWidth(); x += 22)
            for (int y = 10; y < getHeight(); y += 22) g2.fillRect(x, y, 2, 2);

        Set<String> keys = new HashSet<>();
        for (Edge e : automaton.getEdges()) keys.add(e.from + "\u0000" + e.to);

        List<Runnable> labels = new ArrayList<>();
        Edge last = automaton.getLastEdge();
        for (Edge e : automaton.getEdges()) {
            boolean hl = e == last;
            if (e.isLoop()) drawLoop(g2, e, hl, labels);
            else drawEdge(g2, e, keys.contains(e.to + "\u0000" + e.from), hl, labels);
        }
        for (String s : automaton.getStates()) drawNode(g2, s);
        for (Runnable r : labels) r.run();
        drawLegend(g2);
        g2.dispose();
    }

    private Color edgeColor(boolean hl) {
        return hl ? accent : new Color(0x64748B);
    }

    private void drawEdge(Graphics2D g2, Edge e, boolean hasReverse, boolean hl, List<Runnable> labels) {
        Point2D.Double a = screen(e.from), b = screen(e.to);
        double dx = b.x - a.x, dy = b.y - a.y, len = Math.hypot(dx, dy);
        if (len < 1) return;
        double nx = -dy / len, ny = dx / len;
        double off = hasReverse ? 46 : 0;
        if (!hasReverse) {
            // si la línea recta atraviesa otro estado, se curva para rodearlo
            for (String o : automaton.getStates()) {
                if (o.equals(e.from) || o.equals(e.to)) continue;
                Point2D.Double p = screen(o);
                double tt = ((p.x - a.x) * dx + (p.y - a.y) * dy) / (len * len);
                if (tt <= 0.05 || tt >= 0.95) continue;
                double dist = (p.x - a.x) * nx + (p.y - a.y) * ny;
                if (Math.abs(dist) < R + 14) {
                    off = (dist >= 0 ? -1 : 1) * 2 * (R + 30 - Math.abs(dist));
                    break;
                }
            }
        }
        Point2D.Double c = new Point2D.Double((a.x + b.x) / 2 + nx * off, (a.y + b.y) / 2 + ny * off);
        Point2D.Double s = toward(a, c, R), t = toward(b, c, R + 1);
        g2.setColor(edgeColor(hl));
        g2.setStroke(new BasicStroke(hl ? 3f : 1.6f));
        g2.draw(new QuadCurve2D.Double(s.x, s.y, c.x, c.y, t.x, t.y));
        arrow(g2, c, t, hl);
        double lx = 0.25 * s.x + 0.5 * c.x + 0.25 * t.x;
        double ly = 0.25 * s.y + 0.5 * c.y + 0.25 * t.y;
        labels.add(() -> drawLabel(g2, e, lx, ly, null, hl));
    }

    private void drawLoop(Graphics2D g2, Edge e, boolean hl, List<Runnable> labels) {
        Point2D.Double c = screen(e.from);
        Point2D.Double d = outward(c);
        double phi = Math.atan2(d.x, -d.y);
        AffineTransform at = new AffineTransform();
        at.translate(c.x, c.y);
        at.rotate(phi);
        double a0 = 0.5;
        Point2D p1 = new Point2D.Double(-R * Math.sin(a0), -R * Math.cos(a0));
        Point2D p2 = new Point2D.Double(R * Math.sin(a0), -R * Math.cos(a0));
        Point2D c1 = new Point2D.Double(-36, -R - 50);
        Point2D c2 = new Point2D.Double(36, -R - 50);
        Shape loop = at.createTransformedShape(new CubicCurve2D.Double(p1.getX(), p1.getY(), c1.getX(), c1.getY(),
                c2.getX(), c2.getY(), p2.getX(), p2.getY()));
        g2.setColor(edgeColor(hl));
        g2.setStroke(new BasicStroke(hl ? 3f : 1.6f));
        g2.draw(loop);
        Point2D tc2 = at.transform(c2, null), tp2 = at.transform(p2, null);
        arrow(g2, new Point2D.Double(tc2.getX(), tc2.getY()), new Point2D.Double(tp2.getX(), tp2.getY()), hl);
        labels.add(() -> drawLabel(g2, e, c.x, c.y, d, hl));
    }

    private static Point2D.Double toward(Point2D.Double from, Point2D.Double to, double dist) {
        double dx = to.x - from.x, dy = to.y - from.y, l = Math.hypot(dx, dy);
        if (l < 1e-6) return new Point2D.Double(from.x, from.y);
        return new Point2D.Double(from.x + dx / l * dist, from.y + dy / l * dist);
    }

    private void arrow(Graphics2D g2, Point2D.Double from, Point2D.Double tip, boolean hl) {
        double ang = Math.atan2(tip.y - from.y, tip.x - from.x);
        double sz = hl ? 12 : 10;
        Path2D p = new Path2D.Double();
        p.moveTo(tip.x, tip.y);
        p.lineTo(tip.x - sz * Math.cos(ang - 0.4), tip.y - sz * Math.sin(ang - 0.4));
        p.lineTo(tip.x - sz * Math.cos(ang + 0.4), tip.y - sz * Math.sin(ang + 0.4));
        p.closePath();
        g2.fill(p);
    }

    /**
     * Dibuja la etiqueta de una arista. Si {@code loopDir} no es null, la
     * etiqueta se ubica afuera del bucle en esa dirección.
     */
    private void drawLabel(Graphics2D g2, Edge e, double x, double y, Point2D.Double loopDir, boolean hl) {
        List<String> lines = new ArrayList<>();
        if (e.inline) {
            lines.add(String.join(", ", e.getLabels()));
        } else {
            List<String> l = e.getLabels();
            for (int i = 0; i < l.size(); i++) {
                if (i == MAX_LABEL_LINES - 1 && l.size() > MAX_LABEL_LINES) {
                    lines.add("… +" + (l.size() - i) + " más (ver tabla)");
                    break;
                }
                lines.add(l.get(i));
            }
        }
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int lh = fm.getHeight() - 1;
        int w = 0;
        for (String s : lines) w = Math.max(w, fm.stringWidth(s));
        w += 10;
        int h = lh * lines.size() + 4;
        double cx = x, cy = y;
        if (loopDir != null) {
            double ext = Math.abs(loopDir.x) * w / 2.0 + Math.abs(loopDir.y) * h / 2.0;
            double dist = R + 42 + ext;
            cx = x + loopDir.x * dist;
            cy = y + loopDir.y * dist;
        }
        double bx = cx - w / 2.0, by = cy - h / 2.0;
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fill(new RoundRectangle2D.Double(bx, by, w, h, 8, 8));
        g2.setColor(hl ? accent : new Color(0xD5DBE5));
        g2.setStroke(new BasicStroke(hl ? 1.6f : 1f));
        g2.draw(new RoundRectangle2D.Double(bx, by, w, h, 8, 8));
        g2.setColor(hl ? Theme.mix(accent, Color.BLACK, 0.2) : Theme.TEXT);
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);
            g2.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (by + 2 + fm.getAscent() + i * lh));
        }
    }

    private void drawNode(Graphics2D g2, String s) {
        Point2D.Double c = screen(s);
        boolean current = s.equals(automaton.getCurrentState());
        boolean accept = automaton.getAcceptStates().contains(s);
        Color fill = Color.WHITE, text = Theme.TEXT;
        Color ring = accent;
        if (current) {
            if (automaton.isHalted()) ring = automaton.isAccepted() ? Theme.ACCEPT : Theme.REJECT;
            fill = ring;
            text = Color.WHITE;
            g2.setColor(Theme.alpha(ring, 45));
            g2.fill(new Ellipse2D.Double(c.x - R - 9, c.y - R - 9, 2 * R + 18, 2 * R + 18));
        }
        // flecha de inicio
        if (s.equals(automaton.getStartState())) {
            Point2D.Double d = outward(c);
            boolean hasLoop = false;
            for (Edge e : automaton.getEdges()) if (e.isLoop() && e.from.equals(s)) hasLoop = true;
            Point2D.Double dir = hasLoop ? new Point2D.Double(d.y, -d.x) : d;
            if (hasLoop && dir.y < 0) dir = new Point2D.Double(-dir.x, -dir.y);
            Point2D.Double tail = new Point2D.Double(c.x + dir.x * (R + 38), c.y + dir.y * (R + 38));
            Point2D.Double tip = new Point2D.Double(c.x + dir.x * (R + 2), c.y + dir.y * (R + 2));
            g2.setColor(Theme.STROKE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Line2D.Double(tail, tip));
            arrow(g2, tail, tip, false);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            Theme.centerText(g2, "inicio", tail.x + dir.x * 16, tail.y + dir.y * 12);
        }
        g2.setColor(fill);
        g2.fill(new Ellipse2D.Double(c.x - R, c.y - R, 2 * R, 2 * R));
        g2.setColor(current ? Theme.mix(ring, Color.BLACK, 0.25) : (s.equals(hover) ? accent : Theme.STROKE));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Ellipse2D.Double(c.x - R, c.y - R, 2 * R, 2 * R));
        if (accept) {
            g2.setColor(current ? Color.WHITE : Theme.STROKE);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new Ellipse2D.Double(c.x - R + 5, c.y - R + 5, 2 * R - 10, 2 * R - 10));
        }
        g2.setFont(NODE_FONT);
        g2.setColor(text);
        Theme.centerText(g2, s, c.x, c.y);
    }

    private void drawLegend(Graphics2D g2) {
        int y = getHeight() - 14;
        int x = 12;
        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        g2.drawString("Arrastra los estados para reorganizar el diagrama", x, y);
        int lx = getWidth() - 300;
        if (lx < 330) return;
        g2.setStroke(new BasicStroke(1.4f));
        // estado
        g2.setColor(Theme.STROKE);
        g2.draw(new Ellipse2D.Double(lx, y - 10, 12, 12));
        g2.setColor(Theme.MUTED);
        g2.drawString("estado", lx + 17, y);
        // aceptación
        lx += 70;
        g2.setColor(Theme.STROKE);
        g2.draw(new Ellipse2D.Double(lx, y - 10, 12, 12));
        g2.draw(new Ellipse2D.Double(lx + 3, y - 7, 6, 6));
        g2.setColor(Theme.MUTED);
        g2.drawString("aceptación", lx + 17, y);
        // actual
        lx += 95;
        g2.setColor(accent);
        g2.fill(new Ellipse2D.Double(lx, y - 10, 12, 12));
        g2.setColor(Theme.MUTED);
        g2.drawString("estado actual", lx + 17, y);
    }
}
