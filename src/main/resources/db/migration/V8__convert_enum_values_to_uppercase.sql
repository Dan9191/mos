-- Convert enum values to uppercase for order_stage_type
UPDATE order_stage_type SET name = UPPER(name);

-- Convert enum values to uppercase for order_status_type
UPDATE order_status_type SET name = UPPER(name);
