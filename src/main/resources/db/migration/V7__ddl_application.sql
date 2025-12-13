CREATE TABLE IF NOT EXISTS application_status (
                                                  id          INTEGER PRIMARY KEY,
                                                  name        VARCHAR(50) NOT NULL UNIQUE,
                                                  description TEXT
);

COMMENT ON TABLE application_status IS              'Статусы заявок.';
COMMENT ON COLUMN application_status.id IS          'Идентификатор статуса заявки.';
COMMENT ON COLUMN application_status.name IS        'Кодовое имя статуса заявки.';
COMMENT ON COLUMN application_status.description IS 'Описание статуса заявки.';

INSERT INTO application_status (id, name, description) VALUES
                                                           (1, 'created',        'Заявка создана'),
                                                           (2, 'consideration',  'Заявка на рассмотрении'),
                                                           (3, 'accepted',       'Заявка принята'),
                                                           (4, 'rejected',       'Заявка отклонена')
    ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS application (
                                           id          BIGSERIAL PRIMARY KEY,
                                           creator_id  UUID NOT NULL,
                                           status_id   INTEGER NOT NULL,
                                           project_id  BIGINT NOT NULL REFERENCES project_template(id),
                                           manager_id  UUID,
                                           created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_application_status
    FOREIGN KEY (status_id)
    REFERENCES application_status (id)
);

COMMENT ON TABLE application IS               'Заявки пользователей.';
COMMENT ON COLUMN application.id IS           'ID заявки.';
COMMENT ON COLUMN application.creator_id IS   'ID пользователя, создавшего заявку.';
COMMENT ON COLUMN application.project_id IS   'ID выбранного шаблона проекта.';
COMMENT ON COLUMN application.status_id IS    'ID текущего статуса заявки.';
COMMENT ON COLUMN application.manager_id IS   'ID менеджера, рассматривающего заявку.';
COMMENT ON COLUMN application.created_at IS   'Дата и время создания заявки.';

CREATE INDEX idx_application_creator_id ON application (creator_id);
CREATE INDEX idx_application_manager_id ON application (manager_id);
CREATE INDEX idx_application_status_id ON application (status_id);