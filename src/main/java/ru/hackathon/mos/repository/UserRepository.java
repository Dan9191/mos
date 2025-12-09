package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.mos.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с пользователями.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}

