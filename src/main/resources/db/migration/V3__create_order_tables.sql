-- Таблица заказов
CREATE TABLE orders (
                        id          BIGSERIAL PRIMARY KEY,
                        client_id   UUID NOT NULL REFERENCES "user"(id),
                        project_id  BIGINT NOT NULL REFERENCES project_template(id),
                        address     VARCHAR(500) NOT NULL,
                        created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE orders IS 'Заказы на строительство';
COMMENT ON COLUMN orders.id IS 'Идентификатор заказа';
COMMENT ON COLUMN orders.client_id IS 'ID клиента (ссылка на user)';
COMMENT ON COLUMN orders.project_id IS 'ID выбранного шаблона проекта';
COMMENT ON COLUMN orders.address IS 'Адрес строительства';
COMMENT ON COLUMN orders.created_at IS 'Дата создания заказа';

-- Типы статусов заказа
CREATE TABLE order_status_type (
                                   id          BIGSERIAL PRIMARY KEY,
                                   name        VARCHAR(50) NOT NULL UNIQUE,
                                   description TEXT
);

COMMENT ON TABLE order_status_type IS 'Типы статусов заказа';
COMMENT ON COLUMN order_status_type.id IS 'Идентификатор типа статуса';
COMMENT ON COLUMN order_status_type.name IS 'Название статуса';
COMMENT ON COLUMN order_status_type.description IS 'Описание статуса';

-- Статусы заказа
CREATE TABLE order_status (
                              id              BIGSERIAL PRIMARY KEY,
                              order_id        BIGINT NOT NULL REFERENCES orders(id),
                              type_id         BIGINT NOT NULL REFERENCES order_status_type(id),
                              status_changed_by UUID NOT NULL REFERENCES "user"(id),
                              created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE order_status IS 'История статусов заказа';
COMMENT ON COLUMN order_status.id IS 'Идентификатор статуса';
COMMENT ON COLUMN order_status.order_id IS 'ID заказа';
COMMENT ON COLUMN order_status.type_id IS 'Тип статуса';
COMMENT ON COLUMN order_status.status_changed_by IS 'Кто изменил статус';
COMMENT ON COLUMN order_status.created_at IS 'Дата изменения статуса';

-- Типы этапов строительства
CREATE TABLE order_stage_type (
                                  id            BIGSERIAL PRIMARY KEY,
                                  name          VARCHAR(50) NOT NULL UNIQUE,
                                  description   TEXT,
                                  is_mandatory  BOOLEAN DEFAULT TRUE,
                                  display_order INTEGER DEFAULT 0
);

COMMENT ON TABLE order_stage_type IS 'Типы этапов строительства';
COMMENT ON COLUMN order_stage_type.id IS 'Идентификатор типа этапа';
COMMENT ON COLUMN order_stage_type.name IS 'Название этапа';
COMMENT ON COLUMN order_stage_type.description IS 'Описание этапа';
COMMENT ON COLUMN order_stage_type.is_mandatory IS 'Обязательный ли этап';
COMMENT ON COLUMN order_stage_type.display_order IS 'Порядок отображения';

-- Этапы строительства
CREATE TABLE order_stage (
                             id                BIGSERIAL PRIMARY KEY,
                             order_id          BIGINT NOT NULL REFERENCES orders(id),
                             type_id           BIGINT NOT NULL REFERENCES order_stage_type(id),
                             stage_changed_by  UUID NOT NULL REFERENCES "user"(id),
                             created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                             start_date        TIMESTAMP WITH TIME ZONE,
                             planned_end_date  TIMESTAMP WITH TIME ZONE,
                             completion_date   TIMESTAMP WITH TIME ZONE,
                             is_completed      BOOLEAN DEFAULT FALSE,
                             progress          INTEGER DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
                             notes             VARCHAR(2000)
);

COMMENT ON TABLE order_stage IS 'Этапы строительства заказа';
COMMENT ON COLUMN order_stage.id IS 'Идентификатор этапа';
COMMENT ON COLUMN order_stage.order_id IS 'ID заказа';
COMMENT ON COLUMN order_stage.type_id IS 'Тип этапа';
COMMENT ON COLUMN order_stage.stage_changed_by IS 'Кто создал/изменил этап';
COMMENT ON COLUMN order_stage.created_at IS 'Дата создания этапа';
COMMENT ON COLUMN order_stage.start_date IS 'Дата начала этапа';
COMMENT ON COLUMN order_stage.planned_end_date IS 'Плановая дата завершения';
COMMENT ON COLUMN order_stage.completion_date IS 'Фактическая дата завершения';
COMMENT ON COLUMN order_stage.is_completed IS 'Этап завершен';
COMMENT ON COLUMN order_stage.progress IS 'Прогресс выполнения (0-100)';
COMMENT ON COLUMN order_stage.notes IS 'Примечания к этапу';