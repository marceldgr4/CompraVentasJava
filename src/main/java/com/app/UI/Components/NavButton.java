package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

public class NavButton  extends JToggleButton {

    private static final Color COLOR_NORMAL   = new Color(20, 30, 60);
    private static final Color COLOR_HOVER    = new Color(35, 50, 95);
    private static final Color COLOR_SELECTED = new Color(25, 118, 210);
    private static final Color ACCENT_LINE    = new Color(100, 181, 246);

    private final String icon;
    private final String label;
    private boolean expanded = true;

    public NavButton(String icon, String label) {
        this.icon = icon;
        this.label = label;
        applyText(true);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
    }
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        applyText(expanded);
        setHorizontalAlignment(expanded ? SwingConstants.RIGHT : SwingConstants.CENTER);

        setBorder( expanded
                ? BorderFactory.createEmptyBorder(0, 18, 0, 18) :
                BorderFactory.createEmptyBorder(0,0,0,0));

    }

    private void applyText(boolean expanded) {
        setText(expanded ? icon+ " " + label : icon);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(isSelected()) {
            g2.setColor(COLOR_SELECTED);

        }else if(getModel().isRollover()) {
            g2.setColor(COLOR_HOVER);
        }else {
            g2.setColor(COLOR_NORMAL);
        }
        g2.fillRect(0, 0, getWidth(), getHeight());

        if(isSelected()) {
            g2.setColor(ACCENT_LINE);
            g2.fillRect(0,0,4, getHeight());
        }
        super.paintComponent(g);
        g2.dispose();

    }
}
