package com.inas.vaadinapp.observer;

/**
 * Singleton d'accès aux bus d'événements du domaine.
 */
public final class DomainEvents {

    private static final DomainEvents INSTANCE = new DomainEvents();

    private final SimpleEventBus<ReservationDomainEvent> reservationEvents = new SimpleEventBus<>();

    private DomainEvents() {
    }

    public static DomainEvents getInstance() {
        return INSTANCE;
    }

    public SimpleEventBus<ReservationDomainEvent> reservationEvents() {
        return reservationEvents;
    }
}
