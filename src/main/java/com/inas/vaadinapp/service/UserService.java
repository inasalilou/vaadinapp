package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationService reservationService;
    private final EventService eventService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ReservationService reservationService, EventService eventService) {
        this.userRepository = userRepository;
        this.reservationService = reservationService;
        this.eventService = eventService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Note: EventService et ReservationService devraient être injectés ici
    // pour les calculs de statistiques, mais nous les laisserons pour plus tard

    /* ------------------- INSCRIPTION ------------------- */

    public User register(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé !");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateInscription(LocalDateTime.now());
        user.setActif(true);

        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }

        return userRepository.save(user);
    }

    /* ------------------- AUTHENTIFICATION ------------------- */

    public Optional<User> login(String email, String password) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /* ------------------- MISE À JOUR PROFIL ------------------- */

    @Transactional
    public User updateProfile(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Mise à jour des champs modifiables
        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());
        user.setTelephone(updatedUser.getTelephone());

        // Vérification de l'unicité de l'email si changé
        if (!user.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email déjà utilisé par un autre utilisateur");
            }
            user.setEmail(updatedUser.getEmail());
        }

        return userRepository.save(user);
    }

    /* ------------------- CHANGEMENT MOT DE PASSE ------------------- */

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        // Validation du nouveau mot de passe
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /* ------------------- DÉSACTIVATION/ACTIVATION COMPTE ------------------- */

    @Transactional
    public User toggleAccountStatus(Long userId, boolean activate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setActif(activate);
        return userRepository.save(user);
    }

    /* ------------------- STATISTIQUES UTILISATEUR ------------------- */

    public UserStatistics getUserStatistics(Long userId) {
        // Vérification que l'utilisateur existe
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Calcul du nombre d'événements créés
        long eventsCreated = eventService.countEventsByOrganizer(userId);

        // Calcul du nombre de réservations
        long reservationsCount = reservationService.countReservationsByUser(userId);

        // Calcul du montant total dépensé
        double totalSpent = reservationService.totalSpentByUser(userId);

        return new UserStatistics(eventsCreated, reservationsCount, totalSpent);
    }

    /* ------------------- LISTE UTILISATEURS AVEC FILTRES ------------------- */

    public List<User> findUsersWithFilters(String searchTerm, Role role, Boolean actif) {
        List<User> users;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Recherche par nom ou prénom
            users = userRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
                searchTerm.trim(), searchTerm.trim());
        } else {
            // Tous les utilisateurs
            users = userRepository.findAll();
        }

        // Filtrage par rôle
        if (role != null) {
            users = users.stream()
                    .filter(user -> user.getRole() == role)
                    .toList();
        }

        // Filtrage par statut actif
        if (actif != null) {
            users = users.stream()
                    .filter(user -> user.getActif() == actif)
                    .toList();
        }

        return users;
    }

    /* ------------------- UTILITAIRES ------------------- */

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /* ------------------- CLASSE INTERNE POUR STATISTIQUES ------------------- */

    public static class UserStatistics {
        private final long eventsCreated;
        private final long reservationsCount;
        private final double totalSpent;

        public UserStatistics(long eventsCreated, long reservationsCount, double totalSpent) {
            this.eventsCreated = eventsCreated;
            this.reservationsCount = reservationsCount;
            this.totalSpent = totalSpent;
        }

        public long getEventsCreated() { return eventsCreated; }
        public long getReservationsCount() { return reservationsCount; }
        public double getTotalSpent() { return totalSpent; }
    }
}
