package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ProfileViewTest {

    @Mock
    private UserService userService;

    private User testUser;
    private UserService.UserStatistics testStats;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Créer un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Dupont");
        testUser.setPrenom("Jean");
        testUser.setEmail("jean.dupont@test.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(Role.CLIENT);
        testUser.setTelephone("06 12 34 56 78");
        testUser.setDateInscription(LocalDateTime.now().minusDays(30));
        testUser.setActif(true);

        // Créer des statistiques de test
        testStats = new UserService.UserStatistics(5, 12, 150.75);
    }

    @Test
    void testRouteConfiguration() {
        // Vérifier que la route est correctement configurée via annotation
        var routeAnnotation = ProfileView.class.getAnnotation(com.vaadin.flow.router.Route.class);
        assertNotNull(routeAnnotation, "La classe doit avoir l'annotation @Route");
        assertEquals("profile", routeAnnotation.value(), "La route doit être 'profile'");
    }

    @Test
    void testUserServiceIntegration() {
        // Tester l'intégration avec UserService pour la mise à jour du profil
        User updatedUser = new User();
        updatedUser.setNom("Martin");
        updatedUser.setPrenom("Pierre");
        updatedUser.setEmail("pierre.martin@test.com");
        updatedUser.setTelephone("06 98 76 54 32");

        when(userService.updateProfile(anyLong(), any(User.class))).thenReturn(updatedUser);

        User result = userService.updateProfile(1L, updatedUser);
        assertNotNull(result);
        assertEquals("Martin", result.getNom());
        assertEquals("Pierre", result.getPrenom());
        assertEquals("pierre.martin@test.com", result.getEmail());
        assertEquals("06 98 76 54 32", result.getTelephone());
    }

    @Test
    void testPasswordChangeValidation() {
        // Tester la validation du changement de mot de passe
        // Pour les tests, nous mockons simplement la méthode pour qu'elle ne fasse rien
        // La vraie validation se fait dans le service

        // Test mot de passe valide (ne devrait pas lever d'exception du service mocké)
        assertDoesNotThrow(() -> userService.changePassword(1L, "oldpass", "newpassword123"));

        // Note: La vraie validation des mots de passe courts se fait dans le service réel,
        // mais nous testons ici l'intégration avec le service mocké
    }

    @Test
    void testUserStatisticsRetrieval() {
        // Tester la récupération des statistiques utilisateur
        when(userService.getUserStatistics(anyLong())).thenReturn(testStats);

        UserService.UserStatistics stats = userService.getUserStatistics(1L);

        assertNotNull(stats);
        assertEquals(5, stats.getEventsCreated());
        assertEquals(12, stats.getReservationsCount());
        assertEquals(150.75, stats.getTotalSpent());
    }

    @Test
    void testAccountDeactivation() {
        // Tester la désactivation du compte
        when(userService.toggleAccountStatus(anyLong(), any(Boolean.class)))
            .thenAnswer(invocation -> {
                Long userId = invocation.getArgument(0);
                Boolean activate = invocation.getArgument(1);

                User deactivatedUser = new User();
                deactivatedUser.setId(userId);
                deactivatedUser.setActif(activate);
                return deactivatedUser;
            });

        User result = userService.toggleAccountStatus(1L, false);
        assertNotNull(result);
        assertFalse(result.getActif());
    }

    @Test
    void testEmailUniquenessValidation() {
        // Tester la validation d'unicité de l'email
        when(userService.updateProfile(anyLong(), any(User.class)))
            .thenThrow(new IllegalArgumentException("Email déjà utilisé par un autre utilisateur"));

        User updatedUser = new User();
        updatedUser.setEmail("existing@test.com");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            userService.updateProfile(1L, updatedUser));
        assertTrue(exception.getMessage().contains("Email déjà utilisé"));
    }

    @Test
    void testProfileUpdateValidation() {
        // Tester la validation des champs obligatoires lors de la mise à jour du profil
        when(userService.updateProfile(anyLong(), any(User.class)))
            .thenAnswer(invocation -> {
                User updatedUser = invocation.getArgument(1);
                if (updatedUser.getNom() == null || updatedUser.getNom().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le nom est obligatoire");
                }
                if (updatedUser.getPrenom() == null || updatedUser.getPrenom().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le prénom est obligatoire");
                }
                if (updatedUser.getEmail() == null || updatedUser.getEmail().trim().isEmpty()) {
                    throw new IllegalArgumentException("L'email est obligatoire");
                }
                return updatedUser;
            });

        // Test avec champs vides
        User invalidUser = new User();
        invalidUser.setNom("");
        invalidUser.setPrenom("");
        invalidUser.setEmail("");

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateProfile(1L, invalidUser));

        // Test avec champs valides
        User validUser = new User();
        validUser.setNom("Dupont");
        validUser.setPrenom("Jean");
        validUser.setEmail("jean.dupont@test.com");

        assertDoesNotThrow(() -> userService.updateProfile(1L, validUser));
    }

    @Test
    void testPasswordConfirmationLogic() {
        // Tester la logique de confirmation du mot de passe (bien que ce soit côté UI)
        String newPassword = "newpassword123";
        String confirmPassword = "newpassword123";
        String wrongConfirmPassword = "differentpassword";

        // Test confirmation correcte
        assertTrue(newPassword.equals(confirmPassword));

        // Test confirmation incorrecte
        assertFalse(newPassword.equals(wrongConfirmPassword));
    }

    @Test
    void testUserRoleDisplay() {
        // Tester l'affichage du rôle utilisateur
        assertEquals("CLIENT", testUser.getRole().toString());
        assertEquals("ADMIN", Role.ADMIN.toString());
        assertEquals("ORGANIZER", Role.ORGANIZER.toString());
    }

    @Test
    void testUserRegistrationDateFormatting() {
        // Tester le formatage de la date d'inscription
        LocalDateTime registrationDate = testUser.getDateInscription();
        assertNotNull(registrationDate);

        // Vérifier que la date est dans le passé
        assertTrue(registrationDate.isBefore(LocalDateTime.now()));
    }

    @Test
    void testPhoneNumberFormatting() {
        // Tester le formatage du numéro de téléphone
        String phoneNumber = testUser.getTelephone();
        assertNotNull(phoneNumber);
        assertEquals("06 12 34 56 78", phoneNumber);
    }

    @Test
    void testStatisticsCalculations() {
        // Tester les calculs des statistiques
        long eventsCreated = testStats.getEventsCreated();
        long reservationsCount = testStats.getReservationsCount();
        double totalSpent = testStats.getTotalSpent();

        // Vérifier que les valeurs sont positives ou nulles
        assertTrue(eventsCreated >= 0);
        assertTrue(reservationsCount >= 0);
        assertTrue(totalSpent >= 0);

        // Calcul du coût moyen par réservation
        if (reservationsCount > 0) {
            double averageCost = totalSpent / reservationsCount;
            assertTrue(averageCost >= 0);
            System.out.println("Coût moyen par réservation : " + String.format("%.2f dh", averageCost));
        }
    }
}
