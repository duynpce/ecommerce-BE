-- =============================================================================
-- V14 : Add REVIEW permissions
-- =============================================================================
-- Context
--   ReviewController uses authorities:
--     REVIEW:WRITE:SELF  → create / update own review   (CUSTOMER)
-- =============================================================================


-- ── 1. Insert REVIEW permissions ─────────────────────────────────────────────
INSERT INTO permissions (resource, action, scope) VALUES
    ('REVIEW', 'WRITE', 'SELF')
ON CONFLICT (resource, action, scope) DO NOTHING;


-- ── 2. Assign REVIEW permissions to roles ────────────────────────────────────

-- SUPER_ADMIN → all REVIEW permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'REVIEW'
WHERE  r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN → all REVIEW permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'REVIEW'
WHERE  r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- CUSTOMER → REVIEW:WRITE:SELF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM   roles       r
JOIN   permissions p ON p.resource = 'REVIEW'
                    AND p.action   = 'WRITE'
                    AND p.scope    = 'SELF'
WHERE  r.name = 'CUSTOMER'
ON CONFLICT DO NOTHING;
