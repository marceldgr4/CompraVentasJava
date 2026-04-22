package com.app.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utilidades para operaciones comunes sobre colecciones.
 */
public final class CollectionUtils {

    private CollectionUtils() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Retorna {@code true} si la colección es null o está vacía.
     *
     * @param collection colección a evaluar
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Retorna la colección dada, o una lista vacía inmutable si es null.
     *
     * @param collection colección de entrada
     * @param <T>        tipo de los elementos
     * @return colección nunca null
     */
    public static <T> Collection<T> emptyIfNull(Collection<T> collection) {
        return collection != null ? collection : Collections.emptyList();
    }

    /**
     * Filtra una lista usando el predicado dado.
     *
     * @param list      lista de entrada
     * @param predicate predicado de filtrado
     * @param <T>       tipo de los elementos
     * @return nueva lista filtrada
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (isNullOrEmpty(list)) return Collections.emptyList();
        return list.stream().filter(predicate).collect(Collectors.toList());
    }
}
