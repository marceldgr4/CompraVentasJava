package com.app.UI.Components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Campo de contraseña con estilo moderno.
 * Hereda de JPasswordField para funcionalidad de seguridad y
 * replica el estilo visual de ModernFieldBase.
 */
public class ModernPasswordField extends JPasswordField {

    private final String placeholder;
    private static final int FIELD_WIDTH = 300;
    private static final int FIELD_HEIGHT = 45;
    private static final int BORDER_RADIUS = 15;
    private static final int PADDING = 10;
    private static final int PADDING_LR = 15;

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        configureStyle();
    }

    private void configureStyle() {
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(PADDING, PADDING_LR, PADDING, PADDING_LR));
        setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        setForeground(new Color(50, 50, 50));
        setEchoChar('•'); // Carácter de máscara estándar
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

        // Placeholder
        if (getPassword().length == 0) {
            g2.setColor(new Color(160, 160, 160));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(placeholder, PADDING_LR, y);
        }
        g2.dispose();
    }
}
