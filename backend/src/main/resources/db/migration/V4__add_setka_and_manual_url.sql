ALTER TABLE platform_settings
    ADD COLUMN manual_url VARCHAR(2048);

INSERT INTO platform_settings (platform, enabled, mode)
VALUES ('SETKA', true, 'MANUAL')
ON CONFLICT (platform) DO NOTHING;
