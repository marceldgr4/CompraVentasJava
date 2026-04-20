package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

public class ButtonFactory {

    public static JButton createPrimaryButton(String text) {
        return createButton(text, UIConstants.PRIMARY_COLOR);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, UIConstants.SUCCESS_COLOR);
    }

    public static JButton createWarningButton(String text) {
        return createButton(text, UIConstants.WARNING_COLOR);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, UIConstants.DANGER_COLOR);
    }

    public static JButton createNeutralButton(String text) {
        return createButton(text, UIConstants.NEUTRAL_COLOR);
    }

    public static JButton createButton(String text, Color background) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color fillColor = background;
                if (getModel().isPressed()) {
                    fillColor = darken(background, 0.8f);
                } else if (getModel().isRollover()) {
                    fillColor = brighten(background, 1.1f);
                }

                g2.setColor(fillColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIConstants.FONT_HEADER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        return btn;
    }

    private static Color darken(Color color, float factor) {
        return new Color(
                (int) (color.getRed() * factor),
                (int) (color.getGreen() * factor),
                (int) (color.getBlue() * factor),
                color.getAlpha()
        );
    }

    private static Color brighten(Color color, float factor) {
        return new Color(
                Math.min((int) (color.getRed() * factor), 255),
                Math.min((int) (color.getGreen() * factor), 255),
                Math.min((int) (color.getBlue() * factor), 255),
                color.getAlpha()
        );
    }

    private ButtonFactory() {}
}
