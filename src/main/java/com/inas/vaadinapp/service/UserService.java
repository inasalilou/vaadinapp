package com.inas.vaadinapp.service;

import com.inas.vaadinapp.dto.UserStats;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Inscription d'un nouvel utilisateur
     * - validation Bean Validation
     * - vérification unicité email
     * - hashage du mot de passe
     */
    public User registerUser(User user) {
        // Validation des contraintes (@NotBlank, @Email, @Size...)
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Données utilisateur invalides : " + message);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("L'email est déjà utilisé");
        }

        // Hashage du mot de passe
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // dateInscription et actif sont déjà gérés dans le constructeur,
        // mais on peut s'assurer qu'ils sont bien initialisés
        if (user.getActif() == null) {
            user.setActif(true);
        }

        return userRepository.save(user);
    }

    /**
     * Authentification : retourne l'utilisateur si email/password sont corrects
     */
    public Optional<User> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    /**
     * Mise à jour du profil (nom, prénom, téléphone, actif éventuellement)
     */
    public User updateProfile(Long userId, String nom, String prenom, String telephone, Boolean actif) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (nom != null && !nom.isBlank()) {
            user.setNom(nom);
        }
        if (prenom != null && !prenom.isBlank()) {
            user.setPrenom(prenom);
        }
        user.setTelephone(telephone); // peut être null
        if (actif != null) {
            user.setActif(actif);
        }

        return userRepository.save(user);
    }

    /**
     * Changement de mot de passe
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Désactivation / activation d'un compte
     */
    public void setAccountActive(Long userId, boolean actif) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setActif(actif);
        userRepository.save(user);
    }

    /**
     * Récupération des statistiques utilisateur.
     * Pour l'instant, valeurs fictives (on complètera quand Event/Reservation seront créés).
     */
    public UserStats getUserStats(Long userId) {
        // TODO: implémenter avec EventService et ReservationService plus tard
        return new UserStats(0, 0, 0.0);
    }

    /**
     * Liste des utilisateurs avec filtres (nom, rôle, actif)
     * Si un filtre est null, il n'est pas appliqué.
     */
    public List<User> findUsers(String nomFilter, Role roleFilter, Boolean actifFilter) {
        return userRepository.findAll().stream()
                .filter(u -> nomFilter == null
                        || u.getNom().toLowerCase().contains(nomFilter.toLowerCase())
                        || u.getPrenom().toLowerCase().contains(nomFilter.toLowerCase()))
                .filter(u -> roleFilter == null || u.getRole() == roleFilter)
                .filter(u -> actifFilter == null || u.getActif().equals(actifFilter))
                .collect(Collectors.toList());
    }

    // Méthodes utilitaires déjà utiles pour la suite

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
