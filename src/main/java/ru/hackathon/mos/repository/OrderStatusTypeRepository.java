package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStatusType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusTypeRepository extends JpaRepository<OrderStatusType, Long> {

    // Исправляем - используем JPQL с UPPER для регистронезависимого поиска
    @Query("SELECT ost FROM OrderStatusType ost WHERE UPPER(ost.name) = UPPER(:name)")
    Optional<OrderStatusType> findByName(@Param("name") String name);

    // ИСПРАВЛЕННЫЙ метод - убрали вложенный SELECT
    @Query("SELECT ost FROM OrderStatusType ost WHERE UPPER(ost.name) IN :names")
    List<OrderStatusType> findByNames(@Param("names") List<String> names);

    // Исправляем и этот метод
    @Query("SELECT CASE WHEN COUNT(ost) > 0 THEN true ELSE false END " +
            "FROM OrderStatusType ost WHERE UPPER(ost.name) = UPPER(:name)")
    boolean existsByName(@Param("name") String name);
}