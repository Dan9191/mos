-- Индексы для таблицы file_entity
CREATE INDEX idx_file_entity_owner ON file_entity(owner_type, owner_id);
CREATE INDEX idx_file_entity_role ON file_entity(owner_type, owner_id, file_role);

-- Индексы для таблицы orders
CREATE INDEX idx_orders_client ON orders(client_id);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

-- Индексы для таблицы order_status
CREATE INDEX idx_order_status_order ON order_status(order_id);
CREATE INDEX idx_order_status_created ON order_status(order_id, created_at DESC);

-- Индексы для таблицы order_stage
CREATE INDEX idx_order_stage_order ON order_stage(order_id);
CREATE INDEX idx_order_stage_completed ON order_stage(order_id, is_completed);
CREATE INDEX idx_order_stage_type ON order_stage(order_id, type_id);

-- Индексы для таблицы chat_message
CREATE INDEX idx_chat_message_order ON chat_message(order_id);
CREATE INDEX idx_chat_message_created ON chat_message(order_id, created_at DESC);

-- Индексы для таблицы web_camera
CREATE INDEX idx_web_camera_order ON web_camera(order_id);
CREATE INDEX idx_web_camera_status ON web_camera(is_active, status);