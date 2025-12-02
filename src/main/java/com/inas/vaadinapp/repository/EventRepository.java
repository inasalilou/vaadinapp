package com.inas.vaadinapp.repository;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCategorie(Category categorie);

    List<Event> findByVilleIgnoreCase(String ville);

    List<Event> findByDateDebutBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByOrganisateurId(Long userId);

    // Trouver les événements publiés entre deux dates
    List<Event> findByStatusAndDateDebutBetween(EventStatus status, LocalDateTime start, LocalDateTime end);

    // Trouver les événements d'un organisateur avec un statut donné
    List<Event> findByOrganisateurIdAndStatus(Long organisateurId, EventStatus status);

    // Trouver les événements disponibles (publiés et non terminés)
    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.dateFin > :currentDate")
    List<Event> findAvailableEvents(@Param("status") EventStatus status, @Param("currentDate") LocalDateTime currentDate);

    // Compter le nombre d'événements par catégorie
    long countByCategorie(Category categorie);

    // Compter les événements par organisateur
    long countByOrganisateurId(Long organisateurId);

    // Trouver les événements par lieu ou ville
    List<Event> findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase(String lieu, String ville);

    // Rechercher les événements par titre (contenant un mot-clé)
    List<Event> findByTitreContainingIgnoreCase(String keyword);

    // Trouver les événements par plage de prix
    List<Event> findByPrixUnitaireBetween(double prixMin, double prixMax);
}
