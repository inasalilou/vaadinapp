package com.inas.vaadinapp.repository;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActifTrueAndRole(Role role);

    List<User> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    long countByRole(Role role);
}
