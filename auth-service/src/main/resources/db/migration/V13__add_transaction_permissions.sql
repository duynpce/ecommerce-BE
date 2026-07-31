-- =============================================================================
-- V13 : Add TRANSACTION permissions
-- =============================================================================
-- Context
--   TransactionController uses authorities:
--     TRANSACTION:CREATE:SELF  → create own transaction        (CUSTOMER)
--     TRANSACTION:WRITE:SELF   → update own transaction        (CONTRIBUTOR)
--     TRANSACTION:WRITE:ALL    → update any transaction        (ADMIN, SUPER_ADMIN)
-- =============================================================================


-- ── 1. Insert TRANSACTION permissions ────────────────────────────────────────
INSERT INTO permissions (resource, action, scope) VALUES
    ('TRANSACTION', 'CREATE', 'SELF'),
    ('TRANSACTION', 'WRITE',  'SELF'),
    ('TRANSACTION', 'WRITE',  'ALL')
ON CONFLICT (resource, action, scope) DO NOTHING;


-- ── 2. Assign TRANSACTION permissions to roles ───────────────────────────────

-- SUPER_ADMIN → all TRANSACTION permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → TRANSACTION:WRITE:ALL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'WRITE'
                    AND p.scope    = 'ALL'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → TRANSACTION:WRITE:SELF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'WRITE'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;

-- CUSTOMER → TRANSACTION:CREATE:SELF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'CREATE'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CUSTOMER'
ON CONFLICT DO NOTHING;
