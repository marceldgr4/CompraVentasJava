package com.app.UI.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class ModernFieldBase extends JTextField {

    protected String placeholder;
    protected static final int FIELD_WIDTH = 300;
    protected static final int FIELD_HEIGHT = 45;
    protected static final int BORDER_RADIUS = 15;
    protected static final int PADDING = 10;
    protected static final int PADDING_LR = 15;

    public ModernFieldBase(String placeholder) {
        this.placeholder = placeholder;
        configureStyle();
    }

    protected void configureStyle() {
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(PADDING, PADDING_LR, PADDING, PADDING_LR));
        setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        setForeground(new Color(50, 50, 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);

        // Borde
        g2.setColor(new Color(210, 210, 210));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);

        super.paintComponent(g);
        paintPlaceholder(g2);
        g2.dispose();
    }

    /**
     * Renderiza el placeholder si el campo está vacío.
     * Subclases pueden override para comportamiento custom.
     */
    protected void paintPlaceholder(Graphics2D g2) {
        if (getTextContent().isEmpty()) {
            g2.setColor(new Color(160, 160, 160));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(placeholder, PADDING_LR, y);
        }
    }

    /**
     * Obtiene el contenido del campo.
     * Override en subclases (ej: PasswordField usa getPassword()).
     */
    protected String getTextContent() {
        return getText();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    protected char[] getPassword() {
        
    }
}
