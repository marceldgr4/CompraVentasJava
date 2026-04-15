package com.app.Componets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernPasswordField extends JPasswordField {
    private String placeholder;

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setPreferredSize(new Dimension(300, 45));
        setMaximumSize(new Dimension(300, 45));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.setColor(new Color(210, 210, 210));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        super.paintComponent(g);
        if (getPassword().length == 0) {
            g2.setColor(new Color(160, 160, 160));
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            int padding = getInsets().left;
            int cHeight = g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, padding, (getHeight() + cHeight) / 2 - 2);
        }
        g2.dispose();
    }
}