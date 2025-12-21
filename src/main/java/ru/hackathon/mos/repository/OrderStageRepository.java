package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStage;
import ru.hackathon.mos.entity.OrderStageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStageRepository extends JpaRepository<OrderStage, Long> {

    /**
     * Найти все этапы заказа
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId ORDER BY os.createdAt DESC")
    Page<OrderStage> findByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    /**
     * Найти активные этапы заказа (не завершенные)
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = false ORDER BY os.createdAt")
    List<OrderStage> findActiveStagesByOrderId(@Param("orderId") Long orderId);

    /**
     * Найти завершенные этапы заказа
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = true ORDER BY os.completionDate DESC")
    List<OrderStage> findCompletedStagesByOrderId(@Param("orderId") Long orderId);

//    /**
//     * Найти текущий активный этап - ИСПРАВЛЕНО: возвращаем первый результат
//     **/
//    default Optional<OrderStage> findCurrentStageByOrderId(Long orderId) {
//        // Используем встроенный метод Spring Data JPA
//        return findFirstByOrderIdAndIsCompletedFalseOrderByCreatedAtDesc(orderId);
//    }

    /**
     * Найти текущий активный этап
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId ORDER BY os.createdAt DESC Limit 1")
    Optional<OrderStage> findCurrentStageByOrderId(@Param("orderId") Long orderId);

    /**
     * Найти первый активный этап - Spring Data JPA сгенерирует метод
     **/
    Optional<OrderStage> findFirstByOrderIdAndIsCompletedFalseOrderByCreatedAtDesc(Long orderId);

    /**
     * Найти этапы определенного типа - ИСПРАВЛЕНО: принимает StageName enum
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId AND os.type.name = :stageType")
    List<OrderStage> findByOrderIdAndType(@Param("orderId") Long orderId,
                                          @Param("stageType") OrderStageType.StageName stageType);

    /**
     * Найти этапы определенного типа по строке (удобный метод)
     **/
    default List<OrderStage> findByOrderIdAndTypeName(Long orderId, String stageTypeName) {
        // Преобразуем строку в enum
        OrderStageType.StageName stageName = OrderStageType.StageName.fromValue(stageTypeName);
        return findByOrderIdAndType(orderId, stageName);
    }

    /**
     * Получить статистику по этапам
     **/
    @Query("SELECT COUNT(os) FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = true")
    Long countCompletedStages(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(os) FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = false")
    Long countActiveStages(@Param("orderId") Long orderId);
}