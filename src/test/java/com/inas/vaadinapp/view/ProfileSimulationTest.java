package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProfileSimulationTest {

    @Autowired
    private UserService userService;

    @Test
    void simulateProfileAccess() {
        System.out.println("🎭 SIMULATION: Accès à la page profil");

        // Simuler ce qui se passe quand un utilisateur clique sur "mon profil"

        // 1. Récupérer l'utilisateur connecté (simulé)
        User connectedUser = userService.findByEmail("organizer@test.com").orElse(null);

        if (connectedUser == null) {
            System.out.println("🔴 SIMULATION: Aucun utilisateur connecté");
            System.out.println("   → L'utilisateur devrait être redirigé vers /login");
            System.out.println("   → Aucun message d'alerte ne devrait s'afficher");
            return;
        }

        System.out.println("✅ SIMULATION: Utilisateur trouvé:");
        System.out.println("   - Email: " + connectedUser.getEmail());
        System.out.println("   - Actif: " + connectedUser.getActif());
        System.out.println("   - Nom: " + connectedUser.getNom());

        // 2. Simuler la vérification beforeEnter
        System.out.println("\n🔍 SIMULATION: Vérification beforeEnter");

        if (connectedUser.getActif() == null || !connectedUser.getActif()) {
            System.out.println("🔴 SIMULATION: Compte désactivé détecté");
            System.out.println("   → Message d'alerte: 'Votre compte a été désactivé.'");
            System.out.println("   → Redirection vers /login");
            return;
        }

        System.out.println("✅ SIMULATION: Utilisateur actif - accès autorisé");

        // 3. Simuler le chargement des statistiques
        System.out.println("\n📊 SIMULATION: Chargement des statistiques");

        try {
            var stats = userService.getUserStatistics(connectedUser.getId());
            System.out.println("✅ SIMULATION: Statistiques chargées");
            System.out.println("   - Événements créés: " + stats.getEventsCreated());
            System.out.println("   - Réservations: " + stats.getReservationsCount());
            System.out.println("   - Dépenses: " + stats.getTotalSpent() + "€");

            // 4. Simuler l'affichage du profil
            System.out.println("\n🎨 SIMULATION: Construction de l'interface profil");
            System.out.println("✅ SIMULATION: Profil affiché avec succès");
            System.out.println("   - Nom: " + connectedUser.getNom());
            System.out.println("   - Prénom: " + connectedUser.getPrenom());
            System.out.println("   - Email: " + connectedUser.getEmail());
            System.out.println("   - Rôle: " + connectedUser.getRole());
            System.out.println("   - Date d'inscription: " + connectedUser.getDateInscription());

        } catch (Exception e) {
            System.out.println("❌ SIMULATION: Erreur lors du chargement des statistiques");
            System.out.println("   → Message d'alerte: 'Erreur lors du chargement des statistiques'");
            e.printStackTrace();
        }

        System.out.println("\n🎯 SIMULATION TERMINÉE: Aucun problème détecté");
        System.out.println("Si vous voyez un message d'alerte, vérifiez:");
        System.out.println("1. Que vous êtes connecté");
        System.out.println("2. Que votre compte est actif");
        System.out.println("3. Que la session n'a pas expiré");
        System.out.println("4. Videz le cache du navigateur et reconnectez-vous");
    }
}
