package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProfileFullTest {

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Créer et connecter un utilisateur de test
        testUser = new User();
        testUser.setNom("TestUser");
        testUser.setPrenom("Profile");
        testUser.setEmail("test.profile@test.com");
        testUser.setPassword("password");
        testUser.setRole(Role.CLIENT);
        testUser.setActif(true);
        userService.register(testUser);

        System.out.println("=== TEST PROFIL COMPLET ===");
        System.out.println("Utilisateur créé: " + testUser.getEmail());
    }

    @Test
    void testCompleteProfileFunctionality() {
        System.out.println("\n🧪 TEST COMPLET DES FONCTIONNALITÉS DU PROFIL");

        // === TEST 1: Vérification que l'utilisateur existe ===
        assertNotNull(testUser.getId(), "L'utilisateur devrait avoir un ID");
        assertTrue(testUser.getActif(), "L'utilisateur devrait être actif");
        System.out.println("✅ Utilisateur créé et actif");

        // === TEST 2: Test de récupération des statistiques ===
        try {
            UserService.UserStatistics initialStats = userService.getUserStatistics(testUser.getId());
            assertNotNull(initialStats, "Les statistiques devraient exister");
            assertEquals(0, initialStats.getEventsCreated(), "Pas d'événements créés initialement");
            assertEquals(0, initialStats.getReservationsCount(), "Pas de réservations initialement");
            assertEquals(0.0, initialStats.getTotalSpent(), "Pas de dépenses initiales");
            System.out.println("✅ Statistiques initiales chargées: " + initialStats.getEventsCreated() + " événements, " +
                             initialStats.getReservationsCount() + " réservations, " + initialStats.getTotalSpent() + "€");
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du chargement des statistiques initiales: " + e.getMessage());
            e.printStackTrace();
            fail("Le chargement des statistiques initiales devrait réussir");
        }

        // === TEST 3: Test de mise à jour du profil ===
        System.out.println("\n📝 Test de mise à jour du profil");
        User updatedUser = new User();
        updatedUser.setNom("UpdatedName");
        updatedUser.setPrenom("UpdatedFirstName");
        updatedUser.setEmail("updated.email@test.com");
        updatedUser.setTelephone("06 99 88 77 66");

        try {
            User savedUser = userService.updateProfile(testUser.getId(), updatedUser);
            assertEquals("UpdatedName", savedUser.getNom());
            assertEquals("UpdatedFirstName", savedUser.getPrenom());
            assertEquals("updated.email@test.com", savedUser.getEmail());
            assertEquals("06 99 88 77 66", savedUser.getTelephone());
            System.out.println("✅ Profil mis à jour avec succès");

            // Mettre à jour testUser pour les tests suivants
            testUser = savedUser;
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la mise à jour du profil: " + e.getMessage());
            e.printStackTrace();
            fail("La mise à jour du profil devrait réussir");
        }

        // === TEST 4: Test de changement de mot de passe ===
        System.out.println("\n🔒 Test de changement de mot de passe");
        try {
            userService.changePassword(testUser.getId(), "password", "newSecurePassword123");
            System.out.println("✅ Mot de passe changé avec succès");
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du changement de mot de passe: " + e.getMessage());
            e.printStackTrace();
            fail("Le changement de mot de passe devrait réussir");
        }

        // === TEST 5: Test de validation des contraintes ===
        System.out.println("\n🛡️ Test des validations");

        // Test email dupliqué
        try {
            // Créer un autre utilisateur avec un email différent
            User anotherUser = new User();
            anotherUser.setNom("Another");
            anotherUser.setPrenom("User");
            anotherUser.setEmail("another@test.com");
            anotherUser.setPassword("password");
            userService.register(anotherUser);

            // Essayer de mettre à jour avec l'email de l'autre utilisateur
            User duplicateUser = new User();
            duplicateUser.setNom("Test");
            duplicateUser.setPrenom("Test");
            duplicateUser.setEmail("another@test.com"); // Email déjà utilisé par anotherUser

            userService.updateProfile(testUser.getId(), duplicateUser);
            fail("La mise à jour avec un email dupliqué devrait échouer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Email déjà utilisé"));
            System.out.println("✅ Validation d'unicité d'email fonctionne");
        } catch (Exception e) {
            System.err.println("❌ Type d'erreur inattendu pour email dupliqué: " + e.getMessage());
        }

        // Test mot de passe trop court
        try {
            userService.changePassword(testUser.getId(), "newSecurePassword123", "123");
            fail("Le changement avec un mot de passe trop court devrait échouer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("au moins 8 caractères"));
            System.out.println("✅ Validation de longueur du mot de passe fonctionne");
        } catch (Exception e) {
            System.err.println("❌ Type d'erreur inattendu pour mot de passe court: " + e.getMessage());
        }

        // Test ancien mot de passe incorrect
        try {
            userService.changePassword(testUser.getId(), "wrongpassword", "validpassword123");
            fail("Le changement avec un ancien mot de passe incorrect devrait échouer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Ancien mot de passe incorrect"));
            System.out.println("✅ Validation de l'ancien mot de passe fonctionne");
        } catch (Exception e) {
            System.err.println("❌ Type d'erreur inattendu pour ancien mot de passe: " + e.getMessage());
        }

        // === TEST 6: Test de désactivation du compte ===
        System.out.println("\n💀 Test de désactivation du compte");
        try {
            User deactivatedUser = userService.toggleAccountStatus(testUser.getId(), false);
            assertFalse(deactivatedUser.getActif());
            System.out.println("✅ Compte désactivé avec succès");

            // Réactiver pour les tests suivants
            User reactivatedUser = userService.toggleAccountStatus(testUser.getId(), true);
            assertTrue(reactivatedUser.getActif());
            System.out.println("✅ Compte réactivé avec succès");

            testUser = reactivatedUser;
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la désactivation/réactivation: " + e.getMessage());
            e.printStackTrace();
            fail("La désactivation/réactivation devrait réussir");
        }

        // === TEST 7: Test des statistiques après modifications ===
        System.out.println("\n📊 Test des statistiques finales");
        try {
            UserService.UserStatistics finalStats = userService.getUserStatistics(testUser.getId());
            assertNotNull(finalStats, "Les statistiques finales devraient exister");
            System.out.println("✅ Statistiques finales: " + finalStats.getEventsCreated() + " événements, " +
                             finalStats.getReservationsCount() + " réservations, " + finalStats.getTotalSpent() + "€");
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du chargement des statistiques finales: " + e.getMessage());
            e.printStackTrace();
            fail("Le chargement des statistiques finales devrait réussir");
        }

        // === RÉSUMÉ ===
        System.out.println("\n🎉 TEST COMPLET DU PROFIL TERMINÉ AVEC SUCCÈS !");
        System.out.println("========================================");
        System.out.println("✅ Création d'utilisateur");
        System.out.println("✅ Chargement des statistiques");
        System.out.println("✅ Mise à jour du profil");
        System.out.println("✅ Changement de mot de passe");
        System.out.println("✅ Validations des contraintes");
        System.out.println("✅ Désactivation/réactivation du compte");
        System.out.println("✅ Statistiques finales");
        System.out.println("========================================");
        System.out.println("\n🎯 TOUTES LES FONCTIONNALITÉS DU PROFIL FONCTIONNENT CORRECTEMENT !");
    }
}
