ALTER TABLE order ADD COLUMN client_contact VARCHAR(256);

COMMENT ON COLUMN order.client_contact IS 'Контактная информация (телефон или email) клиента';