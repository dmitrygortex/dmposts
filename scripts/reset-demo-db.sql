\set ON_ERROR_STOP on

-- Local Docker Compose demo/dev database cleanup.
-- Run only against the local content_crm database:
-- docker compose exec -T postgres psql -U content_crm -d content_crm < scripts/reset-demo-db.sql

BEGIN;

DO $$
DECLARE
    missing_emails TEXT;
BEGIN
    IF current_database() <> 'content_crm' OR current_user <> 'content_crm' THEN
        RAISE EXCEPTION 'Refusing to reset database %, user %. Expected local content_crm/content_crm.',
            current_database(), current_user;
    END IF;

    SELECT string_agg(expected.email, ', ' ORDER BY expected.email)
    INTO missing_emails
    FROM (VALUES
        ('owner@example.com'),
        ('manager@example.com'),
        ('executor@example.com')
    ) AS expected(email)
    WHERE NOT EXISTS (
        SELECT 1
        FROM users
        WHERE lower(users.email) = expected.email
    );

    IF missing_emails IS NOT NULL THEN
        RAISE EXCEPTION 'Demo users are missing: %. Create OWNER, CONTENT_MANAGER and EXECUTOR before cleanup.',
            missing_emails;
    END IF;
END $$;

DELETE FROM publication_attempts;
DELETE FROM publication_variants;
DELETE FROM approvals;
DELETE FROM media_files;
DELETE FROM tasks;
DELETE FROM notifications;
DELETE FROM content_units;

DELETE FROM users
WHERE lower(email) NOT IN (
    'owner@example.com',
    'manager@example.com',
    'executor@example.com'
);

UPDATE users
SET
    full_name = CASE lower(email)
        WHEN 'owner@example.com' THEN 'Дмитрий Голиков'
        WHEN 'manager@example.com' THEN 'Контент Менеджер'
        WHEN 'executor@example.com' THEN 'Дизайнер Исполнитель'
        ELSE full_name
    END,
    role = CASE lower(email)
        WHEN 'owner@example.com' THEN 'OWNER'
        WHEN 'manager@example.com' THEN 'CONTENT_MANAGER'
        WHEN 'executor@example.com' THEN 'EXECUTOR'
        ELSE role
    END,
    is_active = true,
    updated_at = now()
WHERE lower(email) IN (
    'owner@example.com',
    'manager@example.com',
    'executor@example.com'
);

INSERT INTO platform_settings (platform, enabled, mode)
VALUES
    ('TELEGRAM', true, 'AUTO'),
    ('VK', true, 'AUTO_WITH_MANUAL_FALLBACK'),
    ('MASTODON', true, 'AUTO_WITH_MANUAL_FALLBACK'),
    ('TENCHAT', true, 'MANUAL'),
    ('SETKA', true, 'MANUAL'),
    ('MAX', true, 'MANUAL')
ON CONFLICT (platform) DO UPDATE
SET
    enabled = EXCLUDED.enabled,
    mode = EXCLUDED.mode,
    access_token_encrypted = NULL,
    community_id = NULL,
    manual_url = NULL,
    instance_url = NULL,
    updated_at = now();

SELECT setval(pg_get_serial_sequence('content_units', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('tasks', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('media_files', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('approvals', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('publication_variants', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('publication_attempts', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('notifications', 'id'), 1, false);
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT max(id) FROM users), true);

COMMIT;

SELECT id, email, full_name, role, is_active
FROM users
ORDER BY id;

SELECT
    (SELECT count(*) FROM content_units) AS content_units,
    (SELECT count(*) FROM tasks) AS tasks,
    (SELECT count(*) FROM media_files) AS media_files,
    (SELECT count(*) FROM approvals) AS approvals,
    (SELECT count(*) FROM publication_variants) AS publication_variants,
    (SELECT count(*) FROM publication_attempts) AS publication_attempts,
    (SELECT count(*) FROM notifications) AS notifications;
