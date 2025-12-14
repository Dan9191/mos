package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.Document;
import ru.hackathon.mos.entity.DocumentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Найти все документы по orderId
    List<Document> findByOrderId(Long orderId);

    // Найти документ по ID и orderId
    Optional<Document> findByIdAndOrderId(Long id, Long orderId);

    // Найти документы по статусу
    List<Document> findByOrderIdAndStatus(Long orderId, String status);

    // Найти документы по типу
    @Query("SELECT d FROM Document d WHERE d.order.id = :orderId AND d.type.name = :typeName")
    List<Document> findByOrderIdAndTypeName(@Param("orderId") Long orderId,
                                            @Param("typeName") DocumentType.TypeName typeName);

    // Получить историю версий документа
    @Query("SELECT d FROM Document d WHERE d.order.id = :orderId AND d.title = :title ORDER BY d.version DESC")
    List<Document> findHistoryByOrderIdAndTitle(@Param("orderId") Long orderId,
                                                @Param("title") String title);
}