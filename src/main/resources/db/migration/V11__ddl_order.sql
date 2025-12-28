ALTER TABLE orders ADD COLUMN client_contact VARCHAR(256);

COMMENT ON COLUMN orders.client_contact IS 'Контактная информация (телефон или email) клиента';