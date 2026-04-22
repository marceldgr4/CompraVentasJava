package com.app.Utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utilidades para validación de datos de entrada en el sistema.
 */
public final class ValidationUtils {

    /** Patrón básico de validación de correo electrónico. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");

    /** Patrón de teléfono colombiano (7-10 dígitos, puede iniciar con +57). */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+57)?[0-9]{7,10}$");

    private ValidationUtils() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Valida que un correo electrónico tenga formato correcto.
     *
     * @param email correo a validar
     * @return {@code true} si el formato es válido
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida que un número de teléfono colombiano sea válido.
     *
     * @param phone teléfono a validar
     * @return {@code true} si el formato es válido
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.trim().replace(" ", "")).matches();
    }

    /**
     * Valida que un precio sea mayor que cero.
     *
     * @param price precio a evaluar
     * @return {@code true} si el precio es positivo
     */
    public static boolean isPositivePrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Valida que una cantidad sea un entero no negativo.
     *
     * @param amount cantidad a evaluar
     * @return {@code true} si la cantidad es >= 0
     */
    public static boolean isValidAmount(int amount) {
        return amount >= 0;
    }

    /**
     * Valida que un texto no sea null ni esté en blanco y no supere la longitud máxima.
     *
     * @param value  texto a validar
     * @param maxLen longitud máxima permitida
     * @return {@code true} si el texto es válido
     */
    public static boolean isValidText(String value, int maxLen) {
        return value != null && !value.isBlank() && value.length() <= maxLen;
    }
}
