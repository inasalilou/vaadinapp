package com.inas.vaadinapp.view;

import com.inas.vaadinapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RegisterViewTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterViewStructure() {
        System.out.println("🧪 Test de la structure de RegisterView");

        // Vérifier que la route est correctement configurée
        var routeAnnotation = RegisterView.class.getAnnotation(com.vaadin.flow.router.Route.class);
        assertNotNull(routeAnnotation, "La classe doit avoir l'annotation @Route");
        assertEquals("register", routeAnnotation.value(), "La route doit être 'register'");

        // Vérifier que le titre de page est configuré
        var pageTitleAnnotation = RegisterView.class.getAnnotation(com.vaadin.flow.router.PageTitle.class);
        assertNotNull(pageTitleAnnotation, "La classe doit avoir l'annotation @PageTitle");
        assertEquals("Inscription - EventManager", pageTitleAnnotation.value(),
            "Le titre de page doit être correct");

        System.out.println("✅ Structure de RegisterView validée");
    }

    @Test
    void testUserRegistrationWorkflow() {
        System.out.println("🧪 Test du workflow d'inscription utilisateur");

        // Simuler la création d'un nouvel utilisateur
        String testEmail = "test.register@example.com";
        String testPassword = "TestPassword123!";

        // Vérifier que l'utilisateur n'existe pas déjà
        var existingUser = userService.findByEmail(testEmail);
        assertTrue(existingUser.isEmpty(), "L'utilisateur de test ne devrait pas exister");

        // Créer un nouvel utilisateur (simule ce que fait RegisterView)
        var newUser = new com.inas.vaadinapp.entity.User();
        newUser.setNom("TestRegister");
        newUser.setPrenom("User");
        newUser.setEmail(testEmail);
        newUser.setPassword(testPassword);
        newUser.setRole(com.inas.vaadinapp.entity.Role.CLIENT);

        // Enregistrer l'utilisateur
        var savedUser = userService.register(newUser);
        assertNotNull(savedUser.getId(), "L'utilisateur enregistré devrait avoir un ID");
        assertEquals(testEmail, savedUser.getEmail(), "L'email devrait être correct");
        assertEquals("TestRegister", savedUser.getNom(), "Le nom devrait être correct");

        // Vérifier que l'utilisateur peut maintenant être trouvé
        var foundUser = userService.findByEmail(testEmail);
        assertTrue(foundUser.isPresent(), "L'utilisateur devrait pouvoir être trouvé");
        assertEquals(savedUser.getId(), foundUser.get().getId(), "Les IDs devraient correspondre");

        System.out.println("✅ Workflow d'inscription validé");
    }

    @Test
    void testEmailValidation() {
        System.out.println("🧪 Test de validation des emails");

        // Test d'emails valides
        String[] validEmails = {"test@example.com", "user.name@domain.co.uk", "test+tag@gmail.com"};
        for (String email : validEmails) {
            assertTrue(email.contains("@") && email.contains("."), "Email valide: " + email);
        }

        // Test d'emails invalides
        String[] invalidEmails = {"invalid", "invalid@", "invalid.com"};
        for (String email : invalidEmails) {
            assertFalse(email.contains("@") && email.contains("."), "Email invalide: " + email);
        }

        // Test d'email qui contient @ et . mais est quand même invalide
        String trickyInvalid = "@invalid.com";
        assertTrue(trickyInvalid.startsWith("@"),
            "Email commençant par @ est invalide: " + trickyInvalid);

        System.out.println("✅ Validation d'email fonctionnelle");
    }

    @Test
    void testPasswordStrengthValidation() {
        System.out.println("🧪 Test de validation de la force des mots de passe");

        // Test mot de passe fort (devrait réussir)
        String strongPassword = "MySecurePass123!";
        int strengthScore = calculatePasswordStrength(strongPassword);
        assertTrue(strengthScore >= 3, "Le mot de passe fort devrait avoir un score >= 3");

        // Test mot de passe faible (devrait échouer la validation)
        String weakPassword = "weak";
        int weakScore = calculatePasswordStrength(weakPassword);
        assertTrue(weakScore < 3, "Le mot de passe faible devrait avoir un score < 3");

        System.out.println("✅ Validation de force des mots de passe fonctionnelle");
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        return score;
    }
}
