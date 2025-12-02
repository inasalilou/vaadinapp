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
import java.util.Arrays;
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
        reservation.setPrixUnitaire(event.getPrixUnitaire());
        reservation.setMontantTotal(nbPlaces * event.getPrixUnitaire());
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

    /* ================== CONFIRMATION ================== */

    @Transactional
    public Reservation confirmReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable."));

        if (!reservation.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous ne pouvez confirmer que vos propres réservations.");
        }
        if (reservation.getStatus() != ReservationStatus.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les réservations en attente peuvent être confirmées.");
        }
        if (reservation.getEvent().getStatus() != EventStatus.PUBLIE || reservation.getEvent().getDateFin().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'événement n'est plus valide pour confirmation.");
        }

        // Re-vérifier la disponibilité des places au moment de la confirmation
        int available = eventService.getAvailablePlaces(reservation.getEvent().getId());
        if (reservation.getNbPlaces() > available) {
            throw new IllegalArgumentException("Pas assez de places disponibles pour confirmer la réservation. Il reste : " + available);
        }

        reservation.setStatus(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    /* ================== RECAPITULATIF ================== */

    public ReservationSummary generateReservationSummary(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable."));

        if (!reservation.getClient().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous ne pouvez voir le récapitulatif que de vos propres réservations.");
        }

        return new ReservationSummary(reservation);
    }

    /* ================== STATISTIQUES AVANCEES ================== */

    public ReservationStatistics getReservationStatistics() {
        long totalReservations = reservationRepository.count();
        long pending = reservationRepository.findByStatus(ReservationStatus.EN_ATTENTE).size();
        long confirmed = reservationRepository.findByStatus(ReservationStatus.CONFIRMEE).size();
        long cancelled = reservationRepository.findByStatus(ReservationStatus.ANNULEE).size();

        List<ReservationStatus> activeStatuses = Arrays.asList(ReservationStatus.EN_ATTENTE, ReservationStatus.CONFIRMEE);
        Double totalRevenue = reservationRepository.sumMontantTotalByClientIdAndStatusIn(null, activeStatuses); // Global sum
        Integer totalPlacesReserved = reservationRepository.sumPlacesByEventIdAndStatusIn(null, activeStatuses); // Global sum

        // Réservations du mois en cours
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
        long currentMonthReservations = reservationRepository.findByDateReservationBetween(startOfMonth, endOfMonth).size();

        return new ReservationStatistics(totalReservations, pending, confirmed, cancelled,
                totalRevenue != null ? totalRevenue : 0.0,
                totalPlacesReserved != null ? totalPlacesReserved : 0,
                currentMonthReservations);
    }

    /* ================== CLASSES INTERNES ================== */

    public static class ReservationSummary {
        private final Long reservationId;
        private final String eventTitle;
        private final LocalDateTime eventDate;
        private final String eventLocation;
        private final int nbPlaces;
        private final double prixUnitaire;
        private final double montantTotal;
        private final ReservationStatus status;
        private final String codeReservation;
        private final LocalDateTime dateReservation;

        public ReservationSummary(Reservation reservation) {
            this.reservationId = reservation.getId();
            this.eventTitle = reservation.getEvent().getTitre();
            this.eventDate = reservation.getEvent().getDateDebut();
            this.eventLocation = reservation.getEvent().getLieu() + ", " + reservation.getEvent().getVille();
            this.nbPlaces = reservation.getNbPlaces();
            this.prixUnitaire = reservation.getPrixUnitaire();
            this.montantTotal = reservation.getMontantTotal();
            this.status = reservation.getStatus();
            this.codeReservation = reservation.getCodeReservation();
            this.dateReservation = reservation.getDateReservation();
        }

        // Getters
        public Long getReservationId() { return reservationId; }
        public String getEventTitle() { return eventTitle; }
        public LocalDateTime getEventDate() { return eventDate; }
        public String getEventLocation() { return eventLocation; }
        public int getNbPlaces() { return nbPlaces; }
        public double getPrixUnitaire() { return prixUnitaire; }
        public double getMontantTotal() { return montantTotal; }
        public ReservationStatus getStatus() { return status; }
        public String getCodeReservation() { return codeReservation; }
        public LocalDateTime getDateReservation() { return dateReservation; }
    }

    public static class ReservationStatistics {
        private final long totalReservations;
        private final long pendingReservations;
        private final long confirmedReservations;
        private final long cancelledReservations;
        private final double totalRevenue;
        private final int totalPlacesReserved;
        private final long currentMonthReservations;

        public ReservationStatistics(long totalReservations, long pendingReservations,
                                   long confirmedReservations, long cancelledReservations,
                                   double totalRevenue, int totalPlacesReserved,
                                   long currentMonthReservations) {
            this.totalReservations = totalReservations;
            this.pendingReservations = pendingReservations;
            this.confirmedReservations = confirmedReservations;
            this.cancelledReservations = cancelledReservations;
            this.totalRevenue = totalRevenue;
            this.totalPlacesReserved = totalPlacesReserved;
            this.currentMonthReservations = currentMonthReservations;
        }

        // Getters
        public long getTotalReservations() { return totalReservations; }
        public long getPendingReservations() { return pendingReservations; }
        public long getConfirmedReservations() { return confirmedReservations; }
        public long getCancelledReservations() { return cancelledReservations; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getTotalPlacesReserved() { return totalPlacesReserved; }
        public long getCurrentMonthReservations() { return currentMonthReservations; }
    }
}
