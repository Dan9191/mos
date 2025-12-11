package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStage;

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

    /**
     * Найти текущий активный этап
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = false ORDER BY os.createdAt DESC")
    Optional<OrderStage> findCurrentStageByOrderId(@Param("orderId") Long orderId);

    /**
     * Найти этапы определенного типа - ИСПРАВЛЕНО: принимает String
     **/
    @Query("SELECT os FROM OrderStage os WHERE os.order.id = :orderId AND os.type.name = :stageType")
    List<OrderStage> findByOrderIdAndType(@Param("orderId") Long orderId,
                                          @Param("stageType") String stageType);

    /**
     * Получить статистику по этапам
     **/
    @Query("SELECT COUNT(os) FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = true")
    Long countCompletedStages(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(os) FROM OrderStage os WHERE os.order.id = :orderId AND os.isCompleted = false")
    Long countActiveStages(@Param("orderId") Long orderId);
}