ALTER TABLE platform_settings
    ADD COLUMN community_id VARCHAR(255);

ALTER TABLE platform_settings
    ADD COLUMN api_version VARCHAR(20) NOT NULL DEFAULT '5.199';
