package com.inas.vaadinapp.config;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInit {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public DataInit(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {

        // Empêche la duplication à chaque redémarrage
        if (eventRepository.count() > 0) {
            return;
        }

        // --------- Création d’un utilisateur ORGANIZER ----------
        User org = new User();
        org.setNom("Admin");
        org.setPrenom("Event");
        org.setEmail("organizer@test.com");
        org.setPassword("password123");
        org.setRole(Role.ORGANIZER);
        userRepository.save(org);

        // --------- ÉVÉNEMENT 1 ----------
        Event e1 = new Event();
        e1.setTitre("Concert de Jazz");
        e1.setDescription("Un magnifique concert de jazz.");
        e1.setCategorie(Category.CONCERT);
        e1.setVille("Paris");
        e1.setLieu("Salle Olympia");
        e1.setPrix(35.0);
        e1.setCapaciteMax(200);
        e1.setDateDebut(LocalDateTime.now().plusDays(5));
        e1.setDateFin(LocalDateTime.now().plusDays(5).plusHours(2));
        e1.setCreateur(org);
        e1.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e1);

        // --------- ÉVÉNEMENT 2 ----------
        Event e2 = new Event();
        e2.setTitre("Conférence IA");
        e2.setDescription("Exploration du futur de l'intelligence artificielle.");
        e2.setCategorie(Category.CONFERENCE);
        e2.setVille("Lyon");
        e2.setLieu("Centre des Congrès");
        e2.setPrix(60.0);
        e2.setCapaciteMax(150);
        e2.setDateDebut(LocalDateTime.now().plusDays(10));
        e2.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        e2.setCreateur(org);
        e2.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e2);

        // --------- ÉVÉNEMENT 3 ----------
        Event e3 = new Event();
        e3.setTitre("Match de foot");
        e3.setDescription("Match PSG vs OM au Vélodrome !");
        e3.setCategorie(Category.SPORT);
        e3.setVille("Marseille");
        e3.setLieu("Stade Vélodrome");
        e3.setPrix(80.0);
        e3.setCapaciteMax(50000);
        e3.setDateDebut(LocalDateTime.now().plusDays(2));
        e3.setDateFin(LocalDateTime.now().plusDays(2).plusHours(2));
        e3.setCreateur(org);
        e3.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e3);
    }
}
