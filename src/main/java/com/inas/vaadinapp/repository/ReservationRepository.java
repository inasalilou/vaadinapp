package com.inas.vaadinapp.repository;

import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByCodeReservation(String codeReservation);

    Optional<Reservation> findByCodeReservation(String codeReservation);

    List<Reservation> findByClientId(Long userId);

    List<Reservation> findByEventId(Long eventId);

    List<Reservation> findByStatus(ReservationStatus status);

    // Trouver les réservations d'un événement avec un statut donné
    List<Reservation> findByEventIdAndStatus(Long eventId, ReservationStatus status);

    // Calculer le nombre total de places réservées pour un événement
    @Query("SELECT COALESCE(SUM(r.nbPlaces), 0) FROM Reservation r WHERE r.event.id = :eventId AND r.status IN :statuses")
    Integer sumPlacesByEventIdAndStatusIn(@Param("eventId") Long eventId, @Param("statuses") List<ReservationStatus> statuses);

    // Trouver les réservations entre deux dates
    List<Reservation> findByDateReservationBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Trouver les réservations confirmées d'un utilisateur
    List<Reservation> findByClientIdAndStatus(Long userId, ReservationStatus status);

    // Calculer le montant total des réservations par utilisateur
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r WHERE r.client.id = :userId AND r.status IN :statuses")
    Double sumMontantTotalByClientIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<ReservationStatus> statuses);

    // Compter les réservations par statut pour un événement
    long countByEventIdAndStatus(Long eventId, ReservationStatus status);

    // Trouver les réservations récentes d'un utilisateur (derniers 30 jours)
    @Query("SELECT r FROM Reservation r WHERE r.client.id = :userId AND r.dateReservation >= :since ORDER BY r.dateReservation DESC")
    List<Reservation> findRecentReservationsByClientId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Vérifier si un utilisateur a déjà réservé un événement spécifique
    boolean existsByClientIdAndEventId(Long clientId, Long eventId);
}
