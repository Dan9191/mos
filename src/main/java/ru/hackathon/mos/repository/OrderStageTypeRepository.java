package ru.hackathon.mos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.OrderStageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStageTypeRepository extends JpaRepository<OrderStageType, Long> {

    // Используем default метод с вашим fromValue
    default Optional<OrderStageType> findByName(String name) {
        try {
            // Преобразуем строку в enum через ваш метод
            OrderStageType.StageName stageName = OrderStageType.StageName.fromValue(name);
            // Ищем по enum
            return findByStageName(stageName);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // Вспомогательный метод для поиска по enum
    @Query("SELECT ost FROM OrderStageType ost WHERE ost.name = :name")
    Optional<OrderStageType> findByStageName(@Param("name") OrderStageType.StageName name);

    @Query("SELECT ost FROM OrderStageType ost WHERE ost.isMandatory = true ORDER BY ost.displayOrder")
    List<OrderStageType> findMandatoryStageTypes();

    @Query("SELECT ost FROM OrderStageType ost ORDER BY ost.displayOrder")
    List<OrderStageType> findAllOrdered();

    // Аналогично для exists
    default boolean existsByName(String name) {
        try {
            OrderStageType.StageName stageName = OrderStageType.StageName.fromValue(name);
            return existsByStageName(stageName);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Query("SELECT CASE WHEN COUNT(ost) > 0 THEN true ELSE false END " +
            "FROM OrderStageType ost WHERE ost.name = :name")
    boolean existsByStageName(@Param("name") OrderStageType.StageName name);
}