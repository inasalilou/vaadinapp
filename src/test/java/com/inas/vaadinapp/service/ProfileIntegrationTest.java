package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.repository.EventRepository;
import com.inas.vaadinapp.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProfileIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Créer un utilisateur pour les tests
        testUser = new User();
        testUser.setNom("Profile");
        testUser.setPrenom("Test");
        testUser.setEmail("profile.test@test.com");
        testUser.setPassword("password");
        testUser.setRole(Role.CLIENT);
        testUser.setActif(true);
        userService.register(testUser);
    }

    @Test
    void testBasicProfileFunctionality() {
        System.out.println("🧪 Test des fonctionnalités de base du profil");

        // === TEST 1: MISE À JOUR DU PROFIL ===
        System.out.println("📝 Test de mise à jour du profil");

        User updatedUser = new User();
        updatedUser.setNom("UpdatedName");
        updatedUser.setPrenom("UpdatedFirstName");
        updatedUser.setEmail("updated.email@test.com");
        updatedUser.setTelephone("06 98 76 54 32");

        User savedUser = userService.updateProfile(testUser.getId(), updatedUser);

        assertEquals("UpdatedName", savedUser.getNom());
        assertEquals("UpdatedFirstName", savedUser.getPrenom());
        assertEquals("updated.email@test.com", savedUser.getEmail());
        assertEquals("06 98 76 54 32", savedUser.getTelephone());
        System.out.println("✅ Mise à jour du profil réussie");

        // === TEST 2: CHANGEMENT DE MOT DE PASSE ===
        System.out.println("🔒 Test de changement de mot de passe");

        userService.changePassword(testUser.getId(), "password", "newSecurePassword123");
        System.out.println("✅ Changement de mot de passe réussi");

        // === TEST 3: STATISTIQUES UTILISATEUR ===
        System.out.println("📊 Test des statistiques utilisateur");

        UserService.UserStatistics stats = userService.getUserStatistics(testUser.getId());

        assertNotNull(stats);
        assertEquals(0, stats.getEventsCreated()); // Pas d'événements créés
        assertEquals(0, stats.getReservationsCount()); // Pas de réservations
        assertEquals(0.0, stats.getTotalSpent()); // Pas de dépenses

        System.out.println("📈 Statistiques utilisateur:");
        System.out.println("   - Événements créés: " + stats.getEventsCreated());
        System.out.println("   - Réservations: " + stats.getReservationsCount());
        System.out.println("   - Dépenses totales: " + String.format("%.2f €", stats.getTotalSpent()));

        // === TEST 4: VALIDATION MOT DE PASSE ===
        System.out.println("🔐 Test de validation du mot de passe");

        // Tester avec l'ancien mot de passe qui n'est plus valide
        Exception passwordException = assertThrows(IllegalArgumentException.class, () ->
            userService.changePassword(testUser.getId(), "oldpassword", "newpassword123"));
        assertTrue(passwordException.getMessage().contains("Ancien mot de passe incorrect"));
        System.out.println("✅ Validation de l'ancien mot de passe fonctionnelle");

        // === TEST 5: DÉSACTIVATION DU COMPTE ===
        System.out.println("💀 Test de désactivation du compte");

        User deactivatedUser = userService.toggleAccountStatus(testUser.getId(), false);
        assertFalse(deactivatedUser.getActif());
        System.out.println("✅ Désactivation du compte réussie");

        User reactivatedUser = userService.toggleAccountStatus(testUser.getId(), true);
        assertTrue(reactivatedUser.getActif());
        System.out.println("✅ Réactivation du compte réussie");

        System.out.println("\n🎉 TOUTES LES FONCTIONNALITÉS DE BASE DU PROFIL SONT OPÉRATIONNELLES !");
    }

    @Test
    void testProfileWithReservations() {
        System.out.println("🎫 Test du profil avec réservations");

        // Créer un événement avec les données existantes
        var existingEvents = eventService.findAll();
        assertFalse(existingEvents.isEmpty(), "Il devrait y avoir des événements existants");

        Event event = existingEvents.get(0);

        // Créer une réservation
        Reservation reservation = reservationService.createReservation(
            event.getId(), testUser.getId(), 2, "Test reservation"
        );

        assertNotNull(reservation);
        assertEquals(2, reservation.getNbPlaces());
        assertEquals(event.getPrixUnitaire() * 2, reservation.getMontantTotal());

        // Vérifier les statistiques mises à jour
        UserService.UserStatistics stats = userService.getUserStatistics(testUser.getId());

        assertEquals(0, stats.getEventsCreated()); // Pas d'événements créés par ce user
        assertEquals(1, stats.getReservationsCount()); // 1 réservation
        assertEquals(event.getPrixUnitaire() * 2, stats.getTotalSpent()); // Coût de la réservation

        System.out.println("📊 Statistiques après réservation:");
        System.out.println("   - Réservations: " + stats.getReservationsCount());
        System.out.println("   - Dépenses: " + String.format("%.2f €", stats.getTotalSpent()));
        System.out.println("✅ Fonctionnalités avec réservations validées");
    }
}
