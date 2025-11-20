package com.inas.vaadinapp.repository;

import com.inas.vaadinapp.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByCodeReservation(String codeReservation);

    Optional<Reservation> findByCodeReservation(String codeReservation);

    List<Reservation> findByClientId(Long userId);

    List<Reservation> findByEventId(Long eventId);
}
