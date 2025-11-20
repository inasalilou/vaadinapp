package com.inas.vaadinapp.repository;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCategorie(Category categorie);

    List<Event> findByVilleIgnoreCase(String ville);

    List<Event> findByDateDebutBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByCreateurId(Long userId);
}
