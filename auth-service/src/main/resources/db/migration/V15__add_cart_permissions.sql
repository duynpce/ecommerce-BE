-- =============================================================================
-- V15 : Add CART permissions
-- =============================================================================
-- Context
--   CartController uses authorities:
--     CART:READ_SELF   → view own cart
--     CART:WRITE_SELF  → add / update / delete items in own cart
-- =============================================================================


-- ── 1. Insert CART permissions ───────────────────────────────────────────────
INSERT INTO permissions (resource, action, scope) VALUES
    ('CART', 'READ',  'SELF'),
    ('CART', 'WRITE', 'SELF')
ON CONFLICT (resource, action, scope) DO NOTHING;


-- ── 2. Assign CART permissions to roles ──────────────────────────────────────

-- SUPER_ADMIN → all CART permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'CART'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → all CART permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'CART'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → all CART permissions (contributor can also buy and manage their own cart)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'CART'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;

-- CUSTOMER → all CART permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'CART'
WHERE  r.name = 'CUSTOMER'
ON CONFLICT DO NOTHING;
