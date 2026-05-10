CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE content_units (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    base_text TEXT,
    content_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    responsible_user_id BIGINT REFERENCES users(id),
    planned_publish_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    content_unit_id BIGINT NOT NULL REFERENCES content_units(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    assignee_id BIGINT NOT NULL REFERENCES users(id),
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    deadline TIMESTAMP,
    result_comment TEXT,
    review_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE media_files (
    id BIGSERIAL PRIMARY KEY,
    content_unit_id BIGINT NOT NULL REFERENCES content_units(id),
    task_id BIGINT REFERENCES tasks(id),
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL CHECK (size > 0),
    path TEXT NOT NULL,
    uploaded_by_id BIGINT NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE approvals (
    id BIGSERIAL PRIMARY KEY,
    content_unit_id BIGINT NOT NULL REFERENCES content_units(id),
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMP
);

CREATE UNIQUE INDEX uq_approvals_one_pending_per_content_unit
ON approvals(content_unit_id)
WHERE status = 'PENDING';

CREATE TABLE publication_variants (
    id BIGSERIAL PRIMARY KEY,
    content_unit_id BIGINT NOT NULL REFERENCES content_units(id),
    platform VARCHAR(50) NOT NULL,
    adapted_text TEXT,
    scheduled_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    external_post_id VARCHAR(255),
    external_post_url TEXT,
    error_message TEXT,
    manual_instruction TEXT,
    manual_completed_by_id BIGINT REFERENCES users(id),
    manual_completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_publication_variant_content_platform UNIQUE (content_unit_id, platform)
);

CREATE TABLE publication_attempts (
    id BIGSERIAL PRIMARY KEY,
    publication_variant_id BIGINT NOT NULL REFERENCES publication_variants(id),
    attempt_number INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    response_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_publication_attempt_number UNIQUE (publication_variant_id, attempt_number)
);

CREATE TABLE platform_settings (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(50) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    mode VARCHAR(50) NOT NULL,
    access_token_encrypted TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    link TEXT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_content_units_status ON content_units(status);
CREATE INDEX idx_content_units_responsible_user ON content_units(responsible_user_id);
CREATE INDEX idx_content_units_planned_publish_at ON content_units(planned_publish_at);

CREATE INDEX idx_tasks_content_unit ON tasks(content_unit_id);
CREATE INDEX idx_tasks_assignee_status ON tasks(assignee_id, status);
CREATE INDEX idx_tasks_deadline ON tasks(deadline);

CREATE INDEX idx_media_files_content_unit ON media_files(content_unit_id);
CREATE INDEX idx_media_files_task ON media_files(task_id);

CREATE INDEX idx_approvals_content_unit ON approvals(content_unit_id);
CREATE INDEX idx_approvals_status ON approvals(status);

CREATE INDEX idx_publication_variants_content_unit ON publication_variants(content_unit_id);
CREATE INDEX idx_publication_variants_status_scheduled_at ON publication_variants(status, scheduled_at);
CREATE INDEX idx_publication_variants_platform ON publication_variants(platform);

CREATE INDEX idx_publication_attempts_variant ON publication_attempts(publication_variant_id);

CREATE INDEX idx_notifications_user_is_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
