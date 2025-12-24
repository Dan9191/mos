ALTER TABLE application
    ADD COLUMN contact VARCHAR(100);

UPDATE application a
SET contact = u.email
    FROM "user" u
WHERE a.creator_id = u.id;

ALTER TABLE application
    ALTER COLUMN contact SET NOT NULL;

COMMENT ON COLUMN application.contact IS 'Контактная информация (телефон или email) заявителя. Первоначально заполнено email из таблицы user.';