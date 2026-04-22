package com.app.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades para formateo y manejo de valores monetarios.
 */
public final class CurrencyUtils {

    /** Locale colombiano para formato de moneda (COP). */
    private static final Locale LOCALE_CO = new Locale("es", "CO");

    private CurrencyUtils() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Formatea un {@link BigDecimal} como moneda en pesos colombianos.
     * Ejemplo: {@code 1500000} → {@code "$1.500.000"}
     *
     * @param amount valor a formatear
     * @return cadena formateada, o {@code "$0"} si el valor es null
     */
    public static String format(BigDecimal amount) {
        if (amount == null) return "$0";
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_CO);
        nf.setMaximumFractionDigits(0);
        return nf.format(amount);
    }

    /**
     * Formatea un {@code double} como moneda en pesos colombianos.
     *
     * @param amount valor a formatear
     * @return cadena formateada
     */
    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }

    /**
     * Convierte una cadena de texto a {@link BigDecimal} eliminando caracteres
     * no numéricos (útil para parsear entradas del usuario con puntos de miles).
     *
     * @param value cadena de entrada
     * @return valor parseado, o {@link BigDecimal#ZERO} si no es válido
     */
    public static BigDecimal parse(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        // Eliminar símbolos de moneda, puntos de miles y espacios
        String cleaned = value.replaceAll("[^\\d,\\.]", "")
                              .replace(".", "")
                              .replace(",", ".");
        try {
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Redondea un {@link BigDecimal} a 2 decimales usando media aritmética.
     *
     * @param value valor a redondear
     * @return valor redondeado
     */
    public static BigDecimal round(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
