-- =============================================================================
-- V11 : Add TRANSACTION permissions + PRODUCT scoped permissions
--       + PROFILE:COMPLETE:SELF permission
-- =============================================================================
-- Context
--   TransactionController uses two authorities that were never seeded:
--     TRANSACTION:READ:SELF  → /transactions/search        (own transactions)
--     TRANSACTION:READ:ALL   → /transactions/admin/search  (all transactions)
--
--   ProductController has no @PreAuthorize guards and no scoped permissions.
--   V2 only seeded the legacy PRODUCT:MANAGE:ALL catch-all.
--   Granular WRITE and DELETE permissions are introduced here:
--     PRODUCT:WRITE:SELF   → create / update own product   (CONTRIBUTOR)
--     PRODUCT:WRITE:ALL    → create / update any product   (ADMIN, SUPER_ADMIN)
--     PRODUCT:DELETE:SELF  → delete own product            (CONTRIBUTOR)
--     PRODUCT:DELETE:ALL   → delete any product            (ADMIN, SUPER_ADMIN)
--
--   PROFILE:COMPLETE:SELF is a special-purpose permission for inactive accounts.
--   It is injected directly into their token by the auth layer — no role holds it.
-- =============================================================================


-- ── 1. Insert new permissions ─────────────────────────────────────────────────
INSERT INTO permissions (resource, action, scope) VALUES
    ('TRANSACTION', 'READ',    'SELF'),
    ('TRANSACTION', 'READ',    'ALL'),
    ('PRODUCT',     'WRITE',   'SELF'),
    ('PRODUCT',     'WRITE',   'ALL'),
    ('PRODUCT',     'DELETE',  'SELF'),
    ('PRODUCT',     'DELETE',  'ALL'),
    ('PROFILE',     'COMPLETE','SELF')
ON CONFLICT (resource, action, scope) DO NOTHING;


-- ── 2. TRANSACTION permissions ────────────────────────────────────────────────
-- SUPER_ADMIN → all TRANSACTION permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → TRANSACTION:READ:ALL only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'READ'
                    AND p.scope    = 'ALL'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → TRANSACTION:READ:SELF only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'READ'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;

-- CUSTOMER → TRANSACTION:READ:SELF only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'TRANSACTION'
                    AND p.action   = 'READ'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CUSTOMER'
ON CONFLICT DO NOTHING;


-- ── 3. PRODUCT:WRITE permissions ──────────────────────────────────────────────
-- SUPER_ADMIN → PRODUCT:WRITE:SELF + PRODUCT:WRITE:ALL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'WRITE'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → PRODUCT:WRITE:ALL only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'WRITE'
                    AND p.scope    = 'ALL'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → PRODUCT:WRITE:SELF only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'WRITE'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;


-- ── 4. PRODUCT:DELETE permissions ─────────────────────────────────────────────
-- SUPER_ADMIN → PRODUCT:DELETE:SELF + PRODUCT:DELETE:ALL
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'DELETE'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → PRODUCT:DELETE:ALL only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'DELETE'
                    AND p.scope    = 'ALL'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CONTRIBUTOR → PRODUCT:DELETE:SELF only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'PRODUCT'
                    AND p.action   = 'DELETE'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;


-- ── 5. PROFILE:COMPLETE:SELF — intentionally assigned to NO role ──────────────
--   Inactive accounts receive this permission injected directly into their token.
--   It grants access exclusively to the complete-profile endpoint.
