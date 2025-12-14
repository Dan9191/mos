package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.WebCamera;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebCameraRepository extends JpaRepository<WebCamera, Long> {

    List<WebCamera> findByOrderId(Long orderId);

    Optional<WebCamera> findByIdAndOrderId(Long id, Long orderId);
}