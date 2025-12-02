package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProfileDebugTest {

    @Autowired
    private UserService userService;

    @Test
    void debugProfileIssues() {
        System.out.println("🐛 DEBUG: Test de débogage des problèmes de profil");

        // Récupérer l'utilisateur existant
        var organizerOpt = userService.findByEmail("organizer@test.com");

        if (organizerOpt.isPresent()) {
            User user = organizerOpt.get();
            System.out.println("👤 Utilisateur trouvé:");
            System.out.println("   - ID: " + user.getId());
            System.out.println("   - Email: " + user.getEmail());
            System.out.println("   - Actif: " + user.getActif());
            System.out.println("   - Nom: " + user.getNom());
            System.out.println("   - Prénom: " + user.getPrenom());
            System.out.println("   - Rôle: " + user.getRole());

            // Tester les statistiques
            try {
                var stats = userService.getUserStatistics(user.getId());
                System.out.println("📊 Statistiques chargées avec succès:");
                System.out.println("   - Événements créés: " + stats.getEventsCreated());
                System.out.println("   - Réservations: " + stats.getReservationsCount());
                System.out.println("   - Dépenses totales: " + stats.getTotalSpent() + "€");
            } catch (Exception e) {
                System.out.println("❌ ERREUR lors du chargement des statistiques: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("❌ Aucun utilisateur trouvé avec l'email organizer@test.com");
        }

        System.out.println("🎯 Test de débogage terminé");
    }
}
