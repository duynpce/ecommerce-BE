-- Voucher management: platform-wide for admins, own-shop for contributors.
INSERT INTO permissions (resource, action, scope) VALUES
    ('VOUCHER', 'READ',   'ALL'),
    ('VOUCHER', 'WRITE',  'ALL'),
    ('VOUCHER', 'DELETE', 'ALL'),
    ('VOUCHER', 'READ',   'SELF'),
    ('VOUCHER', 'WRITE',  'SELF'),
    ('VOUCHER', 'DELETE', 'SELF')
ON CONFLICT (resource, action, scope) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource = 'VOUCHER' AND p.scope = 'ALL'
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource = 'VOUCHER' AND p.scope = 'SELF'
WHERE r.name = 'CONTRIBUTOR'
ON CONFLICT DO NOTHING;
