package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.mos.entity.UserType;

/**
 * Репозиторий для работы с типом пользователя.
 */
public interface UserTypeRepository extends JpaRepository<UserType, Integer> {
}
