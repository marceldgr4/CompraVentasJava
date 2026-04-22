package com.app.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades para formateo y parsing de fechas en el sistema.
 */
public final class DateUtils {

    /** Formato de fecha estándar para mostrar en la interfaz (DD/MM/YYYY). */
    public static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Formato de fecha y hora para logs y registros. */
    public static final DateTimeFormatter DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtils() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Formatea un {@link LocalDate} al formato de visualización {@code dd/MM/yyyy}.
     *
     * @param date fecha a formatear
     * @return cadena formateada, o cadena vacía si es null
     */
    public static String format(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE) : "";
    }

    /**
     * Formatea un {@link LocalDateTime} al formato {@code dd/MM/yyyy HH:mm}.
     *
     * @param dateTime instante a formatear
     * @return cadena formateada, o cadena vacía si es null
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME) : "";
    }

    /**
     * Parsea una cadena {@code dd/MM/yyyy} a {@link LocalDate}.
     *
     * @param value cadena de entrada
     * @return fecha parseada, o {@code null} si la cadena es inválida
     */
    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim(), DISPLAY_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Calcula los días restantes hasta una fecha de devolución.
     *
     * @param returnDate fecha límite de devolución
     * @return número de días (negativo si ya venció)
     */
    public static long daysUntilReturn(LocalDate returnDate) {
        if (returnDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), returnDate);
    }

    /**
     * Retorna {@code true} si la fecha dada ya pasó (está vencida).
     *
     * @param date fecha a evaluar
     */
    public static boolean isOverdue(LocalDate date) {
        return date != null && LocalDate.now().isAfter(date);
    }
}
