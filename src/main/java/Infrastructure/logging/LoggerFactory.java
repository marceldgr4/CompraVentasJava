package Infrastructure.logging;

import org.slf4j.Logger;

/**
 * Fábrica de loggers para el sistema de compraventa/empeño.
 * Provee una forma centralizada de obtener un {@link Logger} de SLF4J
 * (implementado con Logback declarado en el pom.xml).
 *
 * <p>Uso:
 * <pre>{@code
 *   private static final Logger log = LoggerFactory.getLogger(MiClase.class);
 *   log.info("Mensaje de información");
 *   log.error("Error inesperado", exception);
 * }</pre>
 *
 * <p>Nota: esta clase se llama igual que {@code org.slf4j.LoggerFactory}, por eso
 * se usa el nombre completamente calificado internamente para evitar la colisión.
 */
public final class LoggerFactory {

    private LoggerFactory() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Obtiene el logger SLF4J para la clase indicada.
     *
     * @param clazz clase solicitante del logger
     * @return instancia de {@link Logger}
     */
    public static Logger getLogger(Class<?> clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    /**
     * Obtiene el logger SLF4J por nombre.
     *
     * @param name nombre del logger (por ejemplo, nombre de módulo o funcionalidad)
     * @return instancia de {@link Logger}
     */
    public static Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name);
    }
}
