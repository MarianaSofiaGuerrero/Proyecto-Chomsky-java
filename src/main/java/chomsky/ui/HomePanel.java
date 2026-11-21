package chomsky.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.IntConsumer;

/** Panel principal: diagrama de la jerarquía y selección de módulos. */
public class HomePanel extends JPanel {

    public HomePanel(IntConsumer openLevel) {
        setLayout(new BorderLayout(26, 20));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26, 30, 22, 30));

        JPanel head = new JPanel(new GridLayout(2, 1, 0, 4));
        head.setOpaque(false);
        JLabel t = new JLabel("Simulador de la Jerarquía de Chomsky");
        t.setFont(Theme.TITLE);
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel("Visualice, simule y valide los modelos computacionales de los cuatro niveles. "
                + "Seleccione un módulo para comenzar.");
        s.setFont(new Font(Theme.SANS, Font.PLAIN, 15));
        s.setForeground(Theme.MUTED);
        head.add(t);
        head.add(s);
        add(head, BorderLayout.NORTH);

        add(new HierarchyPanel(openLevel), BorderLayout.WEST);

        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.add(new LevelCard(3, "Lenguajes regulares", "Autómata Finito Determinista",
                "Reconoce cadenas de un lenguaje regular predeterminado o definido por usted. "
                        + "Muestra los estados y el estado actual al leer cada símbolo.",
                "par de ceros  ·  (a|b)*ab  ·  múltiplos de 3", openLevel));
        grid.add(new LevelCard(2, "Lenguajes libres de contexto", "Autómata de Pila",
                "Reconoce lenguajes que necesitan memoria LIFO. Animación de la pila con cada "
                        + "operación de apilar y desapilar.",
                "aⁿbⁿ  ·  ([ ])  ·  w c wᴿ", openLevel));
        grid.add(new LevelCard(1, "Lenguajes sensibles al contexto", "Autómata Linealmente Acotado",
                "Cinta acotada por los delimitadores &lt; y &gt;, con movimiento bidireccional "
                        + "del cabezal y memoria limitada a la longitud de la entrada.",
                "aⁿbⁿcⁿ  ·  w#w", openLevel));
        grid.add(new LevelCard(0, "Lenguajes recursivamente enumerables", "Máquina de Turing",
                "Cinta extensible, alfabeto de cinta, cabezal de lectura/escritura y estado activo. "
                        + "Acepta lenguajes o calcula funciones aritméticas.",
                "palíndromos  ·  suma unaria  ·  n + 1", openLevel));
        add(grid, BorderLayout.CENTER);

        JLabel foot = new JLabel("Teoría de la Computación  ·  Tipo 3 ⊂ Tipo 2 ⊂ Tipo 1 ⊂ Tipo 0  ·  "
                + "Haga clic en una tarjeta o en una región del diagrama");
        foot.setFont(Theme.SMALL);
        foot.setForeground(Theme.MUTED);
        add(foot, BorderLayout.SOUTH);
    }

    /** Tarjeta clicable de un nivel. */
    private static final class LevelCard extends JPanel {
        private final Color color;
        private boolean hover;

        LevelCard(int level, String language, String machine, String desc, String examples, IntConsumer open) {
            this.color = Theme.level(level);
            setOpaque(false);
            setLayout(new BorderLayout(0, 6));
            setBorder(new EmptyBorder(18, 26, 16, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel top = new JPanel(new GridLayout(3, 1, 0, 2));
            top.setOpaque(false);
            JLabel badge = new JLabel("NIVEL " + level + "  ·  GRAMÁTICAS TIPO " + level);
            badge.setFont(new Font(Theme.SANS, Font.BOLD, 12));
            badge.setForeground(color);
            JLabel lang = new JLabel(language);
            lang.setFont(Theme.H2);
            lang.setForeground(Theme.TEXT);
            JLabel mach = new JLabel(machine);
            mach.setFont(new Font(Theme.SANS, Font.BOLD, 14));
            mach.setForeground(Theme.mix(color, Color.BLACK, 0.15));
            top.add(badge);
            top.add(lang);
            top.add(mach);
            add(top, BorderLayout.NORTH);

            JLabel d = new JLabel("<html><div style='line-height:1.3'>" + desc + "</div></html>");
            d.setFont(Theme.BODY);
            d.setForeground(Theme.MUTED);
            d.setVerticalAlignment(SwingConstants.TOP);
            add(d, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            JLabel ex = new JLabel(examples);
            ex.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            ex.setForeground(Theme.MUTED);
            JLabel go = new JLabel("Abrir módulo  →");
            go.setFont(Theme.BODY_BOLD);
            go.setForeground(color);
            bottom.add(ex, BorderLayout.WEST);
            bottom.add(go, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { open.accept(level); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(15, 23, 42, hover ? 28 : 12));
            g2.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 3, 18, 18));
            g2.setColor(hover ? Theme.mix(color, Color.WHITE, 0.95) : Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 4, 18, 18));
            g2.setColor(hover ? color : Theme.BORDER);
            g2.setStroke(new BasicStroke(hover ? 2f : 1f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 5, 18, 18));
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Double(0, 0, 8, h - 4, 8, 8));
            g2.fillRect(4, 0, 4, h - 4);
            g2.dispose();
        }
    }

    /** Diagrama de conjuntos anidados de la jerarquía; cada región abre su módulo. */
    private static final class HierarchyPanel extends JPanel {
        private final RoundRectangle2D[] regions = new RoundRectangle2D[4];
        private int hover = -1;
        private static final String[][] TEXT = {
                {"Tipo 0 · Recursivamente enumerables", "Máquina de Turing"},
                {"Tipo 1 · Sensibles al contexto", "Autómata linealmente acotado"},
                {"Tipo 2 · Libres de contexto", "Autómata de pila"},
                {"Tipo 3 · Regulares", "Autómata finito"},
        };

        HierarchyPanel(IntConsumer open) {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 460));
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int h = regionAt(e.getPoint());
                    if (h != hover) {
                        hover = h;
                        setCursor(Cursor.getPredefinedCursor(h >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) { hover = -1; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    int r = regionAt(e.getPoint());
                    if (r >= 0) open.accept(r);
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int regionAt(Point p) {
            for (int i = 3; i >= 0; i--) if (regions[i] != null && regions[i].contains(p)) return i;
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            double x = 4, y = 4, w = getWidth() - 8, h = getHeight() - 8;
            for (int i = 0; i < 4; i++) {
                Color c = Theme.level(i);
                regions[i] = new RoundRectangle2D.Double(x, y, w, h, 28, 28);
                g2.setColor(Theme.mix(c, Color.WHITE, hover == i ? 0.78 : 0.9));
                g2.fill(regions[i]);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(hover == i ? 2.6f : 1.6f));
                g2.draw(regions[i]);
                g2.setFont(new Font(Theme.SANS, Font.BOLD, 13));
                g2.setColor(Theme.mix(c, Color.BLACK, 0.25));
                g2.drawString(TEXT[i][0], (float) (x + 14), (float) (y + 24));
                g2.setFont(Theme.SMALL);
                g2.setColor(Theme.MUTED);
                g2.drawString(TEXT[i][1], (float) (x + 14), (float) (y + 40));
                x += 22;
                y += 58;
                w -= 44;
                h -= 74;
            }
            g2.setFont(new Font(Theme.SANS, Font.BOLD, 30));
            g2.setColor(Theme.alpha(Theme.L3, 90));
            Theme.centerText(g2, "Σ*", x - 22 + (w + 44) / 2, y - 58 + (h + 74) / 2 + 18);
            g2.dispose();
        }
    }
}
