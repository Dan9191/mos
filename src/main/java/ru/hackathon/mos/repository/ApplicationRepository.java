package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hackathon.mos.entity.Application;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query(value = """
        SELECT a.* FROM application a
        ORDER BY 
            CASE a.status_id 
                WHEN 1 THEN 1  -- created
                WHEN 2 THEN 2  -- consideration  
                WHEN 3 THEN 3  -- accepted
                WHEN 4 THEN 4  -- rejected
                ELSE 5
            END,
            a.created_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM application",
            nativeQuery = true)
    Page<Application> findAllOrderedByStatusAndDate(Pageable pageable);

    @Query(value = """
        SELECT a.* FROM application a
        WHERE a.creator_id = :creatorId
        ORDER BY 
            CASE a.status_id 
                WHEN 1 THEN 1  -- created
                WHEN 2 THEN 2  -- consideration  
                WHEN 3 THEN 3  -- accepted
                WHEN 4 THEN 4  -- rejected
                ELSE 5
            END,
            a.created_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM application a WHERE a.creator_id = :creatorId",
            nativeQuery = true)
    Page<Application> findAllByCreatorIdOrderedByStatusAndDate(UUID creatorId, Pageable pageable);

    @Query(value = """
        SELECT a.* FROM application a
        WHERE a.manager_id = :managerId
        ORDER BY 
            CASE a.status_id 
                WHEN 1 THEN 1  -- created
                WHEN 2 THEN 2  -- consideration  
                WHEN 3 THEN 3  -- accepted
                WHEN 4 THEN 4  -- rejected
                ELSE 5
            END,
            a.created_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM application a WHERE a.manager_id = :managerId",
            nativeQuery = true)
    Page<Application> findAllByManagerIdOrderedByStatusAndDate(UUID managerId, Pageable pageable);
}