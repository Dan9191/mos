package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStageTypeRepository extends JpaRepository<OrderStageType, Long> {

    Optional<OrderStageType> findByName(String name);

    @Query("SELECT ost FROM OrderStageType ost WHERE ost.isMandatory = true ORDER BY ost.displayOrder")
    List<OrderStageType> findMandatoryStageTypes();

    @Query("SELECT ost FROM OrderStageType ost ORDER BY ost.displayOrder")
    List<OrderStageType> findAllOrdered();

    boolean existsByName(String name);
}