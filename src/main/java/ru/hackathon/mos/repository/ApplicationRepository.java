package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.mos.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
