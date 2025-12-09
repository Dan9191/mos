package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.mos.entity.UserType;

import java.util.Optional;

/**
 * Репозиторий для работы с типом пользователя.
 */
public interface UserTypeRepository extends JpaRepository<UserType, Integer> {
    Optional<UserType> findByName(String name);
}
