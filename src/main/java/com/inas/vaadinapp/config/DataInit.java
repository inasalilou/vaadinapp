package com.inas.vaadinapp.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.ReservationRepository;
import com.inas.vaadinapp.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataInit {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(EventRepository eventRepository, UserRepository userRepository, 
                    ReservationRepository reservationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        // Ne charger les données que si la base est vide
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("🚀 Initialisation des données de test...");

        // ========== CRÉATION DES UTILISATEURS (5 minimum) ==========
        User admin = createUser("Admin", "Principal", "admin@event.ma", "admin123", Role.ADMIN, "0612345678");
        User org1 = createUser("Bennani", "Karim", "organizer1@event.ma", "org123", Role.ORGANIZER, "0612345679");
        User org2 = createUser("Alami", "Fatima", "organizer2@event.ma", "org123", Role.ORGANIZER, "0612345680");
        User client1 = createUser("Idrissi", "Mohamed", "client1@event.ma", "client123", Role.CLIENT, "0612345681");
        User client2 = createUser("Ziani", "Amina", "client2@event.ma", "client123", Role.CLIENT, "0612345682");

        System.out.println("✅ 5 utilisateurs créés");

        // ========== CRÉATION DES ÉVÉNEMENTS (15 minimum) ==========

        // --- 3 CONCERTS ---
        Event concert1 = createEvent(
            "Concert de Jazz Fusion",
            "Soirée jazz avec les meilleurs artistes marocains",
            Category.CONCERT,
            "Casablanca",
            "Théâtre Mohammed V",
            500,
            200.0,
            LocalDateTime.now().plusDays(10),
            LocalDateTime.now().plusDays(10).plusHours(3),
            org1,
            EventStatus.PUBLIE
        );

        Event concert2 = createEvent(
            "Festival Gnaoua Rock",
            "Fusion entre musique gnaoua et rock moderne",
            Category.CONCERT,
            "Marrakech",
            "Palais Badi",
            1000,
            300.0,
            LocalDateTime.now().plusDays(20),
            LocalDateTime.now().plusDays(20).plusHours(4),
            org2,
            EventStatus.PUBLIE
        );

        Event concert3 = createEvent(
            "Nuit de la Pop Marocaine",
            "Les plus grands tubes pop du moment",
            Category.CONCERT,
            "Rabat",
            "Salle OLM Souissi",
            300,
            150.0,
            LocalDateTime.now().plusDays(5),
            LocalDateTime.now().plusDays(5).plusHours(3),
            org1,
            EventStatus.BROUILLON
        );

        // --- 3 CONFÉRENCES ---
        Event conf1 = createEvent(
            "Tech Summit Morocco 2026",
            "Conférence sur les nouvelles technologies et IA",
            Category.CONFERENCE,
            "Casablanca",
            "Hyatt Regency",
            800,
            500.0,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now().plusDays(30).plusHours(6),
            org1,
            EventStatus.PUBLIE
        );

        Event conf2 = createEvent(
            "Forum Entrepreneuriat Jeunesse",
            "Conférence sur l'entrepreneuriat et l'innovation",
            Category.CONFERENCE,
            "Rabat",
            "Centre de Conférences Mohammed VI",
            600,
            250.0,
            LocalDateTime.now().plusDays(40),
            LocalDateTime.now().plusDays(40).plusHours(5),
            org2,
            EventStatus.BROUILLON
        );

        Event conf3 = createEvent(
            "Sommet Développement Durable",
            "Conférence environnementale internationale",
            Category.CONFERENCE,
            "Marrakech",
            "Palais des Congrès",
            1200,
            400.0,
            LocalDateTime.now().plusDays(50),
            LocalDateTime.now().plusDays(50).plusHours(8),
            org1,
            EventStatus.PUBLIE
        );

        // --- 3 SPORTS ---
        Event sport1 = createEvent(
            "Match WAC vs Raja",
            "Derby casablancais - Botola Pro",
            Category.SPORT,
            "Casablanca",
            "Stade Mohammed V",
            45000,
            100.0,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(7).plusHours(2),
            org2,
            EventStatus.PUBLIE
        );

        Event sport2 = createEvent(
            "Marathon International de Rabat",
            "Course de 42km à travers la capitale",
            Category.SPORT,
            "Rabat",
            "Avenue Mohammed V",
            2000,
            50.0,
            LocalDateTime.now().plusDays(60),
            LocalDateTime.now().plusDays(60).plusHours(5),
            org1,
            EventStatus.PUBLIE
        );

        Event sport3 = createEvent(
            "Tournoi de Tennis ATP Marrakech",
            "Tournoi ATP 250",
            Category.SPORT,
            "Marrakech",
            "Royal Tennis Club",
            5000,
            350.0,
            LocalDateTime.now().plusDays(35),
            LocalDateTime.now().plusDays(35).plusHours(6),
            org2,
            EventStatus.ANNULE
        );

        // --- 3 FESTIVALS ---
        Event festival1 = createEvent(
            "Festival Mawazine",
            "Le plus grand festival de musique du Maroc",
            Category.FESTIVAL,
            "Rabat",
            "Stade Moulay Abdellah",
            30000,
            0.0,
            LocalDateTime.now().plusDays(45),
            LocalDateTime.now().plusDays(52),
            org1,
            EventStatus.PUBLIE
        );

        Event festival2 = createEvent(
            "Festival du Film de Marrakech",
            "Cinéma international et avant-premières",
            Category.FESTIVAL,
            "Marrakech",
            "Palais des Congrès",
            2000,
            200.0,
            LocalDateTime.now().plusDays(70),
            LocalDateTime.now().plusDays(77),
            org2,
            EventStatus.BROUILLON
        );

        Event festival3 = createEvent(
            "Tanjazz Festival",
            "Festival de jazz international",
            Category.FESTIVAL,
            "Tanger",
            "Grand Socco",
            5000,
            100.0,
            LocalDateTime.now().plusDays(90),
            LocalDateTime.now().plusDays(93),
            org1,
            EventStatus.PUBLIE
        );

        // --- 3 AUTRES ---
        Event autre1 = createEvent(
            "Salon du Livre de Tanger",
            "Rencontres littéraires et dédicaces",
            Category.AUTRE,
            "Tanger",
            "Palais des Institutions Italiennes",
            1000,
            80.0,
            LocalDateTime.now().plusDays(12),
            LocalDateTime.now().plusDays(12).plusHours(8),
            org1,
            EventStatus.PUBLIE
        );

        Event autre2 = createEvent(
            "Festival Gastronomique Fès",
            "Découverte de la cuisine traditionnelle fassi",
            Category.AUTRE,
            "Fès",
            "Palais Jamaï",
            400,
            200.0,
            LocalDateTime.now().plusDays(18),
            LocalDateTime.now().plusDays(18).plusHours(6),
            org2,
            EventStatus.BROUILLON
        );

        Event autre3 = createEvent(
            "Exposition Art Contemporain",
            "Œuvres d'artistes marocains et internationaux",
            Category.AUTRE,
            "Casablanca",
            "Villa des Arts",
            500,
            60.0,
            LocalDateTime.now().plusDays(15),
            LocalDateTime.now().plusDays(15).plusHours(6),
            org1,
            EventStatus.TERMINE
        );

        System.out.println("✅ 15 événements créés");

        // ========== CRÉATION DES RÉSERVATIONS (20 minimum) ==========

        // Réservations pour Concert Jazz
        createReservation(client1, concert1, 2, 200.0, ReservationStatus.CONFIRMEE, "Places VIP svp");
        createReservation(client2, concert1, 3, 200.0, ReservationStatus.CONFIRMEE, null);

        // Réservations pour Festival Gnaoua
        createReservation(client1, concert2, 4, 300.0, ReservationStatus.EN_ATTENTE, "Groupe de 4 personnes");
        createReservation(client2, concert2, 2, 300.0, ReservationStatus.CONFIRMEE, null);

        // Réservations pour Concert Pop
        createReservation(client1, concert3, 1, 150.0, ReservationStatus.ANNULEE, "Empêchement de dernière minute");

        // Réservations pour Tech Summit
        createReservation(client2, conf1, 1, 500.0, ReservationStatus.CONFIRMEE, "Pass journée complète");
        createReservation(client1, conf1, 1, 500.0, ReservationStatus.EN_ATTENTE, null);

        // Réservations pour Sommet Développement
        createReservation(client2, conf3, 2, 400.0, ReservationStatus.CONFIRMEE, "Conférenciers");

        // Réservations pour Match WAC vs Raja
        createReservation(client1, sport1, 4, 100.0, ReservationStatus.CONFIRMEE, "Tribune VIP");
        createReservation(client2, sport1, 6, 100.0, ReservationStatus.CONFIRMEE, null);

        // Réservations pour Marathon
        createReservation(client1, sport2, 1, 50.0, ReservationStatus.EN_ATTENTE, "Coureur expérimenté");
        createReservation(client2, sport2, 1, 50.0, ReservationStatus.CONFIRMEE, null);

        // Réservations pour Tournoi Tennis (annulé)
        createReservation(client1, sport3, 2, 350.0, ReservationStatus.ANNULEE, "Tournoi reporté");

        // Réservations pour Festival Mawazine
        createReservation(client2, festival1, 5, 1000.0, ReservationStatus.CONFIRMEE, "Entrée vip");
        createReservation(client1, festival1, 3, 1000.0, ReservationStatus.CONFIRMEE, null);

        // Réservations pour Tanjazz
        createReservation(client1, festival3, 2, 100.0, ReservationStatus.EN_ATTENTE, null);

        // Réservations pour Salon du Livre
        createReservation(client2, autre1, 3, 80.0, ReservationStatus.CONFIRMEE, null);
        createReservation(client1, autre1, 2, 80.0, ReservationStatus.EN_ATTENTE, "Passionnés de lecture");

        // Réservation pour Expo Art
        createReservation(client2, autre3, 1, 60.0, ReservationStatus.CONFIRMEE, "Collectionneur");

        // Réservations supplémentaires pour atteindre 20
        createReservation(client1, concert1, 1, 200.0, ReservationStatus.CONFIRMEE, null);
        createReservation(client2, sport1, 2, 100.0, ReservationStatus.EN_ATTENTE, null);

        System.out.println("✅ 20 réservations créées");
        System.out.println("🎉 Initialisation terminée avec succès !");
        System.out.println("\n📋 Comptes de test :");
        System.out.println("   ADMIN: admin@event.ma / admin123");
        System.out.println("   ORGANIZER 1: organizer1@event.ma / org123");
        System.out.println("   ORGANIZER 2: organizer2@event.ma / org123");
        System.out.println("   CLIENT 1: client1@event.ma / client123");
        System.out.println("   CLIENT 2: client2@event.ma / client123");
    }

    private User createUser(String nom, String prenom, String email, String password, Role role, String telephone) {
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDateInscription(LocalDateTime.now());
        user.setActif(true);
        user.setTelephone(telephone);
        return userRepository.save(user);
    }

    private Event createEvent(String titre, String description, Category categorie, String ville, String lieu,
                            int capaciteMax, double prixUnitaire, LocalDateTime dateDebut, LocalDateTime dateFin,
                            User organisateur, EventStatus status) {
        Event event = new Event();
        event.setTitre(titre);
        event.setDescription(description);
        event.setCategorie(categorie);
        event.setVille(ville);
        event.setLieu(lieu);
        event.setCapaciteMax(capaciteMax);
        event.setPrixUnitaire(prixUnitaire);
        event.setDateDebut(dateDebut);
        event.setDateFin(dateFin);
        event.setOrganisateur(organisateur);
        event.setStatus(status);
        event.setDateCreation(LocalDateTime.now());
        event.setDateModification(LocalDateTime.now());
        return eventRepository.save(event);
    }

    private void createReservation(User client, Event event, int nbPlaces, double prixUnitaire,
                                  ReservationStatus status, String commentaire) {
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setEvent(event);
        reservation.setNbPlaces(nbPlaces);
        reservation.setPrixUnitaire(prixUnitaire);
        reservation.setMontantTotal(nbPlaces * prixUnitaire);
        reservation.setDateReservation(LocalDateTime.now().minusDays((long)(Math.random() * 10)));
        reservation.setCodeReservation(generateReservationCode());
        reservation.setStatus(status);
        reservation.setCommentaire(commentaire);
        reservationRepository.save(reservation);
    }

    private String generateReservationCode() {
        // UUID réduit pour minimiser le risque de collision (code unique en base)
        return "EVT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}