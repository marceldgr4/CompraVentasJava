package com.app.Componets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernTextField extends JTextField {
    private String placeholder;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setPreferredSize(new Dimension(300, 45));
        setMaximumSize(new Dimension(300, 45));
        setForeground(new Color(50, 50, 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        // Borde
        g2.setColor(new Color(210, 210, 210));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

        super.paintComponent(g);

        if (getText().isEmpty()){
            g2.setColor(new Color(160, 160, 160));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(placeholder, 15, y);
        }
        g2.dispose();
    }
}