package com.inas.vaadinapp.config;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInit {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {

        // --------- Création/Mise à jour des utilisateurs de test ----------
        // On vérifie et crée/met à jour les utilisateurs même si des événements existent déjà
        
        // Admin user
        User admin = userRepository.findByEmail("admin@test.com").orElse(new User());
        admin.setNom("Admin");
        admin.setPrenom("Super");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("admin123")); // Toujours re-hasher pour garantir la bonne valeur
        admin.setRole(Role.ADMIN);
        if (admin.getDateInscription() == null) {
            admin.setDateInscription(LocalDateTime.now());
        }
        admin.setActif(true);
        userRepository.save(admin);

        // Organizer user
        User org = userRepository.findByEmail("organizer@test.com").orElse(new User());
        org.setNom("Organizer");
        org.setPrenom("Event");
        org.setEmail("organizer@test.com");
        org.setPassword(passwordEncoder.encode("password123")); // Toujours re-hasher
        org.setRole(Role.ORGANIZER);
        if (org.getDateInscription() == null) {
            org.setDateInscription(LocalDateTime.now());
        }
        org.setActif(true);
        userRepository.save(org);

        // Client user
        User client = userRepository.findByEmail("client@test.com").orElse(new User());
        client.setNom("Client");
        client.setPrenom("Test");
        client.setEmail("client@test.com");
        client.setPassword(passwordEncoder.encode("password123")); // Toujours re-hasher
        client.setRole(Role.CLIENT);
        if (client.getDateInscription() == null) {
            client.setDateInscription(LocalDateTime.now());
        }
        client.setActif(true);
        userRepository.save(client);

        // --------- Création des événements (seulement si aucun n'existe) ----------
        if (eventRepository.count() > 0) {
            return;
        }

        // --------- ÉVÉNEMENT 1 ----------
        Event e1 = new Event();
        e1.setTitre("Concert de Jazz");
        e1.setDescription("Un magnifique concert de jazz.");
        e1.setCategorie(Category.CONCERT);
        e1.setVille("Paris");
        e1.setLieu("Salle Olympia");
        e1.setPrixUnitaire(35.0);
        e1.setCapaciteMax(200);
        e1.setDateDebut(LocalDateTime.now().plusDays(5));
        e1.setDateFin(LocalDateTime.now().plusDays(5).plusHours(2));
        e1.setOrganisateur(org);
        e1.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e1);

        // --------- ÉVÉNEMENT 2 ----------
        Event e2 = new Event();
        e2.setTitre("Conférence IA");
        e2.setDescription("Exploration du futur de l'intelligence artificielle.");
        e2.setCategorie(Category.CONFERENCE);
        e2.setVille("Lyon");
        e2.setLieu("Centre des Congrès");
        e2.setPrixUnitaire(60.0);
        e2.setCapaciteMax(150);
        e2.setDateDebut(LocalDateTime.now().plusDays(10));
        e2.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        e2.setOrganisateur(org);
        e2.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e2);

        // --------- ÉVÉNEMENT 3 ----------
        Event e3 = new Event();
        e3.setTitre("Match de foot");
        e3.setDescription("Match PSG vs OM au Vélodrome !");
        e3.setCategorie(Category.SPORT);
        e3.setVille("Marseille");
        e3.setLieu("Stade Vélodrome");
        e3.setPrixUnitaire(80.0);
        e3.setCapaciteMax(50000);
        e3.setDateDebut(LocalDateTime.now().plusDays(2));
        e3.setDateFin(LocalDateTime.now().plusDays(2).plusHours(2));
        e3.setOrganisateur(org);
        e3.setStatus(EventStatus.PUBLIE);
        eventRepository.save(e3);
    }
}
