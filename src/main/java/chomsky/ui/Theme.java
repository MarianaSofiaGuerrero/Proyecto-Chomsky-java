package chomsky.ui;

import java.awt.*;

/** Colores y tipografías compartidas por toda la interfaz. */
public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(0xF1F4F9);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(0xDCE2EC);
    public static final Color TEXT = new Color(0x1E293B);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color STROKE = new Color(0x334155);

    public static final Color ACCEPT = new Color(0x16A34A);
    public static final Color REJECT = new Color(0xDC2626);
    public static final Color RUNNING = new Color(0x2563EB);

    /** Color de acento por nivel de la jerarquía (índice = número de nivel). */
    public static final Color L0 = new Color(0x7C3AED);
    public static final Color L1 = new Color(0xEA580C);
    public static final Color L2 = new Color(0x059669);
    public static final Color L3 = new Color(0x2563EB);

    public static Color level(int n) {
        switch (n) {
            case 0: return L0;
            case 1: return L1;
            case 2: return L2;
            default: return L3;
        }
    }

    public static final String SANS = "SansSerif";
    public static final Font TITLE = new Font(SANS, Font.BOLD, 26);
    public static final Font H2 = new Font(SANS, Font.BOLD, 18);
    public static final Font BODY = new Font(SANS, Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font(SANS, Font.BOLD, 13);
    public static final Font SMALL = new Font(SANS, Font.PLAIN, 11);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    public static final Font CELL = new Font(Font.MONOSPACED, Font.BOLD, 20);

    public static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    public static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    /** Dibuja un texto centrado en (cx, cy). */
    public static void centerText(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }
}
