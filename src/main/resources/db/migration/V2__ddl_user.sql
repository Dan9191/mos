CREATE TABLE IF NOT EXISTS user_type (
                           id          SERIAL PRIMARY KEY,
                           name        VARCHAR(128) NOT NULL UNIQUE,
                           description TEXT
);

INSERT INTO user_type (id, name, description) VALUES
                                                  (1, 'User', 'Обычный пользователь системы'),
                                                  (2, 'Manager', 'Менеджер строительной компании'),
                                                  (3, 'Admin', 'Администратор системы')
    ON CONFLICT (name) DO NOTHING;

COMMENT ON TABLE user_type IS              'Типы пользователей.';
COMMENT ON COLUMN user_type.id IS          'Идентификатор типа пользователя.';
COMMENT ON COLUMN user_type.name IS        'Название типа пользователя.';
COMMENT ON COLUMN user_type.description IS 'Описание типа пользователя.';

CREATE TABLE IF NOT EXISTS "user" (
                        id          UUID     PRIMARY KEY,
                        type_id     INTEGER NOT NULL REFERENCES user_type(id),
                        first_name  VARCHAR(128) NOT NULL,
                        last_name   VARCHAR(128) NOT NULL,
                        middle_name VARCHAR(128),
                        email       VARCHAR(256) NOT NULL UNIQUE,
                        created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "user" IS              'Пользователи системы.';
COMMENT ON COLUMN "user".id IS          'Идентификатор пользователя.';
COMMENT ON COLUMN "user".type_id IS     'Ссылка на тип пользователя.';
COMMENT ON COLUMN "user".first_name IS  'Имя пользователя.';
COMMENT ON COLUMN "user".last_name IS   'Фамилия пользователя.';
COMMENT ON COLUMN "user".middle_name IS 'Отчество пользователя.';
COMMENT ON COLUMN "user".email IS       'Email пользователя.';
COMMENT ON COLUMN "user".created_at IS  'Дата и время создания пользователя.';