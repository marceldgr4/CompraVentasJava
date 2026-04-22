package com.app.Utils;

/**
 * Utilidades para manipulación y validación de cadenas de texto.
 */
public final class StringUtils {

    private StringUtils() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Retorna {@code true} si la cadena es null o está en blanco.
     *
     * @param value cadena a evaluar
     */
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Capitaliza la primera letra de cada palabra en una cadena.
     * Ejemplo: {@code "juan perez"} → {@code "Juan Perez"}
     *
     * @param value cadena de entrada
     * @return cadena con cada palabra capitalizada, o cadena vacía si el input es null
     */
    public static String capitalizeWords(String value) {
        if (isNullOrBlank(value)) return "";
        String[] words = value.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Trunca una cadena al máximo de caracteres indicado, añadiendo "..." si se truncó.
     *
     * @param value  cadena original
     * @param maxLen longitud máxima (sin contar "...")
     * @return cadena truncada o la original si cabe
     */
    public static String truncate(String value, int maxLen) {
        if (isNullOrBlank(value) || value.length() <= maxLen) return value;
        return value.substring(0, maxLen) + "...";
    }

    /**
     * Limpia y normaliza una cadena: recorta espacios y colapsa espacios internos múltiples.
     *
     * @param value cadena de entrada
     * @return cadena normalizada, o cadena vacía si el input es null
     */
    public static String normalize(String value) {
        if (isNullOrBlank(value)) return "";
        return value.trim().replaceAll("\\s+", " ");
    }
}
