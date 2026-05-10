INSERT INTO platform_settings (platform, enabled, mode)
VALUES
    ('TELEGRAM', true, 'AUTO'),
    ('VK', true, 'AUTO_WITH_MANUAL_FALLBACK'),
    ('TENCHAT', true, 'MANUAL'),
    ('MAX', true, 'MANUAL')
ON CONFLICT (platform) DO NOTHING;
