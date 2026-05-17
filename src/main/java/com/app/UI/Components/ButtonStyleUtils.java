package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

/**
 * ButtonStyleUtils - Utilidad para estandarizar y homogeneizar el estilo visual
 * de los botones en toda la aplicación Swing.
 *
 * Proporciona métodos para aplicar padding, márgenes, tamaños mínimos y preferidos,
 * fuentes, espaciados, alineaciones y la correcta integración con IconUtils.
 */
public final class ButtonStyleUtils {

    private ButtonStyleUtils() {
        // Clase utilitaria privada
    }

    /**
     * Aplica el estilo moderno base a un botón existente.
     *
     * @param button Botón a estilar
     * @param iconName Nombre del ícono opcional (puede ser null)
     * @param font Fuente a utilizar
     * @param fgColor Color del texto
     */
    public static void applyModernStyle(JButton button, String iconName, Font font, Color fgColor) {
        if (button == null) return;

        button.setFont(font != null ? font : UIConstants.FONT_HEADER);
        button.setForeground(fgColor != null ? fgColor : Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ajustes de L&F nativo
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        // Padding y márgenes estandarizados
        button.setMargin(new Insets(6, 16, 6, 16));

        // Asignación de icono seguro si se especifica
        if (iconName != null && !iconName.trim().isEmpty()) {
            ImageIcon icon = IconUtils.getIcon(iconName, 20);
            if (icon != null) {
                button.setIcon(icon);
                button.setIconTextGap(8);
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setVerticalAlignment(SwingConstants.CENTER);
            }
        }
    }

    /**
     * Configura dimensiones uniformes (mínima y preferida) para un botón.
     *
     * @param button Botón a configurar
     * @param width Ancho deseado
     * @param height Alto deseado
     */
    public static void setStandardDimensions(JButton button, int width, int height) {
        if (button == null) return;
        Dimension dim = new Dimension(width, height);
        button.setPreferredSize(dim);
        button.setMinimumSize(dim);
    }
}
