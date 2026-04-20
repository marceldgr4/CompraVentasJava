package com.app.ViewModel;

import javax.swing.*;


    public abstract class BaseViewModel{
        protected java.util.List<java.util.Observer> observers = new java.util.ArrayList<>();

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

    }
