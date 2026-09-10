-- Shippers can view and progress only delivery work assigned to them.
INSERT INTO roles (name) VALUES ('SHIPPER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO permissions (resource, action, scope) VALUES
    ('DELIVERY', 'READ',  'SELF'),
    ('DELIVERY', 'WRITE', 'SELF'),
    ('DELIVERY', 'READ',  'ALL'),
    ('DELIVERY', 'WRITE', 'ALL')
ON CONFLICT (resource, action, scope) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource = 'DELIVERY' AND p.scope = 'SELF'
WHERE r.name = 'SHIPPER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource = 'DELIVERY' AND p.scope = 'ALL'
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource = 'DELIVERY'
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;
