package com.inas.vaadinapp.observer;

import java.util.function.Consumer;

public interface Observable<T> {

    /**
     * Subscribe and return an unsubscribe handle.
     */
    Subscription subscribe(Observer<T> observer);

    /**
     * Convenience overload for Java's Consumer.
     */
    default Subscription subscribeConsumer(Consumer<T> consumer) {
        return subscribe(consumer::accept);
    }

    interface Subscription extends AutoCloseable {
        @Override
        void close();
    }
}
