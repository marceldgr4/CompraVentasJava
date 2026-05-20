package com.app.ViewModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;


public abstract class BaseViewModel{
    public interface ViewModelListener{
        void onEvent(String event, Object data);
    }
    private final List<ViewModelListener> listeners = new ArrayList<>();

    public void addListener(ViewModelListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void removeListener(ViewModelListener listener){
        listeners.remove(listener);
    }

    protected void notifyListener(String event, Object data){
        listeners.forEach(listener -> listener.onEvent(event,data));
    }


    protected List<Observer> observers = new java.util.ArrayList<>();

        public void  addObserver(Observer observer){
            if(! observers.contains(observer)){
                observers.add(observer);
            }
        }

        public void  removeObserver(Observer observer){
            observers.remove(observer);
        }

        protected  void notifyObservers(String event, Object data){
            for(Observer observer : observers){
                observer.update(null, new ViewModelEvent(event,data));
            }
        }
    public class ViewModelEvent {
        public final String event;
        public final Object data;

        public ViewModelEvent(String event, Object data) {
            this.event = event;
            this.data = data;
        }
    }
}
