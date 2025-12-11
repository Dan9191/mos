package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.Order;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Найти все заказы пользователя
      */
    @Query("SELECT o FROM Order o WHERE o.client.id = :userId")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Найти заказы пользователя с определенным статусом
     */
    @Query("SELECT o FROM Order o WHERE o.client.id = :userId AND " +
            "EXISTS (SELECT os FROM OrderStatus os WHERE os.order.id = o.id AND os.type.name = :statusType " +
            "AND os.createdAt = (SELECT MAX(os2.createdAt) FROM OrderStatus os2 WHERE os2.order.id = o.id))")
    Page<Order> findByUserIdAndStatus(@Param("userId") UUID userId,
                                      @Param("statusType") String statusType,
                                      Pageable pageable);

    /**
     * Найти активные заказы пользователя (не закрытые)
      */
    @Query("SELECT o FROM Order o WHERE o.client.id = :userId AND " +
            "o.id NOT IN (SELECT os.order.id FROM OrderStatus os WHERE os.type.name = 'CLOSED')")
    List<Order> findActiveOrdersByUserId(@Param("userId") UUID userId);

    /**
     * Проверить, принадлежит ли заказ пользователю
      */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o WHERE o.id = :orderId AND o.client.id = :userId")
    boolean existsByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") UUID userId);
}