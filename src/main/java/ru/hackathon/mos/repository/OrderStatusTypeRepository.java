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

    Optional<OrderStatusType> findByName(String name);

    @Query("SELECT ost FROM OrderStatusType ost WHERE ost.name IN :names")
    List<OrderStatusType> findByNames(@Param("names") List<String> names);

    boolean existsByName(String name);
}
