package com.back.Observer;

import java.util.ArrayList;
import java.util.List;

public class Observable {
    protected final List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String id) {
        for (Observer observer : observers) {
            observer.update("Machine is idle", id);
        }
    }
}