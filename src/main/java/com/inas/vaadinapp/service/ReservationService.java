package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.ReservationRepository;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public ReservationService(ReservationRepository reservationRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository,
                              EventService eventService) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    /* ================== CREATION ================== */

    @Transactional
    public Reservation createReservation(Long eventId,
                                         Long userId,
                                         int nbPlaces,
                                         String commentaire) {

        if (nbPlaces <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif.");
        }
        if (nbPlaces > 10) {
            throw new IllegalArgumentException("Une réservation ne peut pas dépasser 10 places.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        if (event.getStatus() != EventStatus.PUBLIE) {
            throw new IllegalArgumentException("L'événement doit être publié pour être réservé.");
        }

        if (event.getDateFin().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'événement est déjà terminé.");
        }

        int available = eventService.getAvailablePlaces(eventId);
        if (nbPlaces > available) {
            throw new IllegalArgumentException("Pas assez de places disponibles. Il reste : " + available);
        }

        Reservation reservation = new Reservation();
        reservation.setEvent(event);
        reservation.setClient(user);
        reservation.setNbPlaces(nbPlaces);
        reservation.setPrixUnitaire(event.getPrix());
        reservation.setMontantTotal(nbPlaces * event.getPrix());
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        reservation.setCommentaire(commentaire);

        String code = generateUniqueCode();
        reservation.setCodeReservation(code);

        return reservationRepository.save(reservation);
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        do {
            int n = random.nextInt(100000); // 0..99999
            code = String.format("EVT-%05d", n);
        } while (reservationRepository.existsByCodeReservation(code));
        return code;
    }

    /* ================== LECTURE ================== */

    public List<Reservation> findByClient(Long userId) {
        return reservationRepository.findByClientId(userId);
    }

    public Optional<Reservation> findByCode(String code) {
        return reservationRepository.findByCodeReservation(code);
    }

    /* ================== ANNULATION ================== */

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable."));

        if (!r.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous ne pouvez annuler que vos propres réservations.");
        }

        LocalDateTime now = LocalDateTime.now();
        Duration d = Duration.between(now, r.getEvent().getDateDebut());
        if (d.toHours() < 48) {
            throw new IllegalArgumentException(
                    "Les réservations peuvent être annulées jusqu'à 48h avant l'événement.");
        }

        r.setStatus(ReservationStatus.ANNULEE);
        reservationRepository.save(r);
        // logique de remboursement éventuelle à ajouter plus tard
    }

    /* ================== STATISTIQUES SIMPLES ================== */

    public long countReservationsByUser(Long userId) {
        return reservationRepository.findByClientId(userId).size();
    }

    public double totalSpentByUser(Long userId) {
        return reservationRepository.findByClientId(userId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE
                        || r.getStatus() == ReservationStatus.EN_ATTENTE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
    }
}
