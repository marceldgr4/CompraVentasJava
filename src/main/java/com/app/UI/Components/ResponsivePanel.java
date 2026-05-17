package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;

/**
 * ResponsivePanel - Contenedor dinámico y adaptativo diseñado para resolver
 * los problemas de superposición y rotura visual al redimensionar ventanas en Java Swing.
 *
 * Utiliza un gestor de diseño fluido (WrapLayout personalizado en un solo nivel)
 * que recalcula de forma dinámica el tamaño preferido del contenedor según el ancho disponible,
 * garantizando que los filtros, formularios y botones fluyan hacia abajo de manera
 * individual, organizada y profesional sin encimarse ni cortarse.
 */
public class ResponsivePanel extends JPanel {

    public ResponsivePanel() {
        super();
        // Usamos un único WrapLayout fluido en el contenedor principal para que todos los
        // componentes (filtros y botones) compartan el mismo cálculo de ancho real y hagan
        // salto de línea (wrap) de forma individual e instantánea al redimensionar la ventana.
        setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        setOpaque(false);
    }

    /**
     * Agrega un componente de filtro (ej. Filtros, Buscadores, Campos de ID).
     */
    public void addFilterComponent(Component comp) {
        add(comp);
        revalidate();
        repaint();
    }

    /**
     * Agrega un componente de acción (ej. Botones de acción, Nuevo, Exportar).
     */
    public void addActionComponent(Component comp) {
        add(comp);
        revalidate();
        repaint();
    }

    /**
     * Limpia todos los componentes internos.
     */
    public void clearComponents() {
        removeAll();
        revalidate();
        repaint();
    }

    /**
     * WrapLayout - Gestor de diseño fluido adaptativo para Java Swing.
     * Extiende FlowLayout para recalcular correctamente el preferredLayoutSize
     * basándose en el ancho real del contenedor padre, permitiendo el salto de línea dinámico.
     */
    public static class WrapLayout extends FlowLayout {

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                Container container = target.getParent();
                
                // Obtenemos el ancho real disponible del contenedor padre más cercano
                if (container != null && container.getSize().width > 0) {
                    targetWidth = container.getSize().width;
                } else if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);

                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }

                        if (rowWidth != 0) { rowWidth += hgap; }
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                addRow(dim, rowWidth, rowHeight);

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) { dim.height += getVgap(); }
            dim.height += rowHeight;
        }
    }
}
