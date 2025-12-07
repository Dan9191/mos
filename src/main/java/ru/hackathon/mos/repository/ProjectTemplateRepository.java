package ru.hackathon.mos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hackathon.mos.entity.ProjectTemplate;

/**
 * Репозиторий для работы с шаблонами проектов.
 */
@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, Long> {

    /**
     * Получение активных шаблонов.
     *
     * @param pageable Настройки пагинации.
     * @return список активных шаблоны
     */
    Page<ProjectTemplate> findAllByIsActiveTrue(Pageable pageable);

}
