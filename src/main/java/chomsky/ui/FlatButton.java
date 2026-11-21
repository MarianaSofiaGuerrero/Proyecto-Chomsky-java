package chomsky.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Botón plano con esquinas redondeadas. */
public class FlatButton extends JButton {
    private Color base;
    private final boolean outline;

    public FlatButton(String text, Color base) {
        this(text, base, false);
    }

    public FlatButton(String text, Color base, boolean outline) {
        super(text);
        this.base = base;
        this.outline = outline;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Theme.BODY_BOLD);
        setForeground(outline ? base : Color.WHITE);
        setBorder(new EmptyBorder(7, 14, 7, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setBase(Color c) {
        base = c;
        if (outline) setForeground(c);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.smooth(g);
        ButtonModel m = getModel();
        Color c = base;
        if (!isEnabled()) c = new Color(0xCBD5E1);
        else if (m.isPressed()) c = Theme.mix(base, Color.BLACK, 0.25);
        else if (m.isRollover()) c = Theme.mix(base, Color.BLACK, 0.12);
        int w = getWidth(), h = getHeight();
        if (outline) {
            g2.setColor(m.isRollover() && isEnabled() ? Theme.alpha(base, 30) : Theme.alpha(Color.WHITE, 230));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            g2.setColor(isEnabled() ? c : new Color(0xCBD5E1));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);
        } else {
            g2.setColor(c);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
