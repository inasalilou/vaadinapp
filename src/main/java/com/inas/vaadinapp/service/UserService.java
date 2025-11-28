package com.inas.vaadinapp.service;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

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

    /* ------------------- UTILITAIRES ------------------- */

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
