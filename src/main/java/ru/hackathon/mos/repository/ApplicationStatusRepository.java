package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.mos.entity.ApplicationStatus;

public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Integer> {
}