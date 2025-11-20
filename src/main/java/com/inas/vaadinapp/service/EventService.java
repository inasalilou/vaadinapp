package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /* --------------------- CREATION ------------------------ */

    // Création d'un événement (ADMIN ou ORGANIZER uniquement)
    public Event createEvent(Event event, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Seuls les ADMIN ou ORGANIZER peuvent créer un événement");
        }

        event.setCreateur(user);
        event.setStatus(EventStatus.BROUILLON);

        return eventRepository.save(event);
    }

    /* --------------------- MODIFICATION ------------------------ */

    // Modification (par créateur ou ADMIN)
    public Event updateEvent(Long eventId, Event updatedEvent, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!event.getCreateur().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Vous n'avez pas la permission de modifier cet événement");
        }

        if (event.getStatus() == EventStatus.TERMINE) {
            throw new IllegalArgumentException("Un événement terminé ne peut plus être modifié");
        }

        event.setTitre(updatedEvent.getTitre());
        event.setDescription(updatedEvent.getDescription());
        event.setCategorie(updatedEvent.getCategorie());
        event.setDateDebut(updatedEvent.getDateDebut());
        event.setDateFin(updatedEvent.getDateFin());
        event.setVille(updatedEvent.getVille());
        event.setLieu(updatedEvent.getLieu());
        event.setPrix(updatedEvent.getPrix());
        event.setCapaciteMax(updatedEvent.getCapaciteMax());

        return eventRepository.save(event);
    }

    /* --------------------- PUBLICATION ------------------------ */

    public void publishEvent(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!event.getCreateur().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Vous n'avez pas la permission de publier cet événement");
        }

        // Vérification : toutes les infos requises
        if (event.getTitre() == null ||
                event.getDateDebut() == null ||
                event.getDateFin() == null ||
                event.getVille() == null ||
                event.getLieu() == null ||
                event.getPrix() < 0 ||
                event.getCapaciteMax() <= 0) {
            throw new IllegalArgumentException("Impossible de publier : informations manquantes");
        }

        event.setStatus(EventStatus.PUBLIE);
        eventRepository.save(event);
    }

    /* --------------------- ANNULATION ------------------------ */

    public void cancelEvent(Long eventId, Long userId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!event.getCreateur().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Vous n'avez pas la permission d'annuler cet événement");
        }

        // TODO : gestion des remboursements / notifications quand les réservations seront complètes
        event.setStatus(EventStatus.ANNULE);
        eventRepository.save(event);
    }

    /* --------------------- SUPPRESSION ------------------------ */

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        if (!event.getReservations().isEmpty()) {
            throw new IllegalArgumentException("Impossible de supprimer : des réservations existent");
        }

        eventRepository.delete(event);
    }

    /* --------------------- RECHERCHE AVEC FILTRES ------------------------ */

    public List<Event> searchEvents(
            String ville,
            Category categorie,
            LocalDateTime start,
            LocalDateTime end,
            Double prixMax
    ) {
        return eventRepository.findAll().stream()
                .filter(e -> ville == null || e.getVille().equalsIgnoreCase(ville))
                .filter(e -> categorie == null || e.getCategorie() == categorie)
                .filter(e -> start == null || e.getDateDebut().isAfter(start))
                .filter(e -> end == null || e.getDateDebut().isBefore(end))
                .filter(e -> prixMax == null || e.getPrix() <= prixMax)
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .collect(Collectors.toList());
    }

    /* --------------------- PLACES DISPONIBLES ------------------------ */

    public int getAvailablePlaces(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        int reserved = event.getReservations().stream()
                .filter(r -> r.getStatus() != ReservationStatus.ANNULEE)
                .mapToInt(Reservation::getNbPlaces)
                .sum();

        return event.getCapaciteMax() - reserved;
    }

    /* --------------------- ÉVÉNEMENTS POPULAIRES ------------------------ */

    public List<Event> getPopularEvents(int limit) {
        return eventRepository.findAll().stream()
                .sorted((e1, e2) -> e2.getReservations().size() - e1.getReservations().size())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /* --------------------- STATISTIQUES ORGANISATEUR ------------------------ */

    public long countEventsByOrganizer(Long userId) {
        return eventRepository.findByCreateurId(userId).size();
    }

    /* --------------------- MARQUER LES ÉVÉNEMENTS TERMINÉS ------------------------ */

    public void updateFinishedEvents() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> events = eventRepository.findAll().stream()
                .filter(e -> e.getDateFin().isBefore(now))
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .collect(Collectors.toList());

        events.forEach(e -> e.setStatus(EventStatus.TERMINE));
        eventRepository.saveAll(events);
    }

    /* --------------------- UTILITAIRES ------------------------ */

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }
}
