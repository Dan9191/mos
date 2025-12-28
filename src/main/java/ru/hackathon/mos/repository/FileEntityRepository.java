package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.FileEntity;

import java.util.List;

/**
 * Репозиторий работы с файлами.
 */
@Repository
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findAllByOwnerTypeAndOwnerIdOrderBySortOrderAsc(String ownerType, Long ownerId);

    List<FileEntity> findAllByOwnerTypeAndOwnerIdAndFileRole(
            String ownerType, Long ownerId, String fileRole);

    boolean existsByOwnerTypeAndOwnerIdAndFileRole(
            String ownerType,
            Long ownerId,
            String fileRole);

    @Query("""
    SELECT COALESCE(MAX(f.sortOrder) + 1, 1)
    FROM FileEntity f
    WHERE f.ownerType = :ownerType
      AND f.ownerId   = :ownerId
""")
    int getNextSortOrder(
            @Param("ownerType") String ownerType,
            @Param("ownerId") Long ownerId
    );
}
