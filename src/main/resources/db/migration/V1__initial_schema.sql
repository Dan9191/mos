CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS project_template (
                                  id          BIGSERIAL PRIMARY KEY,
                                  title       VARCHAR(256) NOT NULL,
                                  description TEXT,
                                  base_price  DECIMAL(12,2),
                                  area_m2     DECIMAL(8,2),
                                  rooms       INTEGER,
                                  style       VARCHAR(64),
                                  is_active   BOOLEAN DEFAULT TRUE,
                                  created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                  updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE  project_template                        IS 'Шаблоны проектов — каталог домов, доступный пользователям для выбора';
COMMENT ON COLUMN project_template.id                     IS 'Уникальный идентификатор шаблона';
COMMENT ON COLUMN project_template.title                  IS 'Название шаблона, отображается в карточке (например, "Минимализм 120 м²")';
COMMENT ON COLUMN project_template.description            IS 'Подробное описание проекта';
COMMENT ON COLUMN project_template.base_price             IS 'Базовая стоимость строительства';
COMMENT ON COLUMN project_template.area_m2                IS 'Общая площадь дома в квадратных метрах';
COMMENT ON COLUMN project_template.rooms                  IS 'Количество комнат';
COMMENT ON COLUMN project_template.style                  IS 'Стиль дома: минимализм, сканди, классика, хайтек и т.д.';
COMMENT ON COLUMN project_template.is_active              IS 'Флаг активности — виден ли шаблон пользователям';
COMMENT ON COLUMN project_template.created_at             IS 'Дата и время создания записи';
COMMENT ON COLUMN project_template.updated_at             IS 'Дата и время последнего обновления';


CREATE TABLE IF NOT EXISTS file_entity (
                             id             BIGSERIAL PRIMARY KEY,
                             owner_type     VARCHAR(50)  NOT NULL,
                             owner_id       BIGINT       NOT NULL,
                             filename       VARCHAR(512) NOT NULL,
                             mime_type      VARCHAR(128) NOT NULL,
                             size_bytes     BIGINT       NOT NULL CHECK (size_bytes >= 0),
                             file_data      BYTEA        NOT NULL,
                             file_role      VARCHAR(64)  DEFAULT 'attachment',
                             sort_order     INTEGER      DEFAULT 0,
                             uploaded_by    VARCHAR(64),
                             created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                             UNIQUE(owner_type, owner_id, file_role, sort_order)
);


COMMENT ON TABLE  file_entity                             IS 'Универсальное хранилище всех файлов системы (одна таблица на всё приложение)';
COMMENT ON COLUMN file_entity.id                          IS 'Уникальный идентификатор файла';
COMMENT ON COLUMN file_entity.owner_type                  IS 'Тип владельца: project_template, order, order_stage, user, chat_message и т.д.';
COMMENT ON COLUMN file_entity.owner_id                    IS 'ID объекта-владельца в его таблице';
COMMENT ON COLUMN file_entity.filename                    IS 'Оригинальное имя файла (например, домик.jpg, смета.pdf)';
COMMENT ON COLUMN file_entity.mime_type                   IS 'MIME-тип файла (image/jpeg, application/pdf и т.д.)';
COMMENT ON COLUMN file_entity.size_bytes                  IS 'Размер файла в байтах';
COMMENT ON COLUMN file_entity.file_data                   IS 'Бинарные данные файла';
COMMENT ON COLUMN file_entity.file_role                   IS 'Роль файла в контексте владельца: preview, gallery, document, plan, photo, avatar, receipt и т.д.';
COMMENT ON COLUMN file_entity.sort_order                  IS 'Порядок сортировки (особенно для галереи)';
COMMENT ON COLUMN file_entity.uploaded_by                 IS 'ID пользователя, загрузившего файл (ссылка на будущую таблицу user)';
COMMENT ON COLUMN file_entity.created_at                  IS 'Дата и время загрузки файла';