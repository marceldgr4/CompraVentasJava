package com.app.ViewModel;

import javax.swing.*;
import java.util.List;
import java.util.Observer;


public abstract class BaseViewModel{
        protected List<Observer> observers = new java.util.ArrayList<>();

        public void  addObserver(java.util.Observer observer){
            if(! observers.contains(observer)){
                observers.add(observer);
            }
        }

        public void  removeObserver(java.util.Observer observer){
            observers.remove(observer);
        }

        protected  void notifyObservers(String event, Object data){
            for(java.util.Observer observer : observers){
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
