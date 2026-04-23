package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

public final class ButtonFactory {
    // ── Colores oficiales de la aplicación ────────────────────────────────────
    private static final Color PRIMARY = UIConstants.PRIMARY_COLOR;  // #1E88E5
    private static final Color SUCCESS = UIConstants.SUCCESS_COLOR;  // #388E3C
    private static final Color WARNING = UIConstants.WARNING_COLOR;  // #F57C00
    private static final Color DANGER = UIConstants.DANGER_COLOR;   // #D32F2F
    private static final Color NEUTRAL = UIConstants.NEUTRAL_COLOR;  // #616161
    private static final Color AMBER = new Color(255, 143, 0);     // #FF8F00

    private ButtonFactory() {
    }

    // ── Métodos de conveniencia ────────────────────────────────────────────────

    /** Botón azul primario (crear, buscar, guardar). */
    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY);
    }

    /** Sobrecarga de compatibilidad con código que pasaba un segundo argumento. */
    public static JButton createPrimaryButton(String text, Object ignored) {
        return createButton(text, PRIMARY);
    }

    /** Botón verde (marcar devuelto, confirmar). */
    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS);
    }

    /** Botón naranja/amarillo (editar, advertencia). */
    public static JButton createWarningButton(String text) {
        return createButton(text, WARNING);
    }

    /** Botón rojo (eliminar, acción destructiva). */
    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER);
    }

    /** Botón gris neutro (refresh, cancelar). */
    public static JButton createNeutralButton(String text) {
        return createButton(text, NEUTRAL);
    }

    /** Botón ámbar (procesar vencidos, acciones de alerta media). */
    public static JButton createAmberButton(String text) {
        return createButton(text, AMBER);
    }

    public static JButton createButton(String text, Color background) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color fill = background;
                if (!isEnabled()) {
                    fill = desaturate(background, 0.4f);
                } else if (getModel().isPressed()) {
                    fill = darken(background, 0.78f);
                } else if (getModel().isRollover()) {
                    fill = brighten(background, 1.12f);
                }

                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        UIConstants.BORDER_RADIUS, UIConstants.BORDER_RADIUS);

                // Texto: llamar al super sobre fondo transparente
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Sin borde nativo; el redondeado ya actúa como borde visual
            }
        };

        btn.setContentAreaFilled(false);  // desactiva relleno nativo del L&F
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIConstants.FONT_HEADER);  // Segoe UI Bold 14
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Padding interno uniforme
        btn.setMargin(new Insets(5, 14, 5, 14));

        return btn;
    }

    // ── Helpers de color ──────────────────────────────────────────────────────

    private static Color darken(Color c, float factor) {
        return new Color(
                clamp((int) (c.getRed()   * factor)),
                clamp((int) (c.getGreen() * factor)),
                clamp((int) (c.getBlue()  * factor)),
                c.getAlpha());
    }

    private static Color brighten(Color c, float factor) {
        return new Color(
                clamp((int) (c.getRed()   * factor)),
                clamp((int) (c.getGreen() * factor)),
                clamp((int) (c.getBlue()  * factor)),
                c.getAlpha());
    }

    /** Mezcla el color con gris para simular estado deshabilitado. */
    private static Color desaturate(Color c, float mix) {
        int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        return new Color(
                clamp((int) (c.getRed()   * (1 - mix) + gray * mix)),
                clamp((int) (c.getGreen() * (1 - mix) + gray * mix)),
                clamp((int) (c.getBlue()  * (1 - mix) + gray * mix)),
                c.getAlpha());
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
