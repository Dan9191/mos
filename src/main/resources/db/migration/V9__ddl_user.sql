DO $$
BEGIN
    -- Проверяем существование колонки
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'user'
        AND column_name = 'username'
        AND table_schema = 'public'
    ) THEN

-- Добавляем колонку
ALTER TABLE "user"
    ADD COLUMN username VARCHAR(128) UNIQUE;

COMMENT ON COLUMN "user".username IS 'Логин пользователя.';

-- Заполняем существующие записи email в качестве логина
UPDATE "user"
SET username = email
WHERE username IS NULL;

-- Добавляем NOT NULL ограничение
ALTER TABLE "user" ALTER COLUMN username SET NOT NULL;
END IF;
END $$;