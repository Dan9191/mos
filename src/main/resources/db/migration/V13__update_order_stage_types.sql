-- 1. Сначала обновляем существующие записи
UPDATE order_stage_type SET
                            name = 'FOUNDATION',
                            description = 'Заливка фундамента',
                            is_mandatory = true,
                            display_order = 3
WHERE id = 1;

UPDATE order_stage_type SET
                            name = 'WALLS',
                            description = 'Возведение стен',
                            is_mandatory = true,
                            display_order = 4
WHERE id = 2;

UPDATE order_stage_type SET
                            name = 'ROOF',
                            description = 'Монтаж кровли',
                            is_mandatory = true,
                            display_order = 6
WHERE id = 3;

UPDATE order_stage_type SET
                            name = 'FINISHING',
                            description = 'Финишная внутренняя отделка',
                            is_mandatory = true,
                            display_order = 15
WHERE id = 4;

UPDATE order_stage_type SET
                            name = 'ELECTRICAL',
                            description = 'Электромонтажные работы',
                            is_mandatory = false,
                            display_order = 11
WHERE id = 5;

UPDATE order_stage_type SET
                            name = 'PLUMBING',
                            description = 'Сантехнические работы',
                            is_mandatory = false,
                            display_order = 12
WHERE id = 6;

UPDATE order_stage_type SET
                            name = 'LANDSCAPING',
                            description = 'Благоустройство территории',
                            is_mandatory = false,
                            display_order = 17
WHERE id = 7;

-- 2. Добавляем недостающие этапы согласно полной последовательности
INSERT INTO order_stage_type (name, description, is_mandatory, display_order) VALUES

-- STATUS: CONSTRUCTION (Строительство)
('SITE_PREPARATION', 'Подготовка участка: расчистка и разметка', TRUE, 1),
('EXCAVATION', 'Земляные работы: котлован и траншеи', TRUE, 2),
-- FOUNDATION уже есть (id=1, display_order=3)
-- WALLS уже есть (id=2, display_order=4)
('FLOOR_SLABS', 'Устройство межэтажных перекрытий и лестниц', TRUE, 5),
-- ROOF уже есть (id=3, display_order=6)
('WINDOWS_DOORS', 'Установка окон и дверей', TRUE, 7),
('EXTERIOR_WALLS', 'Наружная отделка и утепление фасада', TRUE, 8),
('EXTERIOR_INSULATION', 'Дополнительное утепление наружных стен', FALSE, 9),
('HEATING_VENTILATION', 'Монтаж отопления и вентиляции', FALSE, 10),
-- ELECTRICAL уже есть (id=5, display_order=11)
-- PLUMBING уже есть (id=6, display_order=12)
('WALL_PREPARATION', 'Выравнивание стен и потолков', TRUE, 13),
('FLOOR_COVERING', 'Укладка напольных покрытий', TRUE, 14),
-- FINISHING уже есть (id=4, display_order=15)
('PAINTING_DECORATING', 'Покраска и декорирование помещений', TRUE, 16);
-- LANDSCAPING уже есть (id=7, display_order=17)