-- Convert enum values to uppercase for order_stage_type
UPDATE document_type SET name = UPPER(name);
