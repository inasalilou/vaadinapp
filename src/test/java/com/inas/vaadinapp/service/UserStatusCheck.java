package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class UserStatusCheck {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void checkAndFixUserStatus() {
        System.out.println("🔍 VÉRIFICATION DE L'ÉTAT DES UTILISATEURS");
        System.out.println("========================================");

        List<User> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            System.out.println("⚠️ Aucun utilisateur trouvé dans la base de données");
            return;
        }

        for (User user : allUsers) {
            System.out.println("👤 Utilisateur: " + user.getPrenom() + " " + user.getNom());
            System.out.println("   📧 Email: " + user.getEmail());
            System.out.println("   🔓 Actif: " + user.getActif());
            System.out.println("   👑 Rôle: " + user.getRole());

            // Si l'utilisateur n'est pas actif, le réactiver
            if (user.getActif() == null || !user.getActif()) {
                System.out.println("   🚨 COMPTE DÉSACTIVÉ - RÉACTIVATION EN COURS...");

                try {
                    User reactivatedUser = userService.toggleAccountStatus(user.getId(), true);
                    System.out.println("   ✅ COMPTE RÉACTIVÉ AVEC SUCCÈS !");
                    System.out.println("   🔓 Nouvel état: " + reactivatedUser.getActif());
                } catch (Exception e) {
                    System.out.println("   ❌ ERREUR LORS DE LA RÉACTIVATION: " + e.getMessage());
                }
            } else {
                System.out.println("   ✅ Compte actif - tout va bien");
            }

            System.out.println("   ──────────────────────────────────");
        }

        System.out.println("\n🎯 VÉRIFICATION TERMINÉE");
        System.out.println("Si vous voyez encore le message 'Votre compte a été désactivé',");
        System.out.println("essayez de vous déconnecter et de vous reconnecter.");
    }
}
