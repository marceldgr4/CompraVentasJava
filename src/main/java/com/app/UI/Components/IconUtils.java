package com.app.UI.Components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * IconUtils - Utilidad reutilizable para la carga segura de iconos, escalado de imágenes,
 * manejo de transparencia PNG y compatibilidad total con ImageIcon en Java Swing.
 * 
 * Incluye un robusto mecanismo de respaldo (fallback) vectorial mediante Graphics2D 
 * para garantizar que siempre se devuelva un icono visualmente profesional y consistente,
 * incluso si los archivos de recursos físicos no se encuentran en el sistema.
 */
public final class IconUtils {

    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();
    private static final int DEFAULT_SIZE = 20;

    private IconUtils() {
        // Clase utilitaria privada
    }

    /**
     * Carga un icono de forma segura con el tamaño por defecto (20x20).
     *
     * @param name Nombre del icono (ej. "search", "refresh", "add", "edit", "delete")
     * @return ImageIcon escalado y seguro para usar en componentes Swing.
     */
    public static ImageIcon getIcon(String name) {
        return getIcon(name, DEFAULT_SIZE);
    }

    /**
     * Carga un icono de forma segura especificando su tamaño.
     * Utiliza caché para optimizar el rendimiento y memoria.
     *
     * @param name Nombre del icono
     * @param size Tamaño en píxeles (ancho y alto uniforme)
     * @return ImageIcon escalado y seguro.
     */
    public static ImageIcon getIcon(String name, int size) {
        String cacheKey = name + "_" + size;
        if (ICON_CACHE.containsKey(cacheKey)) {
            return ICON_CACHE.get(cacheKey);
        }

        ImageIcon icon = loadFromFileOrResource(name, size);
        if (icon == null) {
            icon = generateVectorFallbackIcon(name, size);
        }

        ICON_CACHE.put(cacheKey, icon);
        return icon;
    }

    /**
     * Intenta cargar el archivo de imagen desde el classpath o sistema de archivos.
     */
    private static ImageIcon loadFromFileOrResource(String name, int size) {
        try {
            // Intentar cargar desde recursos del classpath
            String resourcePath = "/icons/" + (name.endsWith(".png") ? name : name + ".png");
            URL url = IconUtils.class.getResource(resourcePath);
            if (url != null) {
                ImageIcon original = new ImageIcon(url);
                return scaleIcon(original, size);
            }
        } catch (Exception e) {
            // Falla silenciosa, se usará el fallback vectorial
        }
        return null;
    }

    /**
     * Escala un ImageIcon existente garantizando alta calidad y preservando transparencia.
     *
     * @param source Icono original
     * @param size Tamaño deseado
     * @return ImageIcon escalado
     */
    public static ImageIcon scaleIcon(ImageIcon source, int size) {
        if (source == null || source.getImage() == null) {
            return null;
        }
        Image img = source.getImage();
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(img, 0, 0, size, size, null);
        g2.dispose();
        return new ImageIcon(bi);
    }

    /**
     * Genera un icono vectorial de alta calidad en memoria como respaldo (fallback).
     * Garantiza que la interfaz siempre tenga iconos modernos, consistentes y sin errores visuales.
     */
    private static ImageIcon generateVectorFallbackIcon(String name, int size) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        
        // Configuración de renderizado de alta calidad
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo transparente por defecto
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, size, size);
        g2.setComposite(AlphaComposite.SrcOver);

        // Color del icono (blanco por defecto para contrastar con botones de color)
        g2.setColor(Color.WHITE);
        String cleanName = name.toLowerCase().replace(".png", "").trim();

        int pad = Math.max(2, size / 8);
        int w = size - 2 * pad;
        int h = size - 2 * pad;

        switch (cleanName) {
            case "search":
            case "buscar":
                drawSearchIcon(g2, pad, w, h);
                break;
            case "refresh":
            case "reload":
            case "actualizar":
                drawRefreshIcon(g2, pad, w, h);
                break;
            case "add":
            case "new":
            case "nuevo":
                drawAddIcon(g2, pad, w, h);
                break;
            case "edit":
            case "editar":
                drawEditIcon(g2, pad, w, h);
                break;
            case "delete":
            case "eliminar":
                drawDeleteIcon(g2, pad, w, h);
                break;
            case "print":
            case "imprimir":
                drawPrintIcon(g2, pad, w, h);
                break;
            case "warning":
            case "process":
            case "procesar":
                drawWarningIcon(g2, pad, w, h);
                break;
            case "toggle":
            case "activar":
                drawToggleIcon(g2, pad, w, h);
                break;
            case "logout":
            case "exit":
            case "cerrar":
                drawLogoutIcon(g2, pad, w, h);
                break;
            default:
                drawDefaultIcon(g2, pad, w, h);
                break;
        }

        g2.dispose();
        return new ImageIcon(bi);
    }

    private static void drawSearchIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int r = (int) (w * 0.65);
        g2.draw(new Ellipse2D.Double(pad, pad, r, r));
        int startX = (int) (pad + r * 0.85);
        int startY = (int) (pad + r * 0.85);
        g2.draw(new Line2D.Double(startX, startY, pad + w, pad + h));
    }

    private static void drawRefreshIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int arcPad = pad + 2;
        int arcW = w - 4;
        int arcH = h - 4;
        g2.drawArc(arcPad, arcPad, arcW, arcH, 45, 270);
        
        // Flecha del refresh
        int arrowX = arcPad + arcW / 2;
        int arrowY = arcPad;
        g2.fillPolygon(
            new int[]{arrowX - 4, arrowX + 4, arrowX},
            new int[]{arrowY - 4, arrowY - 4, arrowY + 4},
            3
        );
    }

    private static void drawAddIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int midX = pad + w / 2;
        int midY = pad + h / 2;
        g2.draw(new Line2D.Double(midX, pad + 2, midX, pad + h - 2));
        g2.draw(new Line2D.Double(pad + 2, midY, pad + w - 2, midY));
    }

    private static void drawEditIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int px = pad + 2;
        int py = pad + h - 2;
        
        // Representación de un lápiz inclinado
        g2.draw(new Line2D.Double(px, py, px + w / 4.0, py));
        g2.draw(new Line2D.Double(px, py, px, py - h / 4.0));
        g2.draw(new Line2D.Double(px + w / 4.0, py, pad + w, pad + h / 4.0));
        g2.draw(new Line2D.Double(px, py - h / 4.0, pad + w - w / 4.0, pad));
        g2.draw(new Line2D.Double(pad + w - w / 4.0, pad, pad + w, pad + h / 4.0));
    }

    private static void drawDeleteIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int topY = pad + h / 4;
        g2.draw(new Line2D.Double(pad, topY, pad + w, topY)); // Tapa
        g2.draw(new Rectangle2D.Double(pad + w / 4.0, pad, w / 2.0, h / 4.0 - 2)); // Asa
        g2.draw(new RoundRectangle2D.Double(pad + 2, topY + 2, w - 4, h - h / 4.0 - 2, 4, 4)); // Cuerpo
        g2.draw(new Line2D.Double(pad + w / 3.0, topY + 5, pad + w / 3.0, pad + h - 3));
        g2.draw(new Line2D.Double(pad + w * 2 / 3.0, topY + 5, pad + w * 2 / 3.0, pad + h - 3));
    }

    private static void drawPrintIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int subH = h / 3;
        g2.draw(new Rectangle2D.Double(pad + w / 4.0, pad, w / 2.0, subH)); // Papel superior
        g2.draw(new RoundRectangle2D.Double(pad, pad + subH, w, subH * 1.5, 4, 4)); // Impresora
        g2.draw(new Rectangle2D.Double(pad + w / 5.0, pad + subH * 1.8, w * 0.6, subH)); // Papel inferior
    }

    private static void drawWarningIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int midX = pad + w / 2;
        int botY = pad + h;
        Polygon triangle = new Polygon(
            new int[]{midX, pad, pad + w},
            new int[]{pad, botY, botY},
            3
        );
        g2.draw(triangle);
        g2.fillOval(midX - 1, pad + h / 3, 2, h / 3);
        g2.fillOval(midX - 1, pad + h - 4, 2, 2);
    }

    private static void drawToggleIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f));
        int cy = pad + h / 4;
        g2.draw(new RoundRectangle2D.Double(pad, cy, w, h / 2.0, h / 2.0, h / 2.0));
        g2.fill(new Ellipse2D.Double(pad + w / 2.0 + 1, cy + 2, h / 2.0 - 4, h / 2.0 - 4));
    }

    private static void drawLogoutIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Puerta (rectángulo abierto a la derecha)
        g2.draw(new Line2D.Double(pad + w / 2.0, pad, pad, pad));
        g2.draw(new Line2D.Double(pad, pad, pad, pad + h));
        g2.draw(new Line2D.Double(pad, pad + h, pad + w / 2.0, pad + h));
        // Flecha saliendo
        int arrowY = pad + h / 2;
        g2.draw(new Line2D.Double(pad + w / 4.0, arrowY, pad + w, arrowY));
        g2.fillPolygon(
            new int[]{pad + w - 4, pad + w, pad + w - 4},
            new int[]{arrowY - 4, arrowY, arrowY + 4},
            3
        );
    }

    private static void drawDefaultIcon(Graphics2D g2, int pad, int w, int h) {
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new RoundRectangle2D.Double(pad, pad, w, h, 6, 6));
        g2.fillOval(pad + w / 2 - 2, pad + h / 2 - 2, 4, 4);
    }
}
