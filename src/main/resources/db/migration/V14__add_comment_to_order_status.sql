-- Добавляем поле comment в таблицу order_status
ALTER TABLE order_status ADD COLUMN comment TEXT;

COMMENT ON COLUMN order_status.comment IS 'Комментарий к статусу';