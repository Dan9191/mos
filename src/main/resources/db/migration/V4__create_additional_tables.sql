-- Типы документов
CREATE TABLE document_type (
                               id          BIGSERIAL PRIMARY KEY,
                               name        VARCHAR(50) NOT NULL UNIQUE,
                               description TEXT
);

COMMENT ON TABLE document_type IS 'Типы документов';
COMMENT ON COLUMN document_type.id IS 'Идентификатор типа документа';
COMMENT ON COLUMN document_type.name IS 'Название типа документа';
COMMENT ON COLUMN document_type.description IS 'Описание типа документа';

-- Документы
CREATE TABLE document (
                          id            BIGSERIAL PRIMARY KEY,
                          order_id      BIGINT NOT NULL REFERENCES orders(id),
                          type_id       BIGINT NOT NULL REFERENCES document_type(id),
                          title         VARCHAR(256) NOT NULL,
                          description   TEXT,
                          created_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                          file_entity_id BIGINT REFERENCES file_entity(id),
                          status        VARCHAR(50) DEFAULT 'draft',
                          version       INTEGER DEFAULT 1
);

COMMENT ON TABLE document IS 'Документы по заказам';
COMMENT ON COLUMN document.id IS 'Идентификатор документа';
COMMENT ON COLUMN document.order_id IS 'ID заказа';
COMMENT ON COLUMN document.type_id IS 'Тип документа';
COMMENT ON COLUMN document.title IS 'Название документа';
COMMENT ON COLUMN document.description IS 'Описание документа';
COMMENT ON COLUMN document.created_at IS 'Дата создания документа';
COMMENT ON COLUMN document.file_entity_id IS 'Ссылка на файл документа';
COMMENT ON COLUMN document.status IS 'Статус документа (draft, sent, approved, rejected)';
COMMENT ON COLUMN document.version IS 'Версия документа';

-- Сообщения чата
CREATE TABLE chat_message (
                              id          BIGSERIAL PRIMARY KEY,
                              order_id    BIGINT NOT NULL REFERENCES orders(id),
                              user_id     UUID NOT NULL REFERENCES "user"(id),
                              message     TEXT NOT NULL,
                              created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE chat_message IS 'Сообщения в чате по заказу';
COMMENT ON COLUMN chat_message.id IS 'Идентификатор сообщения';
COMMENT ON COLUMN chat_message.order_id IS 'ID заказа';
COMMENT ON COLUMN chat_message.user_id IS 'ID пользователя';
COMMENT ON COLUMN chat_message.message IS 'Текст сообщения';
COMMENT ON COLUMN chat_message.created_at IS 'Дата отправки сообщения';

-- Веб-камеры
CREATE TABLE web_camera (
                            id          BIGSERIAL PRIMARY KEY,
                            order_id    BIGINT NOT NULL REFERENCES orders(id),
                            name        VARCHAR(128) NOT NULL,
                            ip_address  VARCHAR(45) NOT NULL,
                            port        INTEGER
);

COMMENT ON TABLE web_camera IS 'Веб-камеры на объектах строительства';
COMMENT ON COLUMN web_camera.id IS 'Идентификатор камеры';
COMMENT ON COLUMN web_camera.order_id IS 'ID заказа';
COMMENT ON COLUMN web_camera.name IS 'Название камеры';
COMMENT ON COLUMN web_camera.ip_address IS 'IP-адрес камеры';
COMMENT ON COLUMN web_camera.port IS 'Порт камеры';