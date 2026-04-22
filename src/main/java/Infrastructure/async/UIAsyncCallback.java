package Infrastructure.async;

import javax.swing.SwingUtilities;
import java.util.function.Consumer;

/**
 * Callback de resultado asíncrono que garantiza la ejecución en el hilo de
 * despacho de eventos de Swing (EDT — Event Dispatch Thread).
 *
 * <p>Uso típico con {@link AsyncTaskExecutor}:
 * <pre>{@code
 *   UIAsyncCallback<List<Pawn>> callback = new UIAsyncCallback<>(
 *       result -> tableModel.setData(result),
 *       error  -> showError(error.getMessage())
 *   );
 * }</pre>
 *
 * @param <T> tipo del resultado esperado
 */
public class UIAsyncCallback<T> {

    private final Consumer<T> onSuccess;
    private final Consumer<Exception> onError;

    /**
     * Construye un callback con manejadores de éxito y error.
     *
     * @param onSuccess consumidor invocado en el EDT cuando la tarea termina exitosamente
     * @param onError   consumidor invocado en el EDT cuando la tarea lanza una excepción
     */
    public UIAsyncCallback(Consumer<T> onSuccess, Consumer<Exception> onError) {
        this.onSuccess = onSuccess;
        this.onError   = onError;
    }

    /**
     * Ejecuta el callback de éxito en el EDT.
     *
     * @param result resultado de la tarea asíncrona
     */
    public void success(T result) {
        SwingUtilities.invokeLater(() -> onSuccess.accept(result));
    }

    /**
     * Ejecuta el callback de error en el EDT.
     *
     * @param ex excepción producida
     */
    public void failure(Exception ex) {
        SwingUtilities.invokeLater(() -> onError.accept(ex));
    }
}
