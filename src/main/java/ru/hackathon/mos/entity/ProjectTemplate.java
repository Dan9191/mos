package ru.hackathon.mos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Шаблон проекта — элемент каталога домов.
 * Пользователь выбирает один из шаблонов при создании заказа.
 */
@Entity
@Table(name = "project_template")
@Data
public class ProjectTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название шаблона, отображается в карточке.
     * Пример: "Минимализм 120 м²"
     */
    @Column(nullable = false, length = 256, name = "title")
    private String title;

    /**
     * Подробное описание проекта.
     */
    @Column(name = "description")
    private String description;

    /**
     * Базовая стоимость строительства.
     */
    @Column(name = "base_price")
    private BigDecimal basePrice;

    /**
     * Общая площадь дома в м².
     */
    @Column(name = "area_m2")
    private Double areaM2;

    /**
     * Количество комнат.
     */
    @Column(name = "rooms")
    private Integer rooms;

    /**
     * Архитектурный стиль: минимализм, сканди, классика и т.д.
     */
    @Column(name = "style")
    private String style;

    /**
     * Виден ли шаблон пользователям.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Дата создания записи.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата последнего обновления.
     */
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}