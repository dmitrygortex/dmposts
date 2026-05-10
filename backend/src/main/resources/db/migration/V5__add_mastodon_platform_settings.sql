ALTER TABLE platform_settings
    ADD COLUMN instance_url VARCHAR(2048);

INSERT INTO platform_settings (platform, enabled, mode)
VALUES ('MASTODON', true, 'AUTO_WITH_MANUAL_FALLBACK')
ON CONFLICT (platform) DO NOTHING;
