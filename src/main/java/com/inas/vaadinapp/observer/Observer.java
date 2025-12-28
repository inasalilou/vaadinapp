package com.inas.vaadinapp.observer;

@FunctionalInterface
public interface Observer<T> {
    void onEvent(T event);
}
