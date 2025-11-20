package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.ReservationRepository;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    /* ----------- Génération du code de réservation unique ----------- */

    private String generateUniqueCode() {
        String code;
        do {
            String digits = String.format("%05d",
                    ThreadLocalRandom.current().nextInt(0, 100000));
            code = "EVT-" + digits;
        } while (reservationRepository.existsByCodeReservation(code));
        return code;
    }

    /* ----------- Création d'une réservation ----------- */

    public Reservation createReservation(Long eventId, Long userId, int nbPlaces, String commentaire) {

        if (nbPlaces < 1 || nbPlaces > 10) {
            throw new IllegalArgumentException("Une réservation doit être entre 1 et 10 places");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable"));

        if (event.getStatus() != EventStatus.PUBLIE) {
            throw new IllegalArgumentException("L'événement doit être publié pour pouvoir réserver");
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getDateFin().isBefore(now)) {
            throw new IllegalArgumentException("L'événement est déjà terminé");
        }

        int available = eventService.getAvailablePlaces(eventId);
        if (nbPlaces > available) {
            throw new IllegalArgumentException("Pas assez de places disponibles");
        }

        User client = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        double prixUnitaire = event.getPrix();
        double montantTotal = nbPlaces * prixUnitaire;

        Reservation reservation = new Reservation();
        reservation.setEvent(event);
        reservation.setClient(client);
        reservation.setNbPlaces(nbPlaces);
        reservation.setPrixUnitaire(prixUnitaire);
        reservation.setMontantTotal(montantTotal);
        reservation.setDateReservation(now);
        reservation.setCodeReservation(generateUniqueCode());
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        reservation.setCommentaire(commentaire);

        return reservationRepository.save(reservation);
    }

    /* ----------- Confirmation ----------- */

    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        reservation.setStatus(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    /* ----------- Annulation (avec délai 48h) ----------- */

    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));

        Event event = reservation.getEvent();
        LocalDateTime now = LocalDateTime.now();

        // Règle : annulation possible jusqu'à 48h avant l'événement
        if (ChronoUnit.HOURS.between(now, event.getDateDebut()) < 48) {
            throw new IllegalArgumentException("La réservation ne peut plus être annulée (délai de 48h dépassé)");
        }

        reservation.setStatus(ReservationStatus.ANNULEE);
        return reservationRepository.save(reservation);
    }

    /* ----------- Récupération des réservations d'un utilisateur ----------- */

    public List<Reservation> getReservationsForUser(Long userId, ReservationStatus status) {
        return reservationRepository.findByClientId(userId).stream()
                .filter(r -> status == null || r.getStatus() == status)
                .collect(Collectors.toList());
    }

    /* ----------- Vérifier une réservation par code ----------- */

    public Optional<Reservation> findByCode(String codeReservation) {
        return reservationRepository.findByCodeReservation(codeReservation);
    }

    /* ----------- Statistiques de réservation ----------- */

    public double getTotalAmountForUser(Long userId) {
        return reservationRepository.findByClientId(userId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
    }

    public long countReservationsForUser(Long userId) {
        return reservationRepository.findByClientId(userId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .count();
    }

    public long countReservationsForEvent(Long eventId) {
        return reservationRepository.findByEventId(eventId).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .count();
    }
}
