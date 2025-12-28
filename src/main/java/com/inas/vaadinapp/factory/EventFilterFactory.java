package com.inas.vaadinapp.factory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;

/**
 * Factory de Predicates réutilisables pour filtrer des événements.
 * (Pattern Factory + functional interfaces)
 */
public final class EventFilterFactory {

    private EventFilterFactory() {
    }

    public static Predicate<Event> byCategory(Category category) {
        return e -> category == null || e.getCategorie() == category;
    }

    public static Predicate<Event> startAfter(LocalDateTime start) {
        return e -> start == null || (e.getDateDebut() != null && e.getDateDebut().isAfter(start));
    }

    public static Predicate<Event> startBefore(LocalDateTime end) {
        return e -> end == null || (e.getDateDebut() != null && e.getDateDebut().isBefore(end));
    }

    public static Predicate<Event> maxPrice(Double prixMax) {
        return e -> prixMax == null || (e.getPrixUnitaire() != null && e.getPrixUnitaire() <= prixMax);
    }

    public static Predicate<Event> andAll(List<Predicate<Event>> predicates) {
        return Optional.ofNullable(predicates)
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .reduce(event -> true, Predicate::and);
    }
}
