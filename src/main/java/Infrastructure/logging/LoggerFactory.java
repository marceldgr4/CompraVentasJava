package Infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fábrica de loggers para el sistema de compraventa/empeño.
 * Provee una forma centralizada de obtener un {@link Logger} de SLF4J
 * (implementado con Logback declarado en el pom.xml).
 *
 * <p>Uso:
 * <pre>{@code
 *   private static final Logger log = AppLoggerFactory.getLogger(MiClase.class);
 *   log.info("Mensaje de información");
 *   log.error("Error inesperado", exception);
 * }</pre>
 */
public final class AppLoggerFactory {

    private AppLoggerFactory() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Obtiene el logger SLF4J para la clase indicada.
     *
     * @param clazz clase solicitante del logger
     * @return instancia de {@link Logger}
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Obtiene el logger SLF4J por nombre.
     *
     * @param name nombre del logger (por ejemplo, nombre de módulo o funcionalidad)
     * @return instancia de {@link Logger}
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
}
