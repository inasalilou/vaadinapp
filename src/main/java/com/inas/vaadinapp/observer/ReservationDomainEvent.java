package com.inas.vaadinapp.observer;

import java.time.LocalDateTime;

import com.inas.vaadinapp.entity.ReservationStatus;

public final class ReservationDomainEvent {

    public enum Type {
        CREATED,
        CONFIRMED,
        CANCELLED
    }

    private final Type type;
    private final Long reservationId;
    private final Long eventId;
    private final Long userId;
    private final ReservationStatus status;
    private final LocalDateTime occurredAt;

    public ReservationDomainEvent(Type type,
                                 Long reservationId,
                                 Long eventId,
                                 Long userId,
                                 ReservationStatus status,
                                 LocalDateTime occurredAt) {
        this.type = type;
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public Type getType() {
        return type;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
