package com.app.Controllers;

import com.app.Infrastructure.logging.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.Component;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public abstract class BaseController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @FunctionalInterface
    public interface OnSuccess<T>{
        void onResult(T result);
    }
    @FunctionalInterface
    public interface OnError{
        void onError(String message, Throwable cause);
    }
    /* -------------------------------------------------------
    // Ejecución asíncrona genérica
    // -------------------------------------------------------
    *  Ejecuta una operación de fondo (I/O, BD, red) en un hilo separado y
    * despacha el resultado al EDT al terminar.*/

    protected <T> void runAsync(
            Supplier<T> task,
            OnSuccess<T> onSuccess,
            OnError onError){
        new SwingWorker<T,Void>(){
            @Override
            protected T doInBackground() throws Exception {
                return task.get();
            }
            @Override
            protected void done() {
                try{
                    T result = get();
                    onSuccess.onResult(result);
                }catch (ExecutionException ex){
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.error("Error en Operacion Asicrona {}",cause.getMessage(),cause);
                    onError.onError(cause.getMessage(),cause);

                }catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                    log.warn("Operacion Asicrona interrumpida",ex);
                    onError.onError("Operacion Interrumpida",ex);
                }
            }
        }.execute();
    }
/*
 * Ejecuta una operación de fondo que no retorna resultado (void).
 */
    protected void  runAsyncVoid( RunnableWithException task, Runnable onSuccess, OnError onError) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    onSuccess.run();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.error("Error en operación void asíncrona: {}", cause.getMessage(), cause);
                    onError.onError(cause.getMessage(), cause);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.warn("Operación void asíncrona interrumpida", ex);
                    onError.onError("Operación interrumpida.", ex);
                }
            }
        }.execute();
    }
    /*-------------------------------------------------------
    // Interfaz funcional auxiliar
     * Equivalente a {@link Runnable} pero que puede lanzar {@link Exception}.
     * Permite usar lambdas con checked exceptions en {@link #runAsyncVoid}.
     */
    @FunctionalInterface
    public interface RunnableWithException{
        void run() throws Exception;
    }
    /*-------------------------------------------------------
    // Helpers de UI
     * Muestra un diálogo de error centrado en el componente padre.
     * Muestra un diálogo de éxito centrado en el componente padre.
     * Muestra un diálogo de advertencia centrado en el componente padre.
     */
    protected void showError(Component parent, String message){
        SwingUtilities.invokeLater(()-> JOptionPane.showMessageDialog(parent,message,"Error",JOptionPane.ERROR_MESSAGE));
    }

    protected void showSuccess(Component parent, String message){
        SwingUtilities.invokeLater(()-> JOptionPane.showMessageDialog(parent,message,"Exito",JOptionPane.PLAIN_MESSAGE));

    }
    protected void showWarning(Component parent, String message){
        SwingUtilities.invokeLater(()-> JOptionPane.showMessageDialog(parent,message,"Advertencia",JOptionPane.WARNING_MESSAGE));
    }
    /*
     * Muestra un diálogo de confirmación y retorna la decisión del usuario.
     * @param message pregunta de confirmación
     * @param title título del diálogo
     * @return {@code true} si el usuario confirmó
     */
    protected boolean showConfirmation(Component parent, String message, String title){
        int result = JOptionPane.showConfirmDialog(parent,message,title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

}
