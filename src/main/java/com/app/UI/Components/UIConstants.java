package com.app.UI.Components;
import java.awt.*;

/**
 * UI Constants centralizados - colores, fuentes, tamaños.
 */
public class UIConstants {

    // Colores
    public static final Color PRIMARY_COLOR = new Color(30, 136, 229);
    public static final Color SUCCESS_COLOR = new Color(56, 142, 60);
    public static final Color WARNING_COLOR = new Color(245, 124, 0);
    public static final Color DANGER_COLOR = new Color(211, 47, 47);
    public static final Color NEUTRAL_COLOR = new Color(97, 97, 97);

    public static final Color TEXT_PRIMARY = new Color(50, 50, 50);
    public static final Color TEXT_SECONDARY = new Color(150, 150, 150);
    public static final Color BG_PRIMARY = Color.WHITE;
    public static final Color BG_SECONDARY = new Color(245, 247, 250);

    // Fuentes
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // Tamaños
    public static final Dimension BUTTON_SIZE = new Dimension(100, 40);
    public static final Dimension FIELD_SIZE = new Dimension(300, 45);
    public static final int BORDER_RADIUS = 8;
    public static final int PADDING = 16;
    public static final int SPACING = 12;

    private UIConstants() {}
}

