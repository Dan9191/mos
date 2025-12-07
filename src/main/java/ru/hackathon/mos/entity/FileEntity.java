package ru.hackathon.mos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Универсальная сущность для всех файлов в системе.
 * Одна таблица используется для всех типов контента: превью, галерея, документы, фото со стройки и т.д.
 */
@Entity
@Table(name = "file_entity")
@Data
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип владельца файла.
     * Примеры: project_template, order, order_stage, user, chat_message
     */
    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;

    /**
     * ID объекта-владельца в соответствующей таблице.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * Оригинальное имя файла (например, "план_дома.jpg").
     */
    @Column(nullable = false, length = 512)
    private String filename;

    /**
     * MIME-тип файла (image/jpeg, application/pdf и т.д.).
     */
    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    /**
     * Размер файла в байтах.
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * Путь к файлу на диске относительно корня приложения.
     * Пример: /uploads/550e8400-e29b-41d4-a716-446655440000.jpg
     */
    @Column(name = "storage_path", nullable = false, unique = true, length = 1024)
    private String storagePath;

    /**
     * Роль файла в контексте владельца.
     * Примеры: preview, gallery, document, plan, photo, avatar, receipt
     */
    @Column(name = "file_role", length = 64)
    private String fileRole = "attachment";

    /**
     * Порядок отображения (особенно важно для галереи).
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * ID пользователя, загрузившего файл (будет ссылкой на таблицу user).
     */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    /**
     * Дата и время загрузки файла.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}