
ALTER TABLE promotion_tickets
DROP CONSTRAINT IF EXISTS uq_promotion_shop_name,
    DROP COLUMN IF EXISTS shop_name,
    DROP COLUMN IF EXISTS delivery_address;