package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
