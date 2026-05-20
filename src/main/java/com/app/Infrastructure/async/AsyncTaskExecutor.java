package com.app.Infrastructure.async;

import javax.swing.*;

public abstract class AsyncTaskExecutor<T> extends SwingWorker<T, Void> {
    private final  Runnable onStart;
    private final OnSuccess<T> onSuccess;
    private final OnError onError;

    public AsyncTaskExecutor(Runnable onStart, OnSuccess<T> onSuccess, OnError onError) {
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }
    @Override
    protected final T doInBackground() throws Exception {
        return executeTask();
    }
    @Override
    protected final void done() {
        try{
            T result = get();
            onSuccess.accept(result);

        }catch (Exception ex){
            onError.accept(ex);
        }
    }
    public final void executeAsync(){
        if(onStart != null) onStart.run();
        execute();
    }

    protected abstract T executeTask() throws Exception;

    @FunctionalInterface
    public interface OnSuccess<T> {
        void accept(T result);
    }
    @FunctionalInterface
    public interface OnError {
        void accept(Exception error);
    }

}
