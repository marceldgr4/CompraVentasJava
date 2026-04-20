package com.app.ViewModel;

public class ViewModelEvent {
    public final String event;
    public final Object data;

    public ViewModelEvent(String event, Object data) {
        this.event = event;
        this.data = data;
    }
}
