package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStatus;
import ru.hackathon.mos.entity.OrderStatusType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    /**
     * Найти все статусы заказа
     **/
    @Query("SELECT os FROM OrderStatus os WHERE os.order.id = :orderId ORDER BY os.createdAt DESC")
    Page<OrderStatus> findByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    /**
     * Найти последний статус заказа
     **/
    @Query("SELECT os FROM OrderStatus os WHERE os.order.id = :orderId ORDER BY os.createdAt DESC LIMIT 1")
    Optional<OrderStatus> findLatestByOrderId(@Param("orderId") Long orderId);

    /**
     * Найти статусы определенного типа по заказу
      */
    @Query("SELECT os FROM OrderStatus os WHERE os.order.id = :orderId AND os.type.name = :statusType")
    List<OrderStatus> findByOrderIdAndType(@Param("orderId") Long orderId,
                                           @Param("statusType") String statusType);

    /**
     * Проверить существование статуса определенного типа
     */
    @Query("SELECT CASE WHEN COUNT(os) > 0 THEN true ELSE false END " +
            "FROM OrderStatus os WHERE os.order.id = :orderId AND os.type.name = :statusType")
    boolean existsByOrderIdAndType(@Param("orderId") Long orderId,
                                   @Param("statusType") String statusType);
}