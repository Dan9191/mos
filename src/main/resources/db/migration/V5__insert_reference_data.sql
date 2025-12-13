-- Вставка типов статусов заказа
INSERT INTO order_status_type (name, description) VALUES
                                                      ('new', 'Новый заказ'),
                                                      ('documentation', 'Подготовка документации'),
                                                      ('construction', 'Строительство'),
                                                      ('completion', 'Завершение работ'),
                                                      ('closed', 'Заказ закрыт')
    ON CONFLICT (name) DO NOTHING;

-- Вставка типов этапов строительства
INSERT INTO order_stage_type (name, description, is_mandatory, display_order) VALUES
                                                                                  ('foundation', 'Заливка фундамента', TRUE, 1),
                                                                                  ('walls', 'Возведение стен', TRUE, 2),
                                                                                  ('roof', 'Монтаж кровли', TRUE, 3),
                                                                                  ('finishing', 'Внутренняя отделка', TRUE, 4),
                                                                                  ('electrical', 'Электромонтажные работы', FALSE, 5),
                                                                                  ('plumbing', 'Сантехнические работы', FALSE, 6),
                                                                                  ('landscaping', 'Благоустройство территории', FALSE, 7)
    ON CONFLICT (name) DO NOTHING;

-- Вставка типов документов
INSERT INTO document_type (name, description) VALUES
                                                  ('contract', 'Договор подряда'),
                                                  ('estimate', 'Смета'),
                                                  ('site_plan', 'План участка'),
                                                  ('acceptance_certificate', 'Акт приема-передачи'),
                                                  ('permit', 'Разрешительная документация'),
                                                  ('technical_documentation', 'Техническая документация')
    ON CONFLICT (name) DO NOTHING;