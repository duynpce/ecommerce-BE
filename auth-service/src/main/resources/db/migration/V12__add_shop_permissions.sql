-- =============================================================================
-- V12 : Add SHOP permissions
-- =============================================================================
-- Context
--   ShopController uses authorities:
--     SHOP:WRITE:SELF   → create / update own shop (CONTRIBUTOR)
--     SHOP:DELETE:SELF  → delete own shop          (CONTRIBUTOR)
--     SHOP:DELETE:ALL   → delete any shop          (ADMIN, SUPER_ADMIN)
-- =============================================================================


-- ── 1. Insert SHOP permissions ────────────────────────────────────────────────
INSERT INTO permissions (resource, action, scope) VALUES
    ('SHOP', 'WRITE',  'SELF'),
    ('SHOP', 'DELETE', 'SELF'),
    ('SHOP', 'DELETE', 'ALL')
ON CONFLICT (resource, action, scope) DO NOTHING;


-- ── 2. Assign SHOP permissions to roles ───────────────────────────────────────

-- SUPER_ADMIN → all SHOP permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'SHOP'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → SHOP:DELETE:ALL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'SHOP'
                    AND p.action   = 'DELETE'
                    AND p.scope    = 'ALL'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → SHOP:WRITE:SELF + SHOP:DELETE:SELF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'SHOP'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;
