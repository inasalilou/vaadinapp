package com.inas.vaadinapp.observer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implémentation simple du pattern Observer (générique).
 */
public final class SimpleEventBus<T> implements Observable<T> {

    private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();

    @Override
    public Subscription subscribe(Observer<T> observer) {
        Objects.requireNonNull(observer, "observer");
        observers.add(observer);
        return () -> observers.remove(observer);
    }

    public void publish(T event) {
        for (Observer<T> observer : observers) {
            observer.onEvent(event);
        }
    }

    public int observerCount() {
        return observers.size();
    }
}
